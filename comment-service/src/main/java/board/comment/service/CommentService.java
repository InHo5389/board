package board.comment.service;

import board.Snowflake;
import board.comment.entity.Comment;
import board.comment.repository.CommentJpaRepository;
import board.comment.service.request.CommentRequest;
import board.comment.service.response.CommentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.function.Predicate.not;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final Snowflake snowflake = new Snowflake();
    private final CommentJpaRepository commentJpaRepository;

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
                });
    }

    private boolean hasChildren(Comment comment) {
        return commentJpaRepository.countBy(comment.getArticleId(), comment.getCommentId(), 2L) == 2;
    }

    private void delete(Comment comment) {
        commentJpaRepository.delete(comment);
        // 재귀적 삭제
        if (!comment.isRoot()){
            commentJpaRepository.findById(comment.getParentCommentId())
                    .filter(Comment::getDeleted)
                    .filter(not(this::hasChildren))
                    .ifPresent(this::delete);
        }
    }
}
