package board.article.service;

import board.Snowflake;
import board.article.entity.Article;
import board.article.entity.BoardArticleCount;
import board.article.repository.ArticleJpaRepository;
import board.article.repository.BoardArticleCountJpaRepository;
import board.article.service.request.ArticleRequest;
import board.article.service.response.ArticlePageResponse;
import board.article.service.response.ArticleResponse;
import event.EventType;
import event.payload.ArticleCreatedEventPayload;
import event.payload.ArticleDeletedEventPayload;
import event.payload.ArticleUpdatedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import board.outboxmessagerelay.OutboxEventPublisher;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final Snowflake snowflake = new Snowflake();
    private final ArticleJpaRepository articleJpaRepository;
    private final OutboxEventPublisher outboxEventPublisher;
    private final BoardArticleCountJpaRepository boardArticleCountJpaRepository;

    @Transactional
    public ArticleResponse create(ArticleRequest.Create request) {
        Article article = articleJpaRepository.save(
                Article.create(snowflake.nextId(), request.getTitle(), request.getContent(), request.getBoardId(), request.getWriterId())
        );

        int result = boardArticleCountJpaRepository.increase(request.getBoardId());
        if (result == 0) {
            BoardArticleCount boardArticleCount = BoardArticleCount.init(request.getBoardId(), 0L);
            boardArticleCountJpaRepository.save(boardArticleCount);
        }

        outboxEventPublisher.publish(
                EventType.ARTICLE_CREATED,
                ArticleCreatedEventPayload.builder()
                        .articleId(article.getArticleId())
                        .title(article.getTitle())
                        .content(article.getContent())
                        .boardId(article.getBoardId())
                        .writerId(article.getWriterId())
                        .createdAt(article.getCreatedAt())
                        .modifiedAt(article.getModifiedAt())
                        .boardArticleCount(count(article.getBoardId()))
                        .build(),
                article.getBoardId() // 동일한 단일 트랜잭션에서 동일한 Shard로 처리되어야 하니까 지금이 생성되는
                                     // 게 아티클의 Shard 키가 Board ID로 가정하여서
                                     // OutboxEventPublisher에서 나머지연산을 통하여 물리적 Shard로 라우팅이 돼서 동일한 Shard에서 트랜잭션이 처리
        );

        return ArticleResponse.from(article);
    }

    @Transactional
    public ArticleResponse update(Long articleId, ArticleRequest.Update request) {
        Article article = articleJpaRepository.findById(articleId).orElseThrow();
        article.update(request.getTitle(), request.getContent());

        outboxEventPublisher.publish(
                EventType.ARTICLE_UPDATED,
                ArticleUpdatedEventPayload.builder()
                        .articleId(article.getArticleId())
                        .title(article.getTitle())
                        .content(article.getContent())
                        .boardId(article.getBoardId())
                        .writerId(article.getWriterId())
                        .createdAt(article.getCreatedAt())
                        .modifiedAt(article.getModifiedAt())
                        .build(),
                article.getBoardId()
        );

        return ArticleResponse.from(article);
    }

    public ArticleResponse read(Long articleId) {
        return ArticleResponse.from(articleJpaRepository.findById(articleId).orElseThrow());
    }

    @Transactional
    public void delete(Long articleId) {
        Article article = articleJpaRepository.findById(articleId).orElseThrow();
        articleJpaRepository.delete(article);
        boardArticleCountJpaRepository.decrease(article.getBoardId());

        outboxEventPublisher.publish(
                EventType.ARTICLE_DELETED,
                ArticleDeletedEventPayload.builder()
                        .articleId(article.getArticleId())
                        .title(article.getTitle())
                        .content(article.getContent())
                        .boardId(article.getBoardId())
                        .writerId(article.getWriterId())
                        .createdAt(article.getCreatedAt())
                        .modifiedAt(article.getModifiedAt())
                        .boardArticleCount(count(article.getBoardId()))
                        .build(),
                article.getBoardId()
        );
    }

    public ArticlePageResponse readAll(Long boardId, Long page, Long pageSize) {
        return ArticlePageResponse.of(
                articleJpaRepository.findAll(boardId, (page - 1) * pageSize, pageSize).stream()
                        .map(ArticleResponse::from)
                        .toList(),
                articleJpaRepository.count(
                        boardId,
                        PageLimitCalculator.calculatorPageLimit(page, pageSize, 10L))
        );
    }

    public List<ArticleResponse> readAllInfiniteScroll(Long boardId, Long pageSize, Long lastArticleId) {
        List<Article> articles = lastArticleId == null ?
                articleJpaRepository.findAllInfiniteScroll(boardId, pageSize) :
                articleJpaRepository.findAllInfiniteScroll(boardId, pageSize, lastArticleId);
        return articles.stream().map(ArticleResponse::from).toList();
    }

    public Long count(Long boardId) {
        return boardArticleCountJpaRepository.findById(boardId)
                .map(BoardArticleCount::getArticleCount)
                .orElse(0L);
    }
}
