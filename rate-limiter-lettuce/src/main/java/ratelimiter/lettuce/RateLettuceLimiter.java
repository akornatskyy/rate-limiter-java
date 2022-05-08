package ratelimiter.lettuce;

import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.sync.RedisCommands;
import java.util.List;
import java.util.function.Supplier;
import ratelimiter.RateLimit;
import ratelimiter.RateLimiter;
import ratelimiter.RateLimiterOptions;

/**
 * The type Rate lettuce limiter.
 */
public class RateLettuceLimiter implements RateLimiter {
  private final RedisCommands<String, String> commands;
  private final long duration;
  private final long max;
  private final long window;
  private final Supplier<Long> timeSupplier;

  private volatile String script;

  /**
   * Instantiates a new Rate lettuce limiter.
   *
   * @param commands the commands
   * @param options  the options
   */
  public RateLettuceLimiter(
      RedisCommands<String, String> commands,
      RateLimiterOptions options) {
    this(commands, options, System::currentTimeMillis);
  }

  /**
   * Instantiates a new Rate lettuce limiter.
   *
   * @param commands     the commands
   * @param options      the options
   * @param timeSupplier the time supplier
   */
  public RateLettuceLimiter(
      RedisCommands<String, String> commands,
      RateLimiterOptions options,
      Supplier<Long> timeSupplier) {
    this.commands = commands;
    long window = options.getWindow().toMillis();
    this.duration = options.getDuration().toMillis() / window;
    this.max = options.getMax();
    this.window = window;
    this.timeSupplier = timeSupplier;
  }

  @Override
  public RateLimit addAndGet(String key, int delta) {
    long window = this.window;
    long now = this.timeSupplier.get() / window;
    long duration = this.duration;
    long start = now - duration;

    List<Long> result = commands.evalsha(
        ensureScriptLoaded(),
        ScriptOutputType.MULTI,
        new String[] {key},
        String.valueOf(delta),
        String.valueOf(start),
        String.valueOf(now),
        String.valueOf(duration * window));
    long sum = result.get(0);
    long oldest = result.get(1);

    return new RateLimit(this.max - sum, (oldest + duration) * window);
  }

  private String ensureScriptLoaded() {
    if (script == null) {
      synchronized (RateLettuceLimiter.class) {
        if (script == null) {
          script = commands.scriptLoad(ResourceLoader.getScript());
        }
      }
    }

    return script;
  }
}
