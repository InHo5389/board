package board.articleread.client;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

// 커맨더 서버로 데이터를 요청하기 위한 클라이언트
@Slf4j
@Component
@RequiredArgsConstructor
public class LikeClient {

    private RestClient restClient;

    @Value("${endpoints.board-like-service.url}")
    private String likeServiceUrl;

    @PostConstruct
    public void initRestClient() {
        restClient = RestClient.create(likeServiceUrl);
    }

    public long count(Long articleId){
        try {
            return restClient.get()
                    .uri("/v1/article-likes/articles/{articleId}/count", articleId)
                    .retrieve()
                    .body(Long.class);
        }catch (Exception e){
            log.error("[LikeClient.count] = {}",articleId,e);
            return 0;
        }
    }
}
