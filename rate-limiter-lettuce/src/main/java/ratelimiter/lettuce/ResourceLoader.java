package ratelimiter.lettuce;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

class ResourceLoader {
  private ResourceLoader() {
  }

  public static String getScript() {
    try (InputStream is = RateLettuceAsyncLimiter.class.getResourceAsStream(
        "/rate-limiter/lettuce/addAndGet.lua")) {
      return new BufferedReader(new InputStreamReader(is))
          .lines()
          .collect(Collectors.joining("\n"));
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }
}
