package board.view.service;

import board.view.entity.ArticleViewCount;
import board.view.repository.ArticleViewCountBackUpJpaRepository;
import event.EventType;
import event.payload.ArticleViewedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import board.outboxmessagerelay.OutboxEventPublisher;

@Component
@RequiredArgsConstructor
public class ArticleViewCountBackUpProcessor {

    private final OutboxEventPublisher outboxEventPublisher;
    private final ArticleViewCountBackUpJpaRepository articleViewCountBackUpJpaRepository;

    @Transactional
    public void backUp(Long articleId, Long viewCount) {
        int result = articleViewCountBackUpJpaRepository.updateViewCount(articleId, viewCount);
        if (result == 0) {
            articleViewCountBackUpJpaRepository.findById(articleId)
                    .ifPresentOrElse(ignored -> {},
                        ()->articleViewCountBackUpJpaRepository.save(
                                ArticleViewCount.init(articleId, viewCount))
                    );
        }

        outboxEventPublisher.publish(
                EventType.ARTICLE_VIEWED,
                ArticleViewedEventPayload.builder()
                        .articleId(articleId)
                        .articleViewCount(viewCount)
                        .build(),
                articleId
        );
    }
}
