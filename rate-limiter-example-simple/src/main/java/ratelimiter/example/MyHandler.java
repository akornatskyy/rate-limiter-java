package ratelimiter.example;

import ratelimiter.RateLimit;
import ratelimiter.RateLimiter;
import ratelimiter.RateLimiterOptions;
import ratelimiter.inmemory.RateInMemoryLimiter;

import java.time.Duration;

public class MyHandler {
  private final RateLimiterOptions options = RateLimiterOptions.builder()
      .max(5)
      .duration(Duration.ofSeconds(10))
      .build();
  private final RateLimiter limiter = new RateInMemoryLimiter(options);

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
