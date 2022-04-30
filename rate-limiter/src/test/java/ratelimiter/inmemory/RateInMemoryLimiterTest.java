package ratelimiter.inmemory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ratelimiter.RateLimit;
import ratelimiter.RateLimiter;
import ratelimiter.RateLimiterOptions;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;

public class RateInMemoryLimiterTest {
  private static final String KEY = "key";

  private final RateLimiterOptions options = RateLimiterOptions.builder()
      .duration(Duration.ofMinutes(5))
      .max(100)
      .window(Duration.ofSeconds(1))
      .build();

  @Test
  void addAndGetWithDelta() {
    RateLimiter limiter = new RateInMemoryLimiter(options);

    RateLimit limit = limiter.addAndGet(KEY, 5);

    Assertions.assertTrue(limit.hasRemaining());
    Assertions.assertEquals(95, limit.getRemaining());
  }

  @Test
  void addAndGetWithDeltaExceeded() {
    RateLimiter limiter = new RateInMemoryLimiter(options);

    RateLimit limit = limiter.addAndGet(KEY, 105);

    Assertions.assertFalse(limit.hasRemaining());
    Assertions.assertEquals(0, limit.getRemaining());
  }

  @Test
  void getRemainingIsPositive() {
    RateLimiter limiter = new RateInMemoryLimiter(options);

    RateLimit limit = limiter.addAndGet(KEY, 1);

    Assertions.assertEquals(99, limit.getRemaining());
  }

  @Test
  void getRemainingReturnsZero() {
    RateLimiter limiter = new RateInMemoryLimiter(options);

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
    RateLimiter limiter = new RateInMemoryLimiter(
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
    RateLimiter limiter = new RateInMemoryLimiter(options, timestamps::next);

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
    RateLimiter limiter = new RateInMemoryLimiter(options, timestamps::next);

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
    RateLimiter limiter = new RateInMemoryLimiter(options, timestamps::next);

    limiter.addAndGet(KEY, 1);
    RateLimit limit = limiter.addAndGet(KEY, 1);

    Assertions.assertEquals(
        Instant.parse("2022-04-25T12:51:05Z"),
        Instant.ofEpochMilli(limit.getReset()));
    Assertions.assertEquals(99, limit.getRemaining());
  }
}