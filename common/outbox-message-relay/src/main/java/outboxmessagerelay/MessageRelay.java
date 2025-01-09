package outboxmessagerelay;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageRelay {

    private final OutboxRepository outboxRepository;
    private final MessageRelayCoordinator messageRelayCoordinator;
    private final KafkaTemplate<String, String> messageRelayKafkaTemplate;

    /**
     * TransactionalEventListener - 트랜잭션에 대한 이벤트를 받을 수 있음
     * 커밋되기 전에 아웃박스 이벤트를 받으면 받아서 아웃박스 리포지토리에 이제 저장
     * <p>
     * 그러면 이게 커밋되기 전이니까 그 데이터에 대한 비즈니스 로직이 처리되고 거기에
     * 트랜잭션으로 단일하게 묶임
     * 그래서 여기 아웃박스 테이블에 대한 삽입도 동일한 단일 트랜잭션으로 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void createOutbox(OutboxEvent outboxEvent) {
        log.info("[MessageRelay.createOutbox] outboxEvent={}", outboxEvent);
        outboxRepository.save(outboxEvent.getOutbox());
    }

    /**
     * 트랜잭션이 커밋된 다음 트랜잭션이 커밋되면 이제 비동기로 카프카 이벤트를 전송하는데
     * 여기서 실제 발행을 시행해 줄 건데 아웃박스 이벤트에서 아웃박스만 꺼내 가지고 카프카로 전송
     */
    @Async("messageRelayPublishEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishEvent(OutboxEvent outboxEvent) {
        publishEvent(outboxEvent.getOutbox());
    }

    private void publishEvent(Outbox outbox) {
        try {
            messageRelayKafkaTemplate.send(
                    outbox.getEventType().getTopic(),
                    String.valueOf(outbox.getShardKey()),
                    outbox.getPayload()
            ).get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("[MessageRelay.publishEvent] outbox={}", outbox, e);
        }
        outboxRepository.delete(outbox);
    }

    // 10초 동안 전송 안 된 이벤트들을 주기적으로 폴링 보냄
    /**
     * 10초 동안 전송 안 된 이벤트들을 주기적으로 폴링 보내고
     * 이거는 10초마다 수행될 거고 그리고 최초에 실행됐을 때는 5초 동안 딜레이
     *
     */
    @Scheduled(
            fixedDelay = 10,
            initialDelay = 5,
            timeUnit = TimeUnit.SECONDS,
            scheduler = "messageRelayPublishPendingEventExecutor"
    )
    public void publishPendingEvent(){
        AssignedShard assignedShard = messageRelayCoordinator.assignedShards();
        log.info("[MessageRelay.publishPendingEvent] assignedShard size={}", assignedShard);
        for (Long shard : assignedShard.getShards()) {
            List<Outbox> outboxes = outboxRepository.findAllBySharedKeyAndCreatedAtLessThanEqualOrderByCreatedAtAsc(
                    shard,
                    LocalDateTime.now().minusSeconds(10),
                    Pageable.ofSize(100)
            );

            for (Outbox outbox : outboxes) {
                publishEvent(outbox);
            }
        }
    }
}
