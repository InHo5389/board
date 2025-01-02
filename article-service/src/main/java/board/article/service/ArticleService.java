package board.article.service;

import board.Snowflake;
import board.article.entity.Article;
import board.article.repository.ArticleJpaRepository;
import board.article.service.request.ArticleRequest;
import board.article.service.response.ArticleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final Snowflake snowflake = new Snowflake();
    private final ArticleJpaRepository articleJpaRepository;

    @Transactional
    public ArticleResponse create(ArticleRequest.Create request){
        Article article = articleJpaRepository.save(
                Article.create(snowflake.nextId(), request.getTitle(), request.getContent(), request.getBoardId(), request.getWriterId())
        );
        return ArticleResponse.from(article);
    }

    @Transactional
    public ArticleResponse update(Long articleId,ArticleRequest.Update request){
        Article article = articleJpaRepository.findById(articleId).orElseThrow();
        article.update(request.getTitle(), request.getContent());

        return ArticleResponse.from(article);
    }

    public ArticleResponse read(Long articleId){
        return ArticleResponse.from(articleJpaRepository.findById(articleId).orElseThrow());
    }

    @Transactional
    public void delete(Long articleId){
        articleJpaRepository.deleteById(articleId);
    }
}
