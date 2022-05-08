package ratelimiter.inmemory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import ratelimiter.RateLimit;
import ratelimiter.RateLimiter;
import ratelimiter.RateLimiterOptions;

/**
 * The type Rate in memory limiter.
 */
public final class RateInMemoryLimiter implements RateLimiter {
  private final ConcurrentHashMap
      <String, ConcurrentMap<Long, AtomicInteger>> items
      = new ConcurrentHashMap<>();
  private final long duration;
  private final long max;
  private final long window;
  private final Supplier<Long> timeSupplier;

  /**
   * Instantiates a new Rate in memory limiter.
   *
   * @param options the options
   */
  public RateInMemoryLimiter(RateLimiterOptions options) {
    this(options, System::currentTimeMillis);
  }

  /**
   * Instantiates a new Rate in memory limiter.
   *
   * @param options      the options
   * @param timeSupplier the time supplier
   */
  public RateInMemoryLimiter(
      RateLimiterOptions options,
      Supplier<Long> timeSupplier) {
    long window = options.getWindow().toMillis();
    this.duration = options.getDuration().toMillis() / window;
    this.max = options.getMax();
    this.window = window;
    this.timeSupplier = timeSupplier;
  }

  @Override
  public RateLimit addAndGet(String key, int delta) {
    ConcurrentMap<Long, AtomicInteger> counters =
        items.computeIfAbsent(key, (k) -> new ConcurrentHashMap<>());

    long window = this.window;
    long now = this.timeSupplier.get() / window;
    counters.computeIfAbsent(now, (k) -> new AtomicInteger()).addAndGet(delta);

    long duration = this.duration;
    long start = now - duration;
    Iterator<Map.Entry<Long, AtomicInteger>> iterator =
        counters.entrySet().iterator();
    long oldest = now;
    long sum = -1;
    while (iterator.hasNext()) {
      Map.Entry<Long, AtomicInteger> entry = iterator.next();
      long ts = entry.getKey();
      if (ts < start) {
        iterator.remove();
      } else {
        if (ts < oldest) {
          oldest = ts;
        }

        sum += entry.getValue().get();
      }
    }

    return new RateLimit(this.max - sum, (oldest + duration) * window);
  }
}
