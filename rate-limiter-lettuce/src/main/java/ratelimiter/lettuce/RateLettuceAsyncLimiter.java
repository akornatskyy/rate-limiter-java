package ratelimiter.lettuce;

import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.async.RedisAsyncCommands;
import ratelimiter.RateAsyncLimiter;
import ratelimiter.RateLimit;
import ratelimiter.RateLimiterOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RateLettuceAsyncLimiter implements RateAsyncLimiter {
  private final RedisAsyncCommands<String, String> commands;
  private final long duration;
  private final long max;
  private final long window;
  private final Supplier<Long> timeSupplier;

  private volatile String script;

  public RateLettuceAsyncLimiter(
      RedisAsyncCommands<String, String> commands,
      RateLimiterOptions options) {
    this(commands, System::currentTimeMillis, options);
  }

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
