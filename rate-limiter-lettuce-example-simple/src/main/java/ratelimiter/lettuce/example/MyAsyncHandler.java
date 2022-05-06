package ratelimiter.lettuce.example;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import ratelimiter.RateAsyncLimiter;
import ratelimiter.RateLimiterOptions;
import ratelimiter.lettuce.RateLettuceAsyncLimiter;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MyAsyncHandler {
  private final RateLimiterOptions options = RateLimiterOptions.builder()
      .max(5)
      .duration(Duration.ofSeconds(10))
      .build();
  private final RateAsyncLimiter limiter = new RateLettuceAsyncLimiter(
      RedisClient.create()
          .connect(RedisURI.create(
              Optional.ofNullable(System.getenv("REDIS_URI"))
                  .orElse("redis://127.0.0.1")))
          .async(),
      options);

  public CompletableFuture<Boolean> operation() {
    return limiter.addAndGet("key", 1).thenApply(limit -> {
      if (!limit.hasRemaining()) {
        return false;
      }

      // all good
      return true;
    });
  }

  public static void main(String[] args)
      throws ExecutionException, InterruptedException {
    MyAsyncHandler handler = new MyAsyncHandler();
    for (int i = 1; i <= 7; i++) {
      boolean result = handler.operation().get();
      System.out.printf("%d: %s%n", i, result ? "OK" : "FAIL");
    }
  }
}
