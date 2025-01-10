package board.hotarticle.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 인기 글 선정할 때 그 하루 동안 그 당일에만 인기글을 선정하면 되니까
 * calculateDurationToMidnight()로 ttl을 검
 */
class TimeCalculatorUtilsTest {

    @Test
    @DisplayName("")
    void calculateDurationToMidnight() {
        //given
        Duration duration = TimeCalculatorUtils.calculateDurationToMidnight();
        System.out.println("duration.getSeconds / 60 = " + duration.getSeconds() / 60);
        //when
        //then
    }
}