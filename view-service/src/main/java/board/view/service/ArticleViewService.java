package board.view.service;

import board.view.repository.ArticleViewCountRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleViewService {

    private final ArticleViewCountRedisRepository articleViewCountRedisRepository;
    private final ArticleViewCountBackUpProcessor articleViewCountBackUpProcessor;

    private static final int BACK_UP_BACK_SIZE = 100;

    public Long increase(Long articleId, Long userId) {
        Long count = articleViewCountRedisRepository.increase(articleId);

        if (count % BACK_UP_BACK_SIZE == 0) {
            articleViewCountBackUpProcessor.backUp(articleId,count);
        }
        return count;
    }

    public Long count(Long articleId) {
        return articleViewCountRedisRepository.read(articleId);
    }
}
