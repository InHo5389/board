package board.articleread.service.event.handler;

import event.Event;
import event.EventPayload;

public interface EventHandler<T extends EventPayload> {
    void handle(Event<T> event);        // 이벤트를 받았을 때 처리
    boolean supports(Event<T> event);   // 이벤트 핸들러 구현체가 이 이벤트를 지원하는지 확인
}
