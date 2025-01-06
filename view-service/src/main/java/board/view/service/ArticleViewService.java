package board.view.service;

import board.view.repository.ArticleViewCountRedisRepository;
import board.view.repository.ArticleViewDistributedLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.IllegalFormatCodePointException;

@Service
@RequiredArgsConstructor
public class ArticleViewService {

    private final ArticleViewCountRedisRepository articleViewCountRedisRepository;
    private final ArticleViewCountBackUpProcessor articleViewCountBackUpProcessor;
    private final ArticleViewDistributedLockRepository articleViewDistributedLockRepository;

    private static final int BACK_UP_BACK_SIZE = 100;
    private static final Duration ABUSING_TTL = Duration.ofMinutes(10);

    public Long increase(Long articleId, Long userId) {
        if (!articleViewDistributedLockRepository.lock(articleId,userId,ABUSING_TTL)){
            return articleViewCountRedisRepository.read(articleId);
        }

        Long count = articleViewCountRedisRepository.increase(articleId);

        if (count % BACK_UP_BACK_SIZE == 0) {
            articleViewCountBackUpProcessor.backUp(articleId, count);
        }
        return count;
    }

    public Long count(Long articleId) {
        return articleViewCountRedisRepository.read(articleId);
    }
}
