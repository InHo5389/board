package board.article.service;

import board.Snowflake;
import board.article.entity.Article;
import board.article.repository.ArticleJpaRepository;
import board.article.service.request.ArticleRequest;
import board.article.service.response.ArticlePageResponse;
import board.article.service.response.ArticleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final Snowflake snowflake = new Snowflake();
    private final ArticleJpaRepository articleJpaRepository;

    @Transactional
    public ArticleResponse create(ArticleRequest.Create request) {
        Article article = articleJpaRepository.save(
                Article.create(snowflake.nextId(), request.getTitle(), request.getContent(), request.getBoardId(), request.getWriterId())
        );
        return ArticleResponse.from(article);
    }

    @Transactional
    public ArticleResponse update(Long articleId, ArticleRequest.Update request) {
        Article article = articleJpaRepository.findById(articleId).orElseThrow();
        article.update(request.getTitle(), request.getContent());

        return ArticleResponse.from(article);
    }

    public ArticleResponse read(Long articleId) {
        return ArticleResponse.from(articleJpaRepository.findById(articleId).orElseThrow());
    }

    @Transactional
    public void delete(Long articleId) {
        articleJpaRepository.deleteById(articleId);
    }

    public ArticlePageResponse readAll(Long boardId, Long page, Long pageSize) {
        return ArticlePageResponse.of(
                articleJpaRepository.findAll(boardId, (page - 1) * pageSize, pageSize).stream()
                        .map(ArticleResponse::from)
                        .toList(),
                articleJpaRepository.count(
                        boardId,
                        PageLimitCalculator.calculatorPageLimit(page, pageSize, 10L))
        );
    }

    public List<ArticleResponse> readAllInfiniteScroll(Long boardId,Long pageSize,Long lastArticleId){
        List<Article> articles = lastArticleId == null ?
                articleJpaRepository.findAllInfiniteScroll(boardId, pageSize) :
                articleJpaRepository.findAllInfiniteScroll(boardId, pageSize, lastArticleId);
        return articles.stream().map(ArticleResponse::from).toList();
    }
}
