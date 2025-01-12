package board.articleread.repository;

import board.articleread.client.ArticleClient;
import event.payload.*;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Redis에 저장되는 ArticleQueryModel
 * 게시글 데이터 외에도 댓글 수랑 좋아요 수도 비정규화 하여 가지고 있음
 */
@Getter
public class ArticleQueryModel {
    private Long articleId;
    private String title;
    private String content;
    private Long boardId;
    private Long writerId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Long articleCommentCount;
    private Long articleLikeCount;

    public static ArticleQueryModel create(ArticleCreatedEventPayload payload) {
        ArticleQueryModel articleQueryModel = new ArticleQueryModel();
        articleQueryModel.articleId = payload.getArticleId();
        articleQueryModel.title = payload.getTitle();
        articleQueryModel.content = payload.getContent();
        articleQueryModel.boardId = payload.getBoardId();
        articleQueryModel.writerId = payload.getWriterId();
        articleQueryModel.createdAt = payload.getCreatedAt();
        articleQueryModel.modifiedAt = payload.getModifiedAt();
        // 게시글이 처음 작성되었다면 댓글 수랑 좋아요 수가 없어서 0으로 초기화
        articleQueryModel.articleCommentCount = 0L;
        articleQueryModel.articleLikeCount = 0L;
        return articleQueryModel;
    }

    /**
     * 이벤트 뿐만 아니라 Redis에 데이터가 없으면 클라이언트 클래스들로 커맨드 서버에 데이터를 요청하는데
     * 커맨드 서버에서 받아온 데이터로 아티클 커리 모델을 만드는 메서드
     */
    public static ArticleQueryModel create(ArticleClient.ArticleResponse article, Long commentCount, Long likeCount) {
        ArticleQueryModel articleQueryModel = new ArticleQueryModel();
        articleQueryModel.articleId = article.getArticleId();
        articleQueryModel.title = article.getTitle();
        articleQueryModel.content = article.getContent();
        articleQueryModel.boardId = article.getBoardId();
        articleQueryModel.writerId = article.getWriterId();
        articleQueryModel.createdAt = article.getCreatedAt();
        articleQueryModel.modifiedAt = article.getModifiedAt();
        articleQueryModel.articleCommentCount = commentCount;
        articleQueryModel.articleLikeCount = likeCount;
        return articleQueryModel;
    }

    /**
     * 생성 삭제 이런 이벤트가 받으면은 아티클 커리 모델도 데이터를 업데이트 메서드
     */
    public void updateBy(CommentCreatedEventPayload payload) {
        this.articleCommentCount = payload.getArticleCommentCount();
    }

    public void updateBy(CommentDeletedEventPayload payload) {
        this.articleCommentCount = payload.getArticleCommentCount();
    }

    public void updateBy(ArticleLikedEventPayload payload) {
        this.articleLikeCount = payload.getArticleLikeCount();
    }

    public void updateBy(ArticleUnlikedEventPayload payload) {
        this.articleLikeCount = payload.getArticleLikeCount();
    }

    public void updateBy(ArticleUpdatedEventPayload payload) {
        this.title = payload.getTitle();
        this.content = payload.getContent();
        this.boardId = payload.getBoardId();
        this.writerId = payload.getWriterId();
        this.createdAt = payload.getCreatedAt();
        this.modifiedAt = payload.getModifiedAt();
    }
}
