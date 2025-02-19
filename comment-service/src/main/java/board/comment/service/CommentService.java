package board.comment.service;

import board.Snowflake;
import board.comment.entity.ArticleCommentCount;
import board.comment.entity.Comment;
import board.comment.repository.ArticleCommentCountJpaRepository;
import board.comment.repository.CommentJpaRepository;
import board.comment.service.request.CommentRequest;
import board.comment.service.response.CommentPageResponse;
import board.comment.service.response.CommentResponse;
import event.EventType;
import event.payload.CommentCreatedEventPayload;
import event.payload.CommentDeletedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import board.outboxmessagerelay.OutboxEventPublisher;

import java.util.List;

import static java.util.function.Predicate.not;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final Snowflake snowflake = new Snowflake();
    private final CommentJpaRepository commentJpaRepository;
    private final OutboxEventPublisher outboxEventPublisher;
    private final ArticleCommentCountJpaRepository articleCommentCountJpaRepository;

    @Transactional
    public CommentResponse create(CommentRequest.Create request) {
        Comment parent = findParent(request);

        Comment comment = Comment.create(
                snowflake.nextId(),
                request.getContent(),
                parent == null ? null : parent.getParentCommentId(),
                request.getArticleId(),
                request.getWriterId()
        );
        commentJpaRepository.save(comment);

        int result = articleCommentCountJpaRepository.increase(request.getArticleId());
        if (result == 0) {
            ArticleCommentCount articleCommentCount = ArticleCommentCount.init(request.getArticleId(), 1L);
            articleCommentCountJpaRepository.save(articleCommentCount);
        }

        outboxEventPublisher.publish(
                EventType.COMMENT_CREATED,
                CommentCreatedEventPayload.builder()
                        .commentId(comment.getCommentId())
                        .content(comment.getContent())
                        .articleId(comment.getArticleId())
                        .writerId(comment.getWriterId())
                        .deleted(comment.getDeleted())
                        .createdAt(comment.getCreatedAt())
                        .articleCommentCount(count(comment.getArticleId()))
                        .build(),
                comment.getArticleId()
        );

        return CommentResponse.from(comment);
    }

    private Comment findParent(CommentRequest.Create request) {
        Long parentCommentId = request.getParentCommentId();
        if (parentCommentId == null) {
            return null;
        }
        return commentJpaRepository.findById(parentCommentId)
                .filter(not(Comment::getDeleted))
                .filter(Comment::isRoot)
                .orElseThrow();
    }

    public CommentResponse read(Long commentId) {
        Comment comment = commentJpaRepository.findById(commentId).orElseThrow();

        return CommentResponse.from(comment);
    }

    @Transactional
    public void delete(Long commentId) {
        commentJpaRepository.findById(commentId)
                .filter(not(Comment::getDeleted))
                .ifPresent(comment -> {
                    if (hasChildren(comment)) {
                        comment.delete();
                    } else {
                        delete(comment);
                    }

                    outboxEventPublisher.publish(
                            EventType.COMMENT_DELETED,
                            CommentDeletedEventPayload.builder()
                                    .commentId(comment.getCommentId())
                                    .content(comment.getContent())
                                    .articleId(comment.getArticleId())
                                    .writerId(comment.getWriterId())
                                    .deleted(comment.getDeleted())
                                    .createdAt(comment.getCreatedAt())
                                    .articleCommentCount(count(comment.getArticleId()))
                                    .build(),
                            comment.getArticleId()
                    );
                });
    }

    private boolean hasChildren(Comment comment) {
        return commentJpaRepository.countBy(comment.getArticleId(), comment.getCommentId(), 2L) == 2;
    }

    private void delete(Comment comment) {
        commentJpaRepository.delete(comment);
        articleCommentCountJpaRepository.decrease(comment.getArticleId());
        // 재귀적 삭제
        if (!comment.isRoot()) {
            commentJpaRepository.findById(comment.getParentCommentId())
                    .filter(Comment::getDeleted)
                    .filter(not(this::hasChildren))
                    .ifPresent(this::delete);
        }
    }

    public CommentPageResponse readAll(Long articleId, Long page, Long pageSize) {
        return CommentPageResponse.of(
                commentJpaRepository.findAll(articleId, (page - 1) * pageSize, pageSize).stream()
                        .map(CommentResponse::from)
                        .toList(),
                commentJpaRepository.count(articleId, PageLimitCalculator.calculatorPageLimit(page, pageSize, 10L))
        );
    }

    public List<CommentResponse> readAll(Long articleId, Long lastParentCommentId, Long lastCommentId, Long limit) {
        List<Comment> comments = lastParentCommentId == null || lastCommentId == null ?
                commentJpaRepository.findAllInfiniteScroll(articleId, limit) :
                commentJpaRepository.findAllInfiniteScroll(articleId, lastParentCommentId, lastCommentId, limit);

        return comments.stream()
                .map(CommentResponse::from)
                .toList();
    }

    public Long count(Long articleId) {
        return articleCommentCountJpaRepository.findById(articleId)
                .map(ArticleCommentCount::getCommentCount)
                .orElse(0L);
    }
}
