package ratelimiter.lettuce;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResourceLoaderTest {
  @Test
  void getScript() {
    String script = ResourceLoader.getScript();

    Assertions.assertNotNull(script);
  }
}