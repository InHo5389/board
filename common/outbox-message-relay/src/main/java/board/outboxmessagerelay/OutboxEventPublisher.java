package board.outboxmessagerelay;

import board.Snowflake;
import event.Event;
import event.EventPayload;
import event.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

// 아웃박스 이벤트를 만드는 그 이벤트 퍼블리셔
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final Snowflake outboxIdSnowflake = new Snowflake();
    private final Snowflake eventIdSnowflake = new Snowflake();
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 아티클 서비스는 이 모듈을 가져와서 OutboxEventPublisher를 통해서 이벤트를 발행할건데 그에 해당 하는 메서드
     */
    public void publish(EventType type, EventPayload payload, Long shardKey) {
        // ex) articleId=10, shardKey==artticleId 동일할수도 있어서
        // 물리적 샤드가 어딨는지 알아차리기 위해서 10 % 4 = 2(물리적 샤드)
        Outbox outbox = Outbox.create(
                outboxIdSnowflake.nextId(),
                type,
                Event.of(
                        eventIdSnowflake.nextId(), type, payload
                ).toJson(),
                shardKey % MessageRelayConstants.SHARD_COUNT // Shard는 4개만 있다고 가정해서
        );
        applicationEventPublisher.publishEvent(OutboxEvent.of(outbox));
    }
}
