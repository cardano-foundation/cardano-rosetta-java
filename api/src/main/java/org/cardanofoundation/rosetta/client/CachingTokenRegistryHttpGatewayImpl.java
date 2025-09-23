package org.cardanofoundation.rosetta.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.client.model.domain.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CachingTokenRegistryHttpGatewayImpl implements TokenRegistryHttpGateway {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Cache<String, TokenCacheEntry> tokenMetadataCache;

    @Value("${cardano.rosetta.TOKEN_REGISTRY_ENABLED:true}")
    protected boolean enabled;

    @Value("${cardano.rosetta.TOKEN_REGISTRY_BASE_URL:https://tokens.cardano.org/api}")
    protected String tokenRegistryBaseUrl;

    @Value("${cardano.rosetta.HTTP_REQUEST_TIMEOUT_SECONDS:5}")
    protected int httpRequestTimeoutSeconds;

    @Value("${cardano.rosetta.TOKEN_REGISTRY_LOGO_FETCH:false}")
    protected boolean logoFetchEnabled;

    private String batchEndpointUrl;

    @PostConstruct
    public void init() {
        batchEndpointUrl = tokenRegistryBaseUrl + "/v2/subjects/query";
        log.info("TokenRegistryHttpGatewayImpl initialized with enabled: {}, batchEndpointUrl: {}, httpRequestTimeoutSeconds: {}", 
                enabled, batchEndpointUrl, httpRequestTimeoutSeconds);
    }

    @Override
    public Map<String, Optional<TokenSubject>> getTokenMetadataBatch(@NonNull Set<String> subjects) {
        if (!enabled) {
            log.debug("Token registry is disabled, returning empty map");
            return Collections.emptyMap();
        }

        if (subjects.isEmpty()) {
            return Collections.emptyMap();
        }

        // Check cache for existing entries
        Set<String> subjectsToFetch = new HashSet<>();
        Map<String, Optional<TokenSubject>> result = new HashMap<>();

        for (String subject : subjects) {
            TokenCacheEntry cached = tokenMetadataCache.getIfPresent(subject);

            if (cached != null) {
                // We have a cache entry (either found or not found)
                result.put(subject, cached.getTokenSubject());
                log.debug("Retrieved cached entry for subject: {} (found: {})", subject, cached.isFound());
            } else {
                // Not in cache at all, need to fetch
                subjectsToFetch.add(subject);
            }
        }

        // If we have all subjects cached, return cached results
        if (subjectsToFetch.isEmpty()) {
            log.debug("All subjects found in cache, returning cached results");
            return result;
        }

        log.debug("Fetching {} subjects from token registry: {}", subjectsToFetch.size(), subjectsToFetch);

        try {
            // Create batch request with selective properties
            TokenRegistryBatchRequest request = TokenRegistryBatchRequest.builder()
                    .subjects(new ArrayList<>(subjectsToFetch))
                    .properties(buildPropertiesList())
                    .build();

            // Prepare HTTP request
            String requestBody = objectMapper.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(batchEndpointUrl))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(httpRequestTimeoutSeconds))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();

            // Execute request
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Token registry returned non-200 status: {} for batch request", response.statusCode());
                return result; // Return partial results from cache
            }

            // Parse response
            TokenRegistryBatchResponse batchResponse = objectMapper.readValue(response.body(), TokenRegistryBatchResponse.class);

            if (batchResponse.getSubjects() != null) {
                for (TokenSubject tokenSubject : batchResponse.getSubjects()) {
                    // Validate that essential metadata exists
                    if (isValidTokenSubject(tokenSubject)) {
                        result.put(tokenSubject.getSubject(), Optional.of(tokenSubject));

                        // Cache the valid result
                        tokenMetadataCache.put(tokenSubject.getSubject(), TokenCacheEntry.found(tokenSubject));

                        log.debug("Cached valid token metadata for subject: {}", tokenSubject.getSubject());
                    } else {
                        // Token exists in registry but lacks essential metadata - treat as not found
                        result.put(tokenSubject.getSubject(), Optional.empty());
                        tokenMetadataCache.put(tokenSubject.getSubject(), TokenCacheEntry.notFound());
                    }
                }
            }

            // Cache empty results for subjects that were requested but not found in the response
            for (String subjectToFetch : subjectsToFetch) {
                if (!result.containsKey(subjectToFetch)) {
                    result.put(subjectToFetch, Optional.empty());
                    tokenMetadataCache.put(subjectToFetch, TokenCacheEntry.notFound());
                    log.debug("Cached not-found result for subject: {}", subjectToFetch);
                }
            }

            log.debug("Successfully fetched and cached {} token metadata entries", 
                    batchResponse.getSubjects() != null ? batchResponse.getSubjects().size() : 0);

            return result;

        } catch (IOException e) {
            log.error("IO error while fetching token metadata batch", e);
            return result; // Return partial results from cache
        } catch (InterruptedException e) {
            log.error("Request interrupted while fetching token metadata batch", e);
            Thread.currentThread().interrupt();
            return result; // Return partial results from cache
        } catch (Exception e) {
            log.error("Unexpected error while fetching token metadata batch", e);
            return result; // Return partial results from cache
        }
    }

    void evictFromCache(String subject) {
        tokenMetadataCache.invalidate(subject);
        log.debug("Evicted cache entry for subject: {}", subject);
    }

    @Scheduled(fixedRateString = "${cardano.rosetta.TOKEN_REGISTRY_CACHE_CLEAR_RATE:15m}")
    void clearCache() {
        tokenMetadataCache.invalidateAll();
        log.info("Cleared all token metadata cache entries.");
    }

    List<String> buildPropertiesList() {
        List<String> properties = new ArrayList<>();
        
        // Add all properties except logo (conditionally)
        properties.add("name");
        properties.add("description");  
        properties.add("ticker");
        properties.add("decimals");
        properties.add("url");
        properties.add("version");
        
        // Only add logo if enabled
        if (logoFetchEnabled) {
            properties.add("logo");
            log.debug("Logo fetching enabled - including logo property in request");
        } else {
            log.debug("Logo fetching disabled - excluding logo property from request");
        }
        
        return properties;
    }

    /**
     * Validates that a TokenSubject has the essential metadata required.
     * Tokens without name and description are considered invalid and should be treated as not found.
     * 
     * @param tokenSubject The token subject to validate
     * @return true if the token has valid essential metadata, false otherwise
     */
    // THIS IS A WORKAROUND for https://github.com/cardano-foundation/cf-token-metadata-registry/issues/30
    private boolean isValidTokenSubject(TokenSubject tokenSubject) {
        if (tokenSubject == null || tokenSubject.getSubject() == null) {
            return false;
        }
        
        TokenMetadata metadata = tokenSubject.getMetadata();
        if (metadata == null) {
            return false;
        }
        
        // Name and description are essential fields - if they're null, the token data is incomplete
        if (metadata.getName() == null || metadata.getDescription() == null) {
            return false;
        }
        
        // Further check that the actual values exist
        if (metadata.getName().getValue() == null || metadata.getDescription().getValue() == null) {
            return false;
        }
        
        return true;
    }

}
