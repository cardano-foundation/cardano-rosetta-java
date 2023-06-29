package org.cardanofoundation.rosetta.api;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import redis.embedded.RedisServer;

public class IntegrationTestWithDbAndRedis extends IntegrationTestWithDB{
  private static RedisServer redisServer;

  static  {
    redisServer = RedisServer.builder().bind("localhost").port(16739).build();
    redisServer.start();
  }

  @PreDestroy
  public void destroy() {
    redisServer.stop();
  }


}
