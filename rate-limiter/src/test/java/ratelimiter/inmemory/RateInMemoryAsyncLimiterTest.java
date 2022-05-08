package ratelimiter.inmemory;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ratelimiter.RateAsyncLimiter;
import ratelimiter.RateLimit;
import ratelimiter.RateLimiterOptions;

public class RateInMemoryAsyncLimiterTest {
  private static final String KEY = "key";

  private final RateLimiterOptions options = RateLimiterOptions.builder()
      .duration(Duration.ofMinutes(5))
      .max(100)
      .window(Duration.ofSeconds(1))
      .build();

  @Test
  public void addAndGet() throws ExecutionException, InterruptedException {
    RateAsyncLimiter limiter = new RateInMemoryAsyncLimiter(options);

    RateLimit limit = limiter.addAndGet(KEY, 2).get();

    Assertions.assertTrue(limit.hasRemaining());
    Assertions.assertEquals(98, limit.getRemaining());
  }
}