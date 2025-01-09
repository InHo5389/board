package board.hotarticle.service;

import board.hotarticle.repository.ArticleCommentCountRepository;
import board.hotarticle.repository.ArticleLikeCountRepository;
import board.hotarticle.repository.ArticleViewCountRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class HotArticleScoreCalculatorTest {

    @InjectMocks
    private HotArticleScoreCalculator hotArticleScoreCalculator;

    @Mock
    private ArticleLikeCountRepository articleLikeCountRepository;

    @Mock
    private ArticleViewCountRepository articleViewCountRepository;

    @Mock
    private ArticleCommentCountRepository articleCommentCountRepository;

    @Test
    void calculateTest() {
        //given
        Long articleId = 1L;
        long likeCount = RandomGenerator.getDefault().nextLong();
        long commentCount = RandomGenerator.getDefault().nextLong();
        long viewCount = RandomGenerator.getDefault().nextLong();

        given(articleLikeCountRepository.read(articleId)).willReturn(likeCount);
        given(articleViewCountRepository.read(articleId)).willReturn(viewCount);
        given(articleCommentCountRepository.read(articleId)).willReturn(commentCount);
        //when
        long score = hotArticleScoreCalculator.calculate(articleId);
        //then
        Assertions.assertThat(score).isEqualTo(3 * likeCount + 2 * commentCount + 1 * viewCount);
    }

}