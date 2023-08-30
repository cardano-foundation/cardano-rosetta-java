package org.cardanofoundation.rosetta.consumer.configuration.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

@Configuration
public class RedisConfiguration {

  @Bean
  RedisTemplate<String, ?> redisTemplate(//NOSONAR
      final LettuceConnectionFactory lettuceConnectionFactory) {
    var redisTemplate = new RedisTemplate<String, Object>();
    redisTemplate.setConnectionFactory(lettuceConnectionFactory);
    redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    return redisTemplate;
  }

}
