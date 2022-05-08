package ratelimiter;

/**
 * The interface Rate limiter.
 */
public interface RateLimiter {
  RateLimit addAndGet(String key, int delta);
}
