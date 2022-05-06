package ratelimiter.lettuce;

import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.sync.RedisCommands;
import ratelimiter.RateLimit;
import ratelimiter.RateLimiter;
import ratelimiter.RateLimiterOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RateLettuceLimiter implements RateLimiter {
  private final RedisCommands<String, String> commands;
  private final long duration;
  private final long max;
  private final long window;
  private final Supplier<Long> timeSupplier;

  private volatile String script;

  public RateLettuceLimiter(
      RedisCommands<String, String> commands,
      RateLimiterOptions options) {
    this(commands, options, System::currentTimeMillis);
  }

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
