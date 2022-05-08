package ratelimiter.inmemory;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import ratelimiter.RateAsyncLimiter;
import ratelimiter.RateLimit;
import ratelimiter.RateLimiter;
import ratelimiter.RateLimiterOptions;

/**
 * The type Rate in memory async limiter.
 */
public final class RateInMemoryAsyncLimiter implements RateAsyncLimiter {
  private final RateLimiter inner;

  public RateInMemoryAsyncLimiter(RateLimiterOptions options) {
    this(options, System::currentTimeMillis);
  }

  public RateInMemoryAsyncLimiter(
      RateLimiterOptions options,
      Supplier<Long> timeSupplier) {
    this.inner = new RateInMemoryLimiter(options, timeSupplier);
  }

  @Override
  public CompletableFuture<RateLimit> addAndGet(String key, int delta) {
    return CompletableFuture.completedFuture(this.inner.addAndGet(key, delta));
  }
}
