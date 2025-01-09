package board.hotarticle.service;

import board.hotarticle.repository.ArticleCreatedTimeRepository;
import board.hotarticle.repository.HotArticleListRepository;
import board.hotarticle.service.eventhandler.EventHandler;
import event.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class HotArticleScoreUpdaterTest {

    @InjectMocks
    private HotArticleScoreUpdater hotArticleScoreUpdater;

    @Mock
    private HotArticleListRepository hotArticleListRepository;
    @Mock
    private HotArticleScoreCalculator hotArticleScoreCalculator;
    @Mock
    private ArticleCreatedTimeRepository articleCreatedTimeRepository;

    @Test
    @DisplayName("만약 게시글이 오늘 생성되지 않았다면 ")
    void test() {
        //given
        Long articleId = 1L;
        Event event = mock(Event.class);
        EventHandler eventHandler = mock(EventHandler.class);

        given(eventHandler.findArticleId(event)).willReturn(articleId);

        LocalDateTime createdTime = LocalDateTime.now().minusDays(1);
        given(articleCreatedTimeRepository.read(articleId)).willReturn(createdTime);
        //when
        hotArticleScoreUpdater.update(event, eventHandler);
        //then
        verify(eventHandler, never()).handle(event);
        verify(hotArticleListRepository, never())
                .add(anyLong(), any(LocalDateTime.class),anyLong(),anyLong(),any(Duration.class));
    }

    @Test
    @DisplayName("만약 게시글이 오늘 작성했다면 ")
    void test1() {
        //given
        Long articleId = 1L;
        Event event = mock(Event.class);
        EventHandler eventHandler = mock(EventHandler.class);

        given(eventHandler.findArticleId(event)).willReturn(articleId);

        LocalDateTime createdTime = LocalDateTime.now();
        given(articleCreatedTimeRepository.read(articleId)).willReturn(createdTime);
        //when
        hotArticleScoreUpdater.update(event, eventHandler);
        //then
        verify(eventHandler).handle(event);
        verify(hotArticleListRepository)
                .add(anyLong(), any(LocalDateTime.class),anyLong(),anyLong(),any(Duration.class));
    }

}