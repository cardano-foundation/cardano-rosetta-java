package org.cardanofoundation.rosetta.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.cardanofoundation.rosetta.client.model.domain.TokenCacheEntry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.concurrent.TimeUnit.HOURS;

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public ConcurrentMapCacheManager cacheManager() {
    return new ConcurrentMapCacheManager("protocolParamsCache");
  }

  //a cache for token metadata from token registry
  @Bean
  public Cache<String, TokenCacheEntry> tokenMetadataCache(
      @Value("${cardano.rosetta.TOKEN_REGISTRY_CACHE_TTL_HOURS:12}") int cacheTtlHours) {
    return CacheBuilder.newBuilder()
        .maximumSize(10_000) // Maximum 10k cached entries
        .expireAfterWrite(cacheTtlHours, HOURS)
        .recordStats()
        .build();
  }

}
