package board.hotarticle.utils;

import java.time.Duration;
import java.time.LocalDateTime;

import static java.time.LocalTime.MIDNIGHT;

public class TimeCalculatorUtils {

    // 자정까지 얼마나 남았는지 반환
    public static Duration calculateDurationToMidnight() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println(now);
        LocalDateTime midnight = now.plusDays(1).with(MIDNIGHT);
        System.out.println(midnight);
        return Duration.between(now,midnight);
    }
}
