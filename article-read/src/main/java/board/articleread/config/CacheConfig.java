package board.articleread.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.Map;

/**
 *  조회수는 만료시간이 짧은데 이게 너무 길면은 최신화가 안될 수 있음
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory)
                .withInitialCacheConfigurations(
                        Map.of(
                                "articleViewCount",
                                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(1))
                        )
                )
                .build();
    }
}
