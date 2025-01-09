package board.hotarticle.service;

import board.hotarticle.client.ArticleClient;
import board.hotarticle.repository.HotArticleListRepository;
import board.hotarticle.service.eventhandler.EventHandler;
import board.hotarticle.service.response.HotArticleResponse;
import event.Event;
import event.EventPayload;
import event.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 인기글 조회할 때 아티클 클라이언트를 통해서 원본 게시글에 대한 정보들을 조회해서 같이 응답
 * 핫아티클 리포지토리는 게시글 아이디만 저장하고 있어서 원본 정보는 여기에서 이제 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HotArticleService {

    private final ArticleClient articleClient;
    private final List<EventHandler> eventHandlers;
    private final HotArticleScoreUpdater hotArticleScoreUpdater;
    private final HotArticleListRepository hotArticleListRepository;

    // 이벤트를 통해서 인기글 점수를 계산해서 HotArticleListRepository에 인기글 아이디를 저장하는 메서드
    public void handleEvent(Event<EventPayload> event) {
        EventHandler<EventPayload> eventHandler = findEventHandler(event);
        if (eventHandler == null) {
            return;
        }

        //  이벤트가 게시글 생성 또는 삭제된 이벤트인지 검사
        if (isArticleCreatedOrDeleted(event)) {
            eventHandler.handle(event);
        } else {
            // 생성 또는 삭제 이벤트가아니면 점수 업데이트
            hotArticleScoreUpdater.update(event, eventHandler);
        }
    }

    public List<HotArticleResponse> readAll(String dateStr) {
        return hotArticleListRepository.readAll(dateStr).stream()
                .map(articleClient::read)
                .filter(Objects::nonNull)
                .map(HotArticleResponse::from)
                .toList();
    }

    private EventHandler findEventHandler(Event<EventPayload> event) {
        return eventHandlers.stream()
                .filter(eventHandler -> eventHandler.supports(event))
                .findAny()
                .orElse(null);
    }

    private boolean isArticleCreatedOrDeleted(Event<EventPayload> event) {
        return EventType.ARTICLE_CREATED == event.getType() || EventType.ARTICLE_DELETED == event.getType();
    }
}
