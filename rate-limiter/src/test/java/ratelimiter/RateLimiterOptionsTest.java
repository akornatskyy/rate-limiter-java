package ratelimiter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class RateLimiterOptionsTest {
  @Test
  public void builder() {
    RateLimiterOptions options = RateLimiterOptions.builder()
        .duration(Duration.ofMinutes(5))
        .max(100)
        .window(Duration.ofSeconds(1))
        .build();

    Assertions.assertEquals(Duration.ofMinutes(5), options.getDuration());
    Assertions.assertEquals(100, options.getMax());
    Assertions.assertEquals(Duration.ofSeconds(1), options.getWindow());
  }

  @Test
  public void buildThrowsIllegalArgumentException() {
    Assertions.assertEquals(
        "Duration has no default",
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> RateLimiterOptions.builder().build())
            .getMessage());

    Assertions.assertEquals(
        "Duration should be greater than 0",
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> RateLimiterOptions.builder()
                    .duration(Duration.ZERO)
                    .build())
            .getMessage());

    Assertions.assertEquals(
        "Max should be greater than 0",
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> RateLimiterOptions.builder()
                    .duration(Duration.ofHours(1))
                    .build())
            .getMessage());

    Assertions.assertEquals(
        "Window should be greater than 0",
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> RateLimiterOptions.builder()
                    .duration(Duration.ofHours(1))
                    .max(100)
                    .window(Duration.ofSeconds(0))
                    .build())
            .getMessage());
  }
}