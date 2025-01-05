package board.like.service;

import board.like.entity.ArticleLikeCount;
import board.like.repository.ArticleLikeCountJpaRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class ArticleLikeServiceTest {

    @Autowired
    private ArticleLikeService articleLikeService;

    @Autowired
    private ArticleLikeCountJpaRepository articleLikeCountJpaRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void test() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(1000);
        TransactionTemplate template = new TransactionTemplate(transactionManager);

        for (int i = 0; i < 1000; i++) {
            long userId = i + 2;
            executorService.submit(() -> {
                try {
                    template.execute(status -> {
                        articleLikeService.likePessimisticLock1(1L, userId);
                        return null;
                    });
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
    }
}