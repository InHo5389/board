package board.view.repository;

import board.view.entity.ArticleViewCount;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ArticleViewCountBackUpJpaRepositoryTest {

    @Autowired
    private ArticleViewCountBackUpJpaRepository articleViewCountBackUpJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("")
    void test(){
        //given
        articleViewCountBackUpJpaRepository.save(
                ArticleViewCount.init(1L, 0L)
        );
        entityManager.flush();
        entityManager.clear();
        //when
        int result1 = articleViewCountBackUpJpaRepository.updateViewCount(1L, 100L);
        int result2 = articleViewCountBackUpJpaRepository.updateViewCount(1L,300L);
        int result3 = articleViewCountBackUpJpaRepository.updateViewCount(1L,200L);
        //then
        assertThat(result1).isEqualTo(1);
        assertThat(result2).isEqualTo(1);
        assertThat(result3).isEqualTo(0);

        ArticleViewCount articleViewCount = articleViewCountBackUpJpaRepository.findById(1L).get();
        assertThat(articleViewCount.getViewCount()).isEqualTo(300L);
    }
}