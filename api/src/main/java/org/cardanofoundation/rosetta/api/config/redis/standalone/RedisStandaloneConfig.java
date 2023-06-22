package org.cardanofoundation.rosetta.api.config.redis.standalone;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
//@Profile("standalone")
@Slf4j
public class RedisStandaloneConfig {

  @Value("${spring.redis.standalone.host}")
  private String hostname;

  @Value("${spring.redis.standalone.port}")
  private Integer port;

  @Value("${spring.redis.password}")
  private String password;

  @Bean(name = "lettuceConnectionFactory")
  LettuceConnectionFactory lettuceConnectionFactory() {
    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(
        hostname, port);
    redisStandaloneConfiguration.setPassword(password);
    log.info("`Connectiong to redis:` {}", hostname);
    return new LettuceConnectionFactory(redisStandaloneConfiguration);
  }
}