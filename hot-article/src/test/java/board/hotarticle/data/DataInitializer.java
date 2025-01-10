package board.hotarticle.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.random.RandomGenerator;

/**
 * article, view, comment, like 데이터 생성을 요청
 * <p>
 * 30개의 게시글 생성
 * 각 게시글마다:
 * 0~9개의 랜덤한 댓글
 * 0~9개의 랜덤한 좋아요
 * 0~199개의 랜덤한 조회수
 */
public class DataInitializer {
    RestClient articleServiceClient = RestClient.create("http://localhost:9000");
    RestClient commentServiceClient = RestClient.create("http://localhost:9001");
    RestClient likeServiceClient = RestClient.create("http://localhost:9002");
    RestClient viewServiceClient = RestClient.create("http://localhost:9003");

    @Test
    void initialize() throws JsonProcessingException {
        for (int i = 0; i < 30; i++) {
            // 게시글 생성
            Long articleId = createArticle();

            // 랜덤 수 생성
            long commentCount = RandomGenerator.getDefault().nextLong(10);
            long likeCount = RandomGenerator.getDefault().nextLong(10);
            long viewCount = RandomGenerator.getDefault().nextLong(200);

            createComment(articleId, commentCount);
            like(articleId, likeCount);
            view(articleId, viewCount);
        }
    }

    Long createArticle() {
        return articleServiceClient.post()
                .uri("/v1/articles")
                .body(new ArticleCreateRequest("title", "content", 1L, 1L))
                .retrieve()
                .body(ArticleResponse.class)
                .getArticleId();
    }

    @Getter
    @AllArgsConstructor
    static class ArticleCreateRequest {
        private String title;
        private String content;
        private Long writerId;
        private Long boardId;
    }

    @Getter
    static class ArticleResponse {
        private Long articleId;
    }

    @Test
    void test(){
        for (int i = 0; i < 30; i++) {
            Long articleId = createArticle();

            // 랜덤 수 생성
            long commentCount = RandomGenerator.getDefault().nextLong(10);
            System.out.println(commentCount);

            createComment(articleId, commentCount);

        }
    }

    void createComment(Long articleId, long commentCount) {
        while (commentCount-- > 0) {
            CommentRequest.Create request = new CommentRequest.Create(articleId, "content", null, 1L);

            try {
                // 요청 로깅 추가
                ObjectMapper objectMapper = new ObjectMapper();
                System.out.println("Request body: " + objectMapper.writeValueAsString(request));

                commentServiceClient.post()
                        .uri("/v1/comments")
                        .body(request)
                        .retrieve()
                        .toBodilessEntity();  // 응답 처리 추가
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    static class CommentRequest {

        @Getter
        @AllArgsConstructor
        static class Create {
            private Long articleId;
            private String content;
            private Long parentCommentId;
            private Long writerId;
        }
    }

    void like(Long articleId, long likeCount) {
        while (likeCount-- > 0) {
            likeServiceClient.post()
                    .uri("/v1/article-likes/articles/{articleId}/users/{userId}/pessimisticLock2",
                            articleId, likeCount + 1000)
                    .retrieve();
        }
    }

    void view(Long articleId, long viewCount) {
        while (viewCount-- > 0) {
            viewServiceClient.post()
                    .uri("/v1/article-views/articles/{articleId}/users/{userId}",
                            articleId, viewCount)
                    .retrieve();
        }
    }
}