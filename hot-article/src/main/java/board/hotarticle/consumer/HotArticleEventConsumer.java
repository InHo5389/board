package board.hotarticle.consumer;

import board.hotarticle.service.HotArticleService;
import event.Event;
import event.EventPayload;
import event.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotArticleEventConsumer {

    private final HotArticleService hotArticleService;

    // 프카 리스너라는 걸 쓰면은 그 토픽을 구독해서 이벤트를 처리 가능
    @KafkaListener(
            topics = {
                    EventType.Topic.BOARD_ARTICLE,
                    EventType.Topic.BOARD_COMMENT,
                    EventType.Topic.BOARD_LIKE,
                    EventType.Topic.BOARD_VIEW
            })
    /**
     * 컨슘할 때는 이렇게 스트림으로 메세지를 받아 잘 처리되었으면 이 Acknowledgment로 커밋할 수 있음
     * 레코드가 잘 처리되었다는 사실을 Kafka에 알려줄 수 있는데 먼저 저 메세지가 어떤 데이터인지 로그를 한번 찍어봄
     */
    public void listen(String message, Acknowledgment ack) {
        log.info("[HotArticleEventConsumer.listen] received message = {}", message);
        // 이벤트에 저희가 fromJson이라는 메소드를 만들어 놔 가지고 이거 전달된 메세지가 json 그래서 이벤트 객체로 변환
        Event<EventPayload> event = Event.fromJson(message);
        if (event != null){
            hotArticleService.handleEvent(event);
        }
        ack.acknowledge();
    }
}
