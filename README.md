# rate-limiter-java

[![tests](https://github.com/akornatskyy/rate-limiter-java/actions/workflows/tests.yaml/badge.svg)](https://github.com/akornatskyy/rate-limiter-java/actions/workflows/tests.yaml)
[![maven central](https://img.shields.io/maven-central/v/io.github.akornatskyy/rate-limiter.svg)](https://search.maven.org/search?q=g:%22io.github.akornatskyy%22%20AND%20a:%22rate-limiter%22)

Limit the rate of incoming requests.

![rate-limit](misc/docs/rate-limit.png)

## Usage

Initialize the rate limit to 5K requests per hour.

```java
RateLimiterOptions options = RateLimiterOptions.builder()
    .max(5000)
    .duration(Duration.ofHours(1))
    .window(Duration.ofMinutes(1))
    .build();
```

Synchronous rate in memory limiter [example](./rate-limiter-example-simple/src/main/java/ratelimiter/example/MyHandler.java).

```java
RateLimiter limiter = new RateInMemoryLimiter(options);

// somewhere later in your code
RateLimit limit = limiter.addAndGet("key", 1);
if (!limit.hasRemaining()) {
  // not good
  return;
}

// all good
```

More examples:

- [in memory sync](./rate-limiter-example-simple/src/main/java/ratelimiter/example/MyHandler.java)
- [in memory async](./rate-limiter-example-simple/src/main/java/ratelimiter/example/MyAsyncHandler.java)
- [in memory webflux](./rate-limiter-example-webflux/src/main/java/ratelimiter/example/WelcomeController.java)
- [lettuce sync](./rate-limiter-lettuce-example-simple/src/main/java/ratelimiter/lettuce/example/MyHandler.java)
- [lettuce async](./rate-limiter-lettuce-example-simple/src/main/java/ratelimiter/lettuce/example/MyAsyncHandler.java)
- [lettuce webflux](./rate-limiter-lettuce-example-webflux/src/main/java/ratelimiter/lettuce/example/WelcomeController.java)

## Install

Add as a maven dependency:

```xml
<dependency>
  <groupId>com.github.akornatskyy</groupId>
  <artifactId>rate-limiter</artifactId>
  <version>1.1.4</version>
</dependency>
```

when used with [redis](https://redis.io/) [lettuce](https://github.com/lettuce-io/lettuce-core):

```xml
<dependency>
  <groupId>com.github.akornatskyy</groupId>
  <artifactId>rate-limiter-lettuce</artifactId>
  <version>1.1.7</version>
</dependency>
```

## Release

```sh
mvn versions:set -pl rate-limiter -DnewVersion=1.1.X
mvn -pl rate-limiter -P release clean deploy

mvn versions:set -pl rate-limiter-lettuce -DnewVersion=1.1.X
mvn -pl rate-limiter-lettuce -P release clean deploy
```