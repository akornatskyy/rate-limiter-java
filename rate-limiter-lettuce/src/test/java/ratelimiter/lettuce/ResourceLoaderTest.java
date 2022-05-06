package ratelimiter.lettuce;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ResourceLoaderTest {
  @Test
  void getScript() {
    String script = ResourceLoader.getScript();

    Assertions.assertNotNull(script);
  }
}