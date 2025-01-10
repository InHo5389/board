package board.hotarticle.service.eventhandler;

import event.Event;
import event.EventPayload;

public interface EventHandler<T extends EventPayload> {
    void handle(Event<T> event);        // 이벤트를 받았을 때 처리
    boolean supports(Event<T> event);   // 이벤트 핸들러 구현체가 이 이벤트를 지원하는지 확인
    Long findArticleId(Event<T> event); // 이 이벤트가 어떤 아티클에 대한 건지 그 아티클 아이디를 찾음
}
