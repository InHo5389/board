package board.articleread.service;

import board.articleread.client.ArticleClient;
import board.articleread.client.CommentClient;
import board.articleread.client.LikeClient;
import board.articleread.client.ViewClient;
import board.articleread.repository.ArticleQueryModel;
import board.articleread.repository.ArticleQueryModelRepository;
import board.articleread.service.event.handler.EventHandler;
import board.articleread.service.resposne.ArticleReadResponse;
import event.Event;
import event.EventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleReadService {

    private final ArticleClient articleClient;
    private final CommentClient commentClient;
    private final LikeClient likeClient;
    private final ViewClient viewClient;
    private final ArticleQueryModelRepository articleQueryModelRepository;
    private final List<EventHandler> eventHandlers;

    // 컨슈머 통해서 메서드 호출
    public void handleEvent(Event<EventPayload> event) {
        for (EventHandler eventHandler : eventHandlers) {
            if (eventHandler.supports(event)) {
                eventHandler.handle(event);
            }
        }
    }

    /**
     * 게시글 데이터를 조회하기 위한 메서드
     * 데이터를 꺼내서 없으면 람다 호출
     */
    public ArticleReadResponse read(Long articleId) {
        ArticleQueryModel articleQueryModel = articleQueryModelRepository.read(articleId)
                .or(() -> fetch(articleId))
                .orElseThrow();
        return ArticleReadResponse.from(
                articleQueryModel,
                viewClient.count(articleId)
        );
    }

    private Optional<? extends ArticleQueryModel> fetch(Long articleId) {
        Optional<ArticleQueryModel> articleQueryModelOptional = articleClient.read(articleId)
                .map(article -> ArticleQueryModel.create(
                        article,
                        commentClient.count(articleId),
                        likeClient.count(articleId)
                ));
        articleQueryModelOptional.ifPresent(articleQueryModel ->
                articleQueryModelRepository.create(articleQueryModel, Duration.ofDays(1)));
        log.info("[ArticleReadService.fetch] fetch data, articleId = {},isPresent = {}", articleId,articleQueryModelOptional.isPresent());
        return articleQueryModelOptional;
    }
}
