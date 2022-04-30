package ratelimiter.example;

import ratelimiter.RateAsyncLimiter;
import ratelimiter.RateLimiterOptions;
import ratelimiter.inmemory.RateInMemoryAsyncLimiter;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MyAsyncHandler {
  private final RateLimiterOptions options = RateLimiterOptions.builder()
      .max(5)
      .duration(Duration.ofSeconds(10))
      .build();
  private final RateAsyncLimiter limiter = new RateInMemoryAsyncLimiter(options);

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
      Boolean result = handler.operation().get();
      System.out.printf("%d: %s%n", i, result ? "OK" : "FAIL");
    }
  }
}
