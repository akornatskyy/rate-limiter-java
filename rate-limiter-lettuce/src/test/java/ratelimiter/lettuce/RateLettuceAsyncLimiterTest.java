package ratelimiter.lettuce;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.async.RedisAsyncCommands;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ratelimiter.RateAsyncLimiter;
import ratelimiter.RateLimit;
import ratelimiter.RateLimiterOptions;

public class RateLettuceAsyncLimiterTest {
  private static final String KEY = "rate-lettuce-async-limiter-test";

  private final RateLimiterOptions options = RateLimiterOptions.builder()
      .duration(Duration.ofMinutes(5))
      .max(100)
      .window(Duration.ofSeconds(1))
      .build();
  private final RedisAsyncCommands<String, String> commands =
      RedisClient.create()
          .connect(RedisURI.create(
              Optional.ofNullable(System.getenv("REDIS_URI"))
                  .orElse("redis://127.0.0.1")))
          .async();

  @BeforeEach
  void setUp() {
    commands.del(KEY);
  }

  @Test
  void addAndGet() throws ExecutionException, InterruptedException {
    RateAsyncLimiter limiter = new RateLettuceAsyncLimiter(
        commands,
        options);

    RateLimit limit = limiter.addAndGet(KEY, 2).get();

    Assertions.assertTrue(limit.hasRemaining());
    Assertions.assertEquals(98, limit.getRemaining());
  }
}