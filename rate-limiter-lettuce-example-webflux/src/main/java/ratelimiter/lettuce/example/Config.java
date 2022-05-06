package ratelimiter.lettuce.example;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
class Config {
  @Bean(destroyMethod = "shutdown")
  RedisClient redisClient() {
    return RedisClient.create(RedisURI.create(
        Optional.ofNullable(System.getenv("REDIS_URI"))
            .orElse("redis://127.0.0.1")));
  }

  @Bean(destroyMethod = "close")
  StatefulRedisConnection<String, String> connection(RedisClient redisClient) {
    return redisClient.connect();
  }
}
