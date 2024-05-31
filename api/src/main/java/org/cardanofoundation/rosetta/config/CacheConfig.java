package org.cardanofoundation.rosetta.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {
  @Bean
  public ConcurrentMapCacheManager cacheManager() {
    return new ConcurrentMapCacheManager("protocolParamsCache");
  }
}
