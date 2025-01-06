package board.view.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleViewCount {

    @Id
    private Long articleId;
    private Long viewCount;

    public static ArticleViewCount init(Long articleId, Long viewCount) {
        ArticleViewCount articleViewCount = new ArticleViewCount();
        articleViewCount.articleId = articleId;
        articleViewCount.viewCount = viewCount;
        return articleViewCount;
    }
}
