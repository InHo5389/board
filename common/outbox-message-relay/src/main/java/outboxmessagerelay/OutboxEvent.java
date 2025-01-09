package outboxmessagerelay;

import lombok.Getter;

// 아웃박스가 이벤트로 전달될 수 있도록 하는 클래스
@Getter
public class OutboxEvent {

    private Outbox outbox;

    public static OutboxEvent of(Outbox outbox) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.outbox = outbox;
        return outboxEvent;
    }
}
