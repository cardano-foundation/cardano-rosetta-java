package org.cardanofoundation.rosetta.api.config.redis.sentinel;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableCaching
@Profile("sentinel")
public class RedisSentinelConfiguration extends CachingConfigurerSupport {

    /**
     * Redis properties config
     */
    RedisSentinelProperties redisProperties;

    @Autowired
    RedisSentinelConfiguration(RedisSentinelProperties redisProperties) {
        this.redisProperties = redisProperties;
    }


    @Bean
    @Primary
    JedisPoolConfig poolConfig() {
        var jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setTestOnBorrow(redisProperties.getTestOnBorrow());
        jedisPoolConfig.setMaxTotal(redisProperties.getMaxTotal());
        jedisPoolConfig.setMaxIdle(redisProperties.getMaxIdle());
        jedisPoolConfig.setMinIdle(redisProperties.getMinIdle());
        jedisPoolConfig.setTestOnReturn(redisProperties.getTestOnReturn());
        jedisPoolConfig.setTestWhileIdle(redisProperties.getTestWhileIdle());
        return jedisPoolConfig;
    }


    @Bean
    @Primary
    org.springframework.data.redis.connection.RedisSentinelConfiguration sentinelConfig() {
        var sentinelConfig = new org.springframework.data.redis.connection.RedisSentinelConfiguration();

        sentinelConfig.master(redisProperties.getMaster());
        sentinelConfig.setSentinelPassword(RedisPassword.of(redisProperties.getPassword()));
        sentinelConfig.setDatabase(redisProperties.getDatabaseIndex());
        var sentinels = redisProperties.getSentinels()
                .stream()
                .map(getSentinelNodeRedisNodeFunction())
                .collect(Collectors.toSet());

        sentinelConfig.setSentinels(sentinels);
        return sentinelConfig;
    }

    private static Function<RedisSentinelProperties.SentinelNode, RedisNode> getSentinelNodeRedisNodeFunction() {
        return sentinel -> new RedisNode(sentinel.getHost(), sentinel.getPort());
    }

    /**
     * jedis connection factory configuration
     *
     * @return JedisConnectionFactory
     */
    @Bean(name = "jedisConnectionFactory")
    @Autowired
    JedisConnectionFactory jedisConnectionFactory(
            org.springframework.data.redis.connection.RedisSentinelConfiguration sentinelConfig) {

        return new JedisConnectionFactory(sentinelConfig, poolConfig());
    }

    /**
     * Lettuce connection factory configuration
     *
     * @return LettuceConnectionFactory
     */
    @Bean(name = "lettuceConnectionFactory")
    @Autowired
    LettuceConnectionFactory lettuceConnectionFactory(
            org.springframework.data.redis.connection.RedisSentinelConfiguration sentinelConfig) {
        LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration.builder()
                .clientName(UUID.randomUUID().toString())
                .commandTimeout(Duration.ofMillis(redisProperties.getConnectTimeOut()))
                .build();

        return new LettuceConnectionFactory(sentinelConfig, clientConfiguration);
    }

    /**
     * RedisTemplate configuration
     *
     * @return redisTemplate
     */
    @Bean
    @Autowired
    RedisTemplate<String, ?> redisTemplate(//NOSONAR
                                           final LettuceConnectionFactory lettuceConnectionFactory) {
        var redisTemplate = new RedisTemplate<String, Object>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return redisTemplate;
    }

    @Bean
    @Autowired
    RedisTemplate<String, String> redisTemplateString(//NOSONAR // TODO will remove in next version
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

    /**
     * Customize rules for generating keys
     *
     * @return KeyGenerator
     */
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            val sb = new StringBuilder();
            sb.append(target.getClass().getName());
            sb.append(method.getName());
            Arrays.stream(params).sequential().forEach(sb::append);
            log.info("call Redis cache Key : " + sb);
            return sb.toString();
        };
    }

    /**
     * Customize for redis cache manager
     *
     * @return RedisCacheManagerBuilderCustomizer
     */
    @Bean
    RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return builder -> builder.withCacheConfiguration("monolithic",
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(5)));
    }

    /**
     * Cache manager configuration
     *
     * @param redisConnectionFactory bean inject
     * @return CacheManager
     */
    @Bean
    CacheManager cacheManager(
            @Qualifier("jedisConnectionFactory") final RedisConnectionFactory redisConnectionFactory) {
        return RedisCacheManager.create(redisConnectionFactory);
    }
}
