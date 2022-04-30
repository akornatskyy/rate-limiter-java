package ratelimiter;

public interface RateLimiter {
  RateLimit addAndGet(String key, int delta);
}
