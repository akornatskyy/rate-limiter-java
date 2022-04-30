package ratelimiter;

import java.util.concurrent.CompletableFuture;

public interface RateAsyncLimiter {
  CompletableFuture<RateLimit> addAndGet(String key, int delta);
}
