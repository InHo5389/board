package event.payload;

import event.EventPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDeletedEventPayload implements EventPayload {

    private Long commentId;
    private String content;
    private Long parentCommentId;
    private Long articleId;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private Long articleCommentCount;
}
