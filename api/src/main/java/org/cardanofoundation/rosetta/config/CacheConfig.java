package org.cardanofoundation.rosetta.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.cardanofoundation.rosetta.client.model.domain.TokenSubject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public ConcurrentMapCacheManager cacheManager() {
    return new ConcurrentMapCacheManager("protocolParamsCache");
  }

  @Bean
  public Cache<String, Optional<TokenSubject>> tokenMetadataCache(
      @Value("${cardano.rosetta.TOKEN_REGISTRY_CACHE_TTL_HOURS:1}") int cacheTtlHours) {
    return CacheBuilder.newBuilder()
        .maximumSize(10_000) // Maximum 10k cached entries
        .expireAfterWrite(cacheTtlHours, TimeUnit.HOURS)
        .recordStats()
        .build();
  }
}
