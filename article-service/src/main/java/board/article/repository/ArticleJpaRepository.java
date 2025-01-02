package board.article.repository;

import board.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleJpaRepository extends JpaRepository<Article,Long> {
}
