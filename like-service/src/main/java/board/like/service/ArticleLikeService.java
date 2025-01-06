package board.like.service;

import board.Snowflake;
import board.like.entity.ArticleLike;
import board.like.entity.ArticleLikeCount;
import board.like.repository.ArticleLikeCountJpaRepository;
import board.like.repository.ArticleLikeJpaRepository;
import board.like.service.response.ArticleLikeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleLikeService {

    private final Snowflake snowflake = new Snowflake();
    private final ArticleLikeJpaRepository articleLikeJpaRepository;
    private final ArticleLikeCountJpaRepository articleLikeCountJpaRepository;

    public ArticleLikeResponse read(Long articleId, Long userId) {
        return articleLikeJpaRepository.findByArticleIdAndUserId(articleId, userId)
                .map(ArticleLikeResponse::from)
                .orElseThrow();
    }

    /**
     * update 구현
     */
    @Transactional
    public void likePessimisticLock1(Long articleId, Long userId) {
        ArticleLike articleLike = ArticleLike.create(
                snowflake.nextId(),
                articleId,
                userId
        );
        articleLikeJpaRepository.save(articleLike);

        int result = articleLikeCountJpaRepository.increase(articleId);
        if (result == 0) {
            // 최초 요청 시에는 update 되는 레코드가 없으므로 1로 초기화한다.
            // 트래픽이 순식간에 몰릴수 있는 상황에서는 유실될 수 있어서 게시글 생성 시점에 따라 미리 0으로 초기화 해야함
            ArticleLikeCount articleLikeCount = ArticleLikeCount.init(articleId, 1L);
            articleLikeCountJpaRepository.save(articleLikeCount);
        }
    }

    @Transactional
    public void unLikePessimisticLock1(Long articleId, Long userId) {
        articleLikeJpaRepository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(articleLike -> {
                    articleLikeJpaRepository.delete(articleLike);
                    articleLikeCountJpaRepository.decrease(articleId);
                });
    }

    /**
     * select ... for update
     */
    @Transactional
    public void likePessimisticLock2(Long articleId, Long userId) {
        ArticleLike articleLike = ArticleLike.create(
                snowflake.nextId(),
                articleId,
                userId
        );
        articleLikeJpaRepository.save(articleLike);

        ArticleLikeCount articleLikeCount = articleLikeCountJpaRepository.findLockedByArticleId(articleId)
                .orElseGet(() -> ArticleLikeCount.init(articleId, 0L));
        articleLikeCount.increase();
        articleLikeCountJpaRepository.save(articleLikeCount);
    }

    @Transactional
    public void unLikePessimisticLock2(Long articleId, Long userId) {
        articleLikeJpaRepository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(entity -> {
                    articleLikeJpaRepository.delete(entity);
                    ArticleLikeCount articleLikeCount = articleLikeCountJpaRepository.findLockedByArticleId(articleId).orElseThrow();
                    articleLikeCount.decrease();
                });
    }

    /**
     * optimistic
     */
    @Transactional
    public void likeOptimisticLock(Long articleId, Long userId) {
        ArticleLike articleLike = ArticleLike.create(
                snowflake.nextId(),
                articleId,
                userId
        );
        articleLikeJpaRepository.save(articleLike);

        ArticleLikeCount articleLikeCount = articleLikeCountJpaRepository.findById(articleId)
                .orElseGet(() -> ArticleLikeCount.init(articleId, 0L));
        articleLikeCount.increase();
        articleLikeCountJpaRepository.save(articleLikeCount);
    }

    @Transactional
    public void unLunLikeOptimisticLock(Long articleId, Long userId) {
        articleLikeJpaRepository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(entity -> {
                    articleLikeJpaRepository.delete(entity);
                    ArticleLikeCount articleLikeCount = articleLikeCountJpaRepository.findById(articleId).orElseThrow();
                    articleLikeCount.decrease();
                });
    }
}
