package ratelimiter.lettuce;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RedisCommands;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ratelimiter.RateLimit;
import ratelimiter.RateLimiter;
import ratelimiter.RateLimiterOptions;

public class RateLettuceLimiterTest {
  private static final String KEY = "rate-lettuce-limiter-test";

  private final RateLimiterOptions options = RateLimiterOptions.builder()
      .duration(Duration.ofMinutes(5))
      .max(100)
      .window(Duration.ofSeconds(1))
      .build();
  private final RedisCommands<String, String> commands = RedisClient.create()
      .connect(RedisURI.create(
          Optional.ofNullable(System.getenv("REDIS_URI"))
              .orElse("redis://127.0.0.1")))
      .sync();

  @BeforeEach
  void setUp() {
    commands.del(KEY);
  }

  @Test
  void addAndGetWithDelta() {
    RateLimiter limiter = new RateLettuceLimiter(commands, options);

    RateLimit limit = limiter.addAndGet(KEY, 5);

    Assertions.assertTrue(limit.hasRemaining());
    Assertions.assertEquals(95, limit.getRemaining());
  }

  @Test
  void addAndGetWithDeltaExceeded() {
    RateLimiter limiter = new RateLettuceLimiter(commands, options);

    RateLimit limit = limiter.addAndGet(KEY, 105);

    Assertions.assertFalse(limit.hasRemaining());
    Assertions.assertEquals(0, limit.getRemaining());
  }

  @Test
  void getRemainingIsPositive() {
    RateLimiter limiter = new RateLettuceLimiter(commands, options);

    RateLimit limit = limiter.addAndGet(KEY, 1);

    Assertions.assertEquals(99, limit.getRemaining());
  }

  @Test
  void getRemainingReturnsZero() {
    RateLimiter limiter = new RateLettuceLimiter(commands, options);

    for (int i = 1; i <= 100; i++) {
      RateLimit limit = limiter.addAndGet(KEY, 1);

      Assertions.assertTrue(limit.hasRemaining());
      Assertions.assertEquals(options.getMax() - i, limit.getRemaining());
    }

    for (int i = 0; i < 10; i++) {
      RateLimit limit = limiter.addAndGet(KEY, 1);

      Assertions.assertFalse(limit.hasRemaining());
      Assertions.assertEquals(0, limit.getRemaining());
    }
  }

  @Test
  void getReset() {
    RateLimiter limiter = new RateLettuceLimiter(
        commands,
        options,
        () -> Instant.parse("2022-04-25T12:40:15.392Z").toEpochMilli());

    RateLimit limit = limiter.addAndGet(KEY, 1);

    Assertions.assertEquals(
        Instant.parse("2022-04-25T12:45:15Z"),
        Instant.ofEpochMilli(limit.getReset()));
    Assertions.assertEquals(99, limit.getRemaining());
  }

  @Test
  void getResetSameWindow() {
    Iterator<Long> timestamps = Arrays.asList(
        Instant.parse("2022-04-25T12:40:15.392Z").toEpochMilli(),
        Instant.parse("2022-04-25T12:40:15.491Z").toEpochMilli()
    ).iterator();
    RateLimiter limiter = new RateLettuceLimiter(
        commands,
        options,
        timestamps::next);

    limiter.addAndGet(KEY, 1);
    RateLimit limit = limiter.addAndGet(KEY, 1);

    Assertions.assertEquals(
        Instant.parse("2022-04-25T12:45:15Z"),
        Instant.ofEpochMilli(limit.getReset()));
    Assertions.assertEquals(98, limit.getRemaining());
  }

  @Test
  void getResetNextWindow() {
    Iterator<Long> timestamps = Arrays.asList(
        Instant.parse("2022-04-25T12:40:15.392Z").toEpochMilli(),
        Instant.parse("2022-04-25T12:40:16.540Z").toEpochMilli()
    ).iterator();
    RateLimiter limiter = new RateLettuceLimiter(
        commands,
        options,
        timestamps::next);

    limiter.addAndGet(KEY, 1);
    RateLimit limit = limiter.addAndGet(KEY, 1);

    Assertions.assertEquals(
        Instant.parse("2022-04-25T12:45:15Z"),
        Instant.ofEpochMilli(limit.getReset()));
    Assertions.assertEquals(98, limit.getRemaining());
  }

  @Test
  void getResetRemoveWindow() {
    Iterator<Long> timestamps = Arrays.asList(
        Instant.parse("2022-04-25T12:40:15.392Z").toEpochMilli(),
        Instant.parse("2022-04-25T12:46:05.126Z").toEpochMilli()
    ).iterator();
    RateLimiter limiter = new RateLettuceLimiter(
        commands,
        options,
        timestamps::next);

    limiter.addAndGet(KEY, 1);
    RateLimit limit = limiter.addAndGet(KEY, 1);

    Assertions.assertEquals(
        Instant.parse("2022-04-25T12:51:05Z"),
        Instant.ofEpochMilli(limit.getReset()));
    Assertions.assertEquals(99, limit.getRemaining());
  }
}