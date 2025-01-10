package board.outboxmessagerelay;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * AssignedShard가 MessageRelayCoordinator에 의해서 만들어짐
 *  살아있는 애플리케이션들을 추적하고 관리
 *  Redis로 이제 그걸 Sorted Set으로 관리
 */
@Component
@RequiredArgsConstructor
public class MessageRelayCoordinator {

    private final StringRedisTemplate redisTemplate;

    @Value("${spring.application.name}")
    private String applicationName;

    private final String APP_ID = UUID.randomUUID().toString();

    // Ping을 3번 동안 쏘고, 3번 다 실패하면 이 애플리케이션이 죽었다고 판단
    private final int PING_INTERVAL_SECONDS = 3;
    private final int PING_FAILURE_THRESHOLD = 3;

    public AssignedShard assignShards(){
        return AssignedShard.of(APP_ID,findAppIds(),MessageRelayConstants.SHARD_COUNT);
    }

    private List<String> findAppIds() {
        return redisTemplate.opsForZSet().reverseRange(generateKey(), 0, -1).stream()
                .sorted()
                .toList();
    }

    @Scheduled(fixedDelay = PING_INTERVAL_SECONDS, timeUnit = TimeUnit.SECONDS)
    public void ping() {
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection conn = (StringRedisConnection) action;
            String key = generateKey();

            conn.zAdd(key, Instant.now().toEpochMilli(), APP_ID);
            conn.zRemRangeByScore(
                    key,
                    Double.NEGATIVE_INFINITY,
                    Instant.now().minusSeconds(PING_INTERVAL_SECONDS * PING_FAILURE_THRESHOLD).toEpochMilli()
            );
            return null;
        });
    }

    @PreDestroy
    public void leave(){
        redisTemplate.opsForZSet().remove(generateKey(),APP_ID);
    }

    /**
     * 각 마이크 서비스 별로 독립적인 애플리케이션 네임을 가지고 있는데 (board-article-service)
     * 그래서 이 모듈을 각 마이크로 서비스에 독립적으로 붙였을 때 이 코디네이터가 독립적인 키로
     * 동작하도록 해서 그렇게 동작하도록 하려고 이렇게 애플리케이션 네임을 키 파라미터로 줌
     */
    private String generateKey(){
        return "message-relay-coordinator:app-list:%s".formatted(applicationName);
    }
}
