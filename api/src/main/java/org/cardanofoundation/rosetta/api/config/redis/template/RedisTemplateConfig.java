package org.cardanofoundation.rosetta.api.config.redis.template;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;

@Configuration
public class RedisTemplateConfig {

  /**
   * RedisTemplate configuration
   *
   * @return redisTemplate
   */
  @Bean
  @Autowired
  @Primary
  RedisTemplate<String, ?> redisTemplate(//NOSONAR
      final LettuceConnectionFactory lettuceConnectionFactory) {
    var redisTemplate = new RedisTemplate<String, Object>();
    redisTemplate.setConnectionFactory(lettuceConnectionFactory);
    redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    redisTemplate.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
    redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

    return redisTemplate;
  }


  /**
   * RedisTemplate for older version configuration
   *
   * @return redisTemplate
   */
  @Bean
  @Autowired
  @Primary
  RedisTemplate<String, String> redisTemplateString(
      final LettuceConnectionFactory lettuceConnectionFactory) {
    var redisTemplate = new RedisTemplate<String, String>();
    redisTemplate.setConnectionFactory(lettuceConnectionFactory);
    redisTemplate.setValueSerializer(new GenericToStringSerializer<>(Object.class));
    return redisTemplate;
  }

  /**
   * Config bean hashOperations
   *
   * @param redisTemplate bean
   * @param <HK>          hash key type
   * @param <V>           value type
   * @return bean hashOperations
   */
  @Bean
  <HK, V> HashOperations<String, HK, V> hashOperations(
      final RedisTemplate<String, V> redisTemplate) { //NOSONAR
    return redisTemplate.opsForHash();
  }

  /**
   * ListOperations bean configuration
   *
   * @param redisTemplate inject bean
   * @param <V>           value type
   * @return listOperations
   */
  @Bean
  <V> ListOperations<String, V> listOperations(final RedisTemplate<String, V> redisTemplate) {
    return redisTemplate.opsForList();
  }

  /**
   * ZSetOperations configuration
   *
   * @param redisTemplate inject bean
   * @param <V>           value type
   * @return ZSetOperations<String, V>
   */
  @Bean
  <V> ZSetOperations<String, V> zSetOperations(final RedisTemplate<String, V> redisTemplate) {
    return redisTemplate.opsForZSet();
  }

  /**
   * SetOperations configuration
   *
   * @param redisTemplate inject bean
   * @param <V>           value type
   * @return SetOperations<String, V>
   */
  @Bean
  <V> SetOperations<String, V> setOperations(final RedisTemplate<String, V> redisTemplate) {
    return redisTemplate.opsForSet();
  }

  /**
   * ValueOperations configuration
   *
   * @param redisTemplate inject bean
   * @param <V>           value type
   * @return ValueOperations<String, V>
   */
  @Bean
  <V> ValueOperations<String, V> valueOperations(final RedisTemplate<String, V> redisTemplate) {
    return redisTemplate.opsForValue();
  }
}
