package org.cardanofoundation.rosetta.api.config.redis.cluster;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.util.List;

@Configuration
@Slf4j
@Profile("cluster")
public class RedisClusterConfig {

    @Value("${spring.data.redis.cluster.nodes}")
    private List<String> nodes;

    @Value("${spring.data.redis.password}")
    private String password;

    @Bean(name = "lettuceConnectionFactory")
    LettuceConnectionFactory lettuceConnectionFactory() {
        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration(nodes);
        redisClusterConfiguration.setPassword(password);
        return new LettuceConnectionFactory(redisClusterConfiguration);
    }

}