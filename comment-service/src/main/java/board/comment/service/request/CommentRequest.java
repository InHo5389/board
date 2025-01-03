package board.comment.service.request;

import lombok.Getter;

public class CommentRequest {

    @Getter
    public static class Create{
        private Long articleId;
        private String content;
        private Long parentCommentId;
        private Long writerId;
    }
}
