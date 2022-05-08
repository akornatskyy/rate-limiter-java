package ratelimiter.lettuce;

import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.async.RedisAsyncCommands;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import ratelimiter.RateAsyncLimiter;
import ratelimiter.RateLimit;
import ratelimiter.RateLimiterOptions;

/**
 * The type Rate lettuce async limiter.
 */
public class RateLettuceAsyncLimiter implements RateAsyncLimiter {
  private final RedisAsyncCommands<String, String> commands;
  private final long duration;
  private final long max;
  private final long window;
  private final Supplier<Long> timeSupplier;

  private volatile String script;

  /**
   * Instantiates a new Rate lettuce async limiter.
   *
   * @param commands the commands
   * @param options  the options
   */
  public RateLettuceAsyncLimiter(
      RedisAsyncCommands<String, String> commands,
      RateLimiterOptions options) {
    this(commands, System::currentTimeMillis, options);
  }

  /**
   * Instantiates a new Rate lettuce async limiter.
   *
   * @param commands     the commands
   * @param timeSupplier the time supplier
   * @param options      the options
   */
  public RateLettuceAsyncLimiter(
      RedisAsyncCommands<String, String> commands,
      Supplier<Long> timeSupplier,
      RateLimiterOptions options) {
    this.commands = commands;
    long window = options.getWindow().toMillis();
    this.duration = options.getDuration().toMillis() / window;
    this.max = options.getMax();
    this.window = window;
    this.timeSupplier = timeSupplier;
  }

  @Override
  public CompletableFuture<RateLimit> addAndGet(String key, int delta) {
    long window = this.window;
    long now = this.timeSupplier.get() / window;
    long duration = this.duration;
    long start = now - duration;
    return commands.<List<Long>>evalsha(
            ensureScriptLoaded(),
            ScriptOutputType.MULTI,
            new String[] {key},
            String.valueOf(delta),
            String.valueOf(start),
            String.valueOf(now),
            String.valueOf(duration * window))
        .thenApply((result) -> {
          long sum = result.get(0);
          long oldest = result.get(1);

          return new RateLimit(this.max - sum, (oldest + duration) * window);
        })
        .toCompletableFuture();

  }

  private String ensureScriptLoaded() {
    if (script == null) {
      synchronized (RateLettuceAsyncLimiter.class) {
        if (script == null) {
          script = commands.scriptLoad(ResourceLoader.getScript())
              .toCompletableFuture()
              .join();
        }
      }
    }

    return script;
  }
}
