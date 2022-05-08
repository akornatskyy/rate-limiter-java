package ratelimiter;

import java.time.Duration;
import java.util.Objects;

/**
 * The type Rate limiter options.
 */
public final class RateLimiterOptions {
  private final Duration duration;
  private final long max;
  private final Duration window;

  private RateLimiterOptions(Duration duration, long max, Duration window) {
    this.duration = duration;
    this.max = max;
    this.window = window;
  }

  public Duration getDuration() {
    return duration;
  }

  public long getMax() {
    return max;
  }

  public Duration getWindow() {
    return window;
  }

  public static RateLimiterOptions.Builder builder() {
    return new Builder();
  }

  /**
   * The type Builder.
   */
  public static class Builder {
    private Duration duration;
    private long max;
    private Duration window = Duration.ofSeconds(1);

    /**
     * Limiter duration.
     *
     * @param duration the duration
     * @return the builder
     */
    public Builder duration(Duration duration) {
      Objects.requireNonNull(duration);
      this.duration = duration;
      return this;
    }

    /**
     * Max limit.
     *
     * @param max the max
     * @return the builder
     */
    public Builder max(long max) {
      this.max = max;
      return this;
    }

    /**
     * Window size.
     *
     * @param window the window
     * @return the builder
     */
    public Builder window(Duration window) {
      Objects.requireNonNull(duration);
      this.window = window;
      return this;
    }

    /**
     * Build rate limiter options.
     *
     * @return the rate limiter options
     */
    public RateLimiterOptions build() {
      if (duration == null) {
        throw new IllegalArgumentException("Duration has no default");
      }

      if (duration.toMillis() <= 0) {
        throw new IllegalArgumentException("Duration should be greater than 0");
      }

      if (max <= 0) {
        throw new IllegalArgumentException("Max should be greater than 0");
      }

      if (window.toMillis() <= 0) {
        throw new IllegalArgumentException("Window should be greater than 0");
      }

      return new RateLimiterOptions(duration, max, window);
    }
  }
}
