package board.article.service.request;

import lombok.Getter;
import lombok.ToString;

public class ArticleRequest {

    @Getter
    @ToString
    public static class Create{
        private String title;
        private String content;
        private Long writerId;
        private Long boardId;
    }

    @Getter
    @ToString
    public static class Update{
        private String title;
        private String content;
    }
}
