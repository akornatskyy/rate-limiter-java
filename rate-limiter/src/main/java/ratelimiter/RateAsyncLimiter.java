package ratelimiter;

import java.util.concurrent.CompletableFuture;

/**
 * The interface Rate async limiter.
 */
public interface RateAsyncLimiter {
  CompletableFuture<RateLimit> addAndGet(String key, int delta);
}
