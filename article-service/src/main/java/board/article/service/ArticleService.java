package board.article.service;

import board.Snowflake;
import board.article.entity.Article;
import board.article.entity.BoardArticleCount;
import board.article.repository.ArticleJpaRepository;
import board.article.repository.BoardArticleCountJpaRepository;
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
    private final BoardArticleCountJpaRepository boardArticleCountJpaRepository;

    @Transactional
    public ArticleResponse create(ArticleRequest.Create request) {
        Article article = articleJpaRepository.save(
                Article.create(snowflake.nextId(), request.getTitle(), request.getContent(), request.getBoardId(), request.getWriterId())
        );

        int result = boardArticleCountJpaRepository.increase(request.getBoardId());
        if (result == 0) {
            BoardArticleCount boardArticleCount = BoardArticleCount.init(request.getBoardId(), 0L);
            boardArticleCountJpaRepository.save(boardArticleCount);
        }
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
        Article article = articleJpaRepository.findById(articleId).orElseThrow();
        articleJpaRepository.delete(article);
        boardArticleCountJpaRepository.decrease(article.getBoardId());
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

    public List<ArticleResponse> readAllInfiniteScroll(Long boardId, Long pageSize, Long lastArticleId) {
        List<Article> articles = lastArticleId == null ?
                articleJpaRepository.findAllInfiniteScroll(boardId, pageSize) :
                articleJpaRepository.findAllInfiniteScroll(boardId, pageSize, lastArticleId);
        return articles.stream().map(ArticleResponse::from).toList();
    }

    public Long count(Long boardId) {
        return boardArticleCountJpaRepository.findById(boardId)
                .map(BoardArticleCount::getArticleCount)
                .orElse(0L);
    }
}
