package ratelimiter.lettuce.example;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import ratelimiter.RateLimit;
import ratelimiter.RateLimiter;
import ratelimiter.RateLimiterOptions;
import ratelimiter.lettuce.RateLettuceLimiter;

import java.time.Duration;
import java.util.Optional;

public class MyHandler {
  private final RateLimiterOptions options = RateLimiterOptions.builder()
      .max(5)
      .duration(Duration.ofSeconds(10))
      .build();
  private final RateLimiter limiter = new RateLettuceLimiter(
      RedisClient.create()
          .connect(RedisURI.create(
              Optional.ofNullable(System.getenv("REDIS_URI"))
                  .orElse("redis://127.0.0.1")))
          .sync(),
      options);

  public boolean operation() {
    RateLimit limit = limiter.addAndGet("key", 1);
    if (!limit.hasRemaining()) {
      return false;
    }

    // all good
    return true;
  }

  public static void main(String[] args) {
    MyHandler handler = new MyHandler();
    for (int i = 1; i <= 7; i++) {
      boolean result = handler.operation();
      System.out.printf("%d: %s%n", i, result ? "OK" : "FAIL");
    }
  }
}
