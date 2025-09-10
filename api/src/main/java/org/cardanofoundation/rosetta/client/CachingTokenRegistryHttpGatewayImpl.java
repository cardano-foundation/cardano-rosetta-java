package org.cardanofoundation.rosetta.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.client.model.domain.TokenRegistryBatchRequest;
import org.cardanofoundation.rosetta.client.model.domain.TokenRegistryBatchResponse;
import org.cardanofoundation.rosetta.client.model.domain.TokenSubject;
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
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class CachingTokenRegistryHttpGatewayImpl implements TokenRegistryHttpGateway {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Cache<String, Optional<TokenSubject>> tokenMetadataCache;

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
    public Map<String, Optional<TokenSubject>> getTokenMetadataBatch(Set<String> subjects) {
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
            Optional<TokenSubject> cached = Optional.ofNullable(tokenMetadataCache.getIfPresent(subject))
                    .flatMap(Function.identity());

            if (cached.isPresent()) {
                result.put(subject, cached);
            } else {
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

            log.debug("Sending batch request to token registry with {} subjects", subjectsToFetch.size());

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
                    result.put(tokenSubject.getSubject(), Optional.of(tokenSubject));

                    // Cache the result
                    tokenMetadataCache.put(tokenSubject.getSubject(), Optional.of(tokenSubject));

                    log.debug("Cached token metadata for subject: {}", tokenSubject.getSubject());
                }
            }

            // Cache empty results for subjects that were requested but not found
            for (String subjectToFetch : subjectsToFetch) {
                if (!result.containsKey(subjectToFetch)) {
                    result.put(subjectToFetch, Optional.empty());
                    tokenMetadataCache.put(subjectToFetch, Optional.empty());
                    log.debug("Cached empty result for subject: {}", subjectToFetch);
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
        log.debug("Evicted cache entries for subject: {}", subject);
    }

    @Scheduled(fixedRateString = "${cardano.rosetta.TOKEN_REGISTRY_CACHE_CLEAR_RATE:15m}")
    void clearCache() {
        tokenMetadataCache.invalidateAll();
        log.info("Cleared all token metadata cache entries");
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

}
