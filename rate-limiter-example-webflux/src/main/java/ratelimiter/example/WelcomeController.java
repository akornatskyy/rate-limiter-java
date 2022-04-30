package ratelimiter.example;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ratelimiter.RateAsyncLimiter;
import ratelimiter.RateLimit;
import ratelimiter.RateLimiterOptions;
import ratelimiter.inmemory.RateInMemoryAsyncLimiter;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@RestController
public class WelcomeController {
  private final RateLimiterOptions options = RateLimiterOptions.builder()
      .max(5)
      .duration(Duration.ofSeconds(10))
      .build();
  private final RateAsyncLimiter limiter = new RateInMemoryAsyncLimiter(options);

  @RequestMapping(
      method = RequestMethod.GET,
      value = "/",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<ResponseEntity<?>> get(ServerHttpRequest request) {
    String key = request.getRemoteAddress().getHostString();
    return limiter.addAndGet(key, 1)
        .thenCompose(limit -> {
          if (!limit.hasRemaining()) {
            return tooManyRequests(limit);
          }

          return this.process()
              .thenApply(r -> ResponseEntity.ok()
                  .headers(headers(limit))
                  .body(r));
        });
  }

  private CompletableFuture<ResponseEntity<?>> tooManyRequests(
      RateLimit limit) {
    MessageResponse response = new MessageResponse();
    response.message = "API rate limit exceeded. " +
                       "Retry your request again later, please.";
    return CompletableFuture.completedFuture(
        ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .headers(headers(limit))
            .body(response));
  }

  private HttpHeaders headers(RateLimit limit) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-RateLimit-Limit",
                String.valueOf(options.getMax()));
    headers.add("X-RateLimit-Remaining",
                String.valueOf(limit.getRemaining()));
    headers.add("X-RateLimit-Reset",
                String.valueOf(limit.getReset() / 1000));
    return headers;
  }

  private CompletableFuture<MessageResponse> process() {
    MessageResponse response = new MessageResponse();
    response.message = "OK";
    return CompletableFuture.completedFuture(response);
  }

  static class MessageResponse {
    public String message;
  }
}
