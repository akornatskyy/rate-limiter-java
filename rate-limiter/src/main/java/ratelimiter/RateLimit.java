package ratelimiter;

/**
 * The type Rate limit.
 */
public final class RateLimit {
  private final long remaining;
  private final long reset;

  public RateLimit(long remaining, long reset) {
    this.remaining = remaining;
    this.reset = reset;
  }

  public boolean hasRemaining() {
    return remaining > 0;
  }

  public long getRemaining() {
    return remaining > 0 ? remaining - 1 : 0;
  }

  public long getReset() {
    return reset;
  }
}
