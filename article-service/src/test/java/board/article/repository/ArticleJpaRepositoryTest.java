package board.article.repository;

import board.article.entity.Article;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


@Slf4j
@SpringBootTest
class ArticleJpaRepositoryTest {

    @Autowired
    private ArticleJpaRepository articleJpaRepository;

    @Test
    @DisplayName("")
    void findAllTest(){
        //given
        List<Article> articles = articleJpaRepository.findAll(1L, 1499970L, 30L);
        //when
        //then
        Assertions.assertThat(articles.size()).isEqualTo(30);
    }

    @Test
    @DisplayName("")
    void countTest(){
        //given
        Long count = articleJpaRepository.count(1L, 10000L);
        //when
        //then
        Assertions.assertThat(count).isEqualTo(10000);
    }
}