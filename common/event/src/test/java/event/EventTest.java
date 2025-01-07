package event;

import event.payload.ArticleCreatedEventPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EventTest {
    @Test
    @DisplayName("직렬화 역직렬화 테스트")
    void test() {
        //given
        ArticleCreatedEventPayload payload = ArticleCreatedEventPayload.builder()
                .articleId(1L)
                .title("title")
                .content("content")
                .boardId(1L)
                .writerId(1L)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .boardArticleCount(23L)
                .build();

        long eventId = 1234L;
        Event<EventPayload> event = Event.of(
                eventId,
                EventType.ARTICLE_CREATED,
                payload
        );
        String json = event.toJson();
        System.out.println("json = " + json);
        //when
        Event<EventPayload> result = Event.fromJson(json);
        //then
        assertThat(result.getEventId()).isEqualTo(eventId);
        assertThat(result.getType()).isEqualTo(event.getType());
        assertThat(result.getPayload()).isInstanceOf(payload.getClass());

        ArticleCreatedEventPayload resultPayload = (ArticleCreatedEventPayload) result.getPayload();
        assertThat(resultPayload.getArticleId()).isEqualTo(payload.getArticleId());
        assertThat(resultPayload.getTitle()).isEqualTo(payload.getTitle());
    }

}