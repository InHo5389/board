package board.articleread.client;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.Optional;

// 레디스의 원본 데이터가 없을때 커맨더 서버로 데이터를 요청하기 위한 클라이언트
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleClient {

    private RestClient restClient;

    @Value("${endpoints.board-article-service.url}")
    private String articleServiceUrl;

    @PostConstruct
    public void initRestClient() {
        restClient = RestClient.create(articleServiceUrl);
    }

    public Optional<ArticleResponse> read(Long articleId){
        try {
            ArticleResponse articleResponse = restClient.get()
                    .uri("/v1/articles/{articleId}", articleId)
                    .retrieve()
                    .body(ArticleResponse.class);
            return Optional.ofNullable(articleResponse);
        }catch (Exception e){
            log.error("[ArticleClient.read] = {}",articleId,e);
            return Optional.empty();
        }
    }

    // 아티클 클라이언트가 게시글 서비스에서 가져온 데이터들을 담아주는 응답 클래스
    @Getter
    public static class ArticleResponse {
        private Long articleId;
        private String title;
        private String content;
        private Long boardId;
        private Long writerId;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }
}
