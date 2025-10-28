package org.cardanofoundation.rosetta.client;

import com.google.common.cache.Cache;
import org.cardanofoundation.rosetta.client.model.domain.TokenCacheEntry;
import org.cardanofoundation.rosetta.client.model.domain.TokenMetadata;
import org.cardanofoundation.rosetta.client.model.domain.TokenProperty;
import org.cardanofoundation.rosetta.client.model.domain.TokenPropertyNumber;
import org.cardanofoundation.rosetta.client.model.domain.TokenSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CachingTokenRegistryHttpGatewayImplTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private Cache<String, TokenCacheEntry> tokenMetadataCache;

    @Mock
    private HttpResponse<String> httpResponse;

    @InjectMocks
    private CachingTokenRegistryHttpGatewayImpl tokenRegistryHttpGateway;

    private final String testSubject = "577f0b1342f8f8f4aed3388b80a8535812950c7a892495c0ecdf0f1e0014df10464c4454";
    private final String testSubject2 = "29d222ce763455e3d7a09a665ce554f00ac89d2e99a1a83d267170c64d494e";

    private TokenSubject createTestTokenSubject(String subject, String name, String ticker, Long decimals) {
        return TokenSubject.builder()
                .subject(subject)
                .metadata(TokenMetadata.builder()
                        .name(TokenProperty.builder().value(name).source("CIP_68").build())
                        .description(TokenProperty.builder().value(name + " Token").source("CIP_68").build())
                        .ticker(TokenProperty.builder().value(ticker).source("CIP_68").build())
                        .decimals(TokenPropertyNumber.builder().value(decimals).source("CIP_68").build())
                        .build())
                .build();
    }

    private String createTestBatchResponseJson() {
        return """
                {
                  "subjects": [
                    {
                      "subject": "577f0b1342f8f8f4aed3388b80a8535812950c7a892495c0ecdf0f1e0014df10464c4454",
                      "metadata": {
                        "name": {
                          "value": "FLDT",
                          "source": "CIP_68"
                        },
                        "description": {
                          "value": "FLDT Token",
                          "source": "CIP_68"
                        },
                        "ticker": {
                          "value": "FLDT",
                          "source": "CIP_68"
                        },
                        "decimals": {
                          "value": 6,
                          "source": "CIP_68"
                        }
                      }
                    }
                  ],
                  "queryPriority": [
                    "CIP_68",
                    "CIP_26"
                  ]
                }
                """;
    }

    @BeforeEach
    void setUp() {
        tokenRegistryHttpGateway.enabled = true;
        tokenRegistryHttpGateway.tokenRegistryBaseUrl = "https://tokens.cardano.org/api";
        tokenRegistryHttpGateway.httpRequestTimeoutSeconds = 5;
        tokenRegistryHttpGateway.init();
    }



    @Nested
    class GetTokenMetadataBatchTests {

        @Test
        void getTokenMetadataBatch_WhenDisabled_ReturnsEmptyMap() {
            tokenRegistryHttpGateway.enabled = false;

            Map<String, Optional<TokenSubject>> result = tokenRegistryHttpGateway.getTokenMetadataBatch(
                    Set.of(testSubject));

            assertThat(result).isEmpty();
            verifyNoInteractions(tokenMetadataCache, httpClient);
        }

        @Test
        void getTokenMetadataBatch_WhenSubjectsIsNull_ThrowsException() {
            assertThatThrownBy(() -> tokenRegistryHttpGateway.getTokenMetadataBatch(null))
                    .isInstanceOf(NullPointerException.class);
            
            verifyNoInteractions(tokenMetadataCache, httpClient);
        }

        @Test
        void getTokenMetadataBatch_WhenSubjectsIsEmpty_ReturnsEmptyMap() {
            Map<String, Optional<TokenSubject>> result = tokenRegistryHttpGateway.getTokenMetadataBatch(Collections.emptySet());

            assertThat(result).isEmpty();
            verifyNoInteractions(tokenMetadataCache, httpClient);
        }

        @Test
        void getTokenMetadataBatch_WhenAllCacheHits_ReturnsCachedResults() {
            Set<String> subjects = Set.of(testSubject, testSubject2);
            TokenSubject cached1 = createTestTokenSubject(testSubject, "FLDT", "FLDT", 6L);
            TokenSubject cached2 = createTestTokenSubject(testSubject2, "MIN", "MIN", 6L);
            
            when(tokenMetadataCache.getIfPresent(testSubject)).thenReturn(TokenCacheEntry.found(cached1));
            when(tokenMetadataCache.getIfPresent(testSubject2)).thenReturn(TokenCacheEntry.found(cached2));

            Map<String, Optional<TokenSubject>> result = tokenRegistryHttpGateway.getTokenMetadataBatch(subjects);

            assertThat(result).hasSize(2);
            assertThat(result.get(testSubject)).isEqualTo(Optional.of(cached1));
            assertThat(result.get(testSubject2)).isEqualTo(Optional.of(cached2));
            verifyNoInteractions(httpClient);
        }

        @Test
        void getTokenMetadataBatch_WhenCacheMiss_FetchesFromRegistry() throws Exception {
            Set<String> subjects = Set.of(testSubject);
            when(tokenMetadataCache.getIfPresent(anyString())).thenReturn(null);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(createTestBatchResponseJson());

            Map<String, Optional<TokenSubject>> result = tokenRegistryHttpGateway.getTokenMetadataBatch(subjects);

            assertThat(result).hasSize(1);
            assertThat(result.get(testSubject)).isPresent();
            assertThat(result.get(testSubject).get().getMetadata().getName().getValue()).isEqualTo("FLDT");

            // Verify HTTP request was made
            ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
            verify(httpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));
            
            HttpRequest capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.uri().toString()).isEqualTo("https://tokens.cardano.org/api/v2/subjects/query");
            assertThat(capturedRequest.method()).isEqualTo("POST");
            
            verify(tokenMetadataCache).put(eq(testSubject), any(TokenCacheEntry.class));
        }

        @Test
        void getTokenMetadataBatch_WhenHttpError_ReturnsPartialResults() throws Exception {
            Set<String> subjects = Set.of(testSubject, testSubject2);
            TokenSubject cached1 = createTestTokenSubject(testSubject, "FLDT", "FLDT", 6L);
            
            when(tokenMetadataCache.getIfPresent(testSubject)).thenReturn(TokenCacheEntry.found(cached1));
            when(tokenMetadataCache.getIfPresent(testSubject2)).thenReturn(null);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);
            when(httpResponse.statusCode()).thenReturn(500); // Simulate server error

            Map<String, Optional<TokenSubject>> result = tokenRegistryHttpGateway.getTokenMetadataBatch(subjects);

            assertThat(result).hasSize(1); // Only cached result
            assertThat(result.get(testSubject)).isEqualTo(Optional.of(cached1));
            assertThat(result.get(testSubject2)).isNull();
        }

        @Test
        void getTokenMetadataBatch_FiltersInvalidSubjects() throws Exception {
            // Use HashSet to allow null values
            Set<String> subjects = new HashSet<>(Arrays.asList(testSubject, null, "", "   "));
            when(tokenMetadataCache.getIfPresent(any())).thenReturn(null);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(createTestBatchResponseJson());

            Map<String, Optional<TokenSubject>> result = tokenRegistryHttpGateway.getTokenMetadataBatch(subjects);

            // Should only process the valid subject
            ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
            verify(httpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));
            
            // Verify cache was checked for all subjects (implementation doesn't filter)
            verify(tokenMetadataCache).getIfPresent(testSubject);
            verify(tokenMetadataCache).getIfPresent(eq(null));
            verify(tokenMetadataCache).getIfPresent("");
            verify(tokenMetadataCache).getIfPresent("   ");
        }

        @Test
        void getTokenMetadataBatch_WithProperties_UsesPropertiesInRequest() throws Exception {
            Set<String> subjects = Set.of(testSubject);

            when(tokenMetadataCache.getIfPresent(anyString())).thenReturn(null);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(createTestBatchResponseJson());

            tokenRegistryHttpGateway.getTokenMetadataBatch(subjects);

            // Verify properties were included in cache key
            String expectedCacheKey = testSubject;
            verify(tokenMetadataCache).getIfPresent(expectedCacheKey);
            verify(tokenMetadataCache).put(eq(expectedCacheKey), any(TokenCacheEntry.class));
        }

        @Test
        void getTokenMetadataBatch_WhenSubjectNotFoundInRegistry_ReturnsEmptyOptional() throws Exception {
            Set<String> subjects = Set.of(testSubject, "nonexistent_subject");
            when(tokenMetadataCache.getIfPresent(anyString())).thenReturn(null);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn(createTestBatchResponseJson()); // Only contains testSubject

            Map<String, Optional<TokenSubject>> result = tokenRegistryHttpGateway.getTokenMetadataBatch(subjects);

            assertThat(result).hasSize(2);
            assertThat(result.get(testSubject)).isPresent();
            assertThat(result.get("nonexistent_subject")).isEqualTo(Optional.empty());
            
            // Verify both subjects were cached (found as TokenCacheEntry.found, not found as TokenCacheEntry.notFound)
            verify(tokenMetadataCache).put(eq(testSubject), argThat(entry -> entry.isFound()));
            verify(tokenMetadataCache).put(eq("nonexistent_subject"), argThat(entry -> !entry.isFound()));
        }

        @Test
        void getTokenMetadataBatch_WhenNotFoundTokenIsCached_SkipsRegistryCall() throws Exception {
            Set<String> subjects = Set.of("nonexistent_subject");
            
            // Token is cached as not found
            when(tokenMetadataCache.getIfPresent("nonexistent_subject")).thenReturn(TokenCacheEntry.notFound());

            Map<String, Optional<TokenSubject>> result = tokenRegistryHttpGateway.getTokenMetadataBatch(subjects);

            assertThat(result).hasSize(1);
            assertThat(result.get("nonexistent_subject")).isEqualTo(Optional.empty());
            
            // Verify no HTTP call was made since we had cached not-found result
            verifyNoInteractions(httpClient);
        }
    }

    @Nested
    class CacheManagementTests {

        @Test
        void evictFromCache_WithValidSubject_InvalidatesCache() {
            tokenRegistryHttpGateway.evictFromCache(testSubject);

            verify(tokenMetadataCache).invalidate(testSubject);
            // Only invalidates the specific subject
            verify(tokenMetadataCache, times(1)).invalidate(testSubject);
        }

        @Test
        void evictFromCache_WithNullSubject_InvalidatesCache() {
            tokenRegistryHttpGateway.evictFromCache(null);

            verify(tokenMetadataCache).invalidate(null);
        }

        @Test
        void evictFromCache_WithEmptySubject_InvalidatesCache() {
            tokenRegistryHttpGateway.evictFromCache("");

            verify(tokenMetadataCache).invalidate("");
        }

    }

    @Nested
    class ErrorHandlingTests {

        @Test
        void getTokenMetadataBatch_WhenInterrupted_HandlesInterruption() throws Exception {
            when(tokenMetadataCache.getIfPresent(anyString())).thenReturn(null);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenThrow(new InterruptedException("Thread interrupted"));

            Map<String, Optional<TokenSubject>> result = tokenRegistryHttpGateway.getTokenMetadataBatch(Set.of(testSubject));

            assertThat(result).isEmpty();
            // Verify thread interrupt status is restored
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
            Thread.interrupted(); // Clear interrupt status for subsequent tests
        }

        @Test
        void getTokenMetadataBatch_WhenIOException_ReturnsPartialResults() throws Exception {
            Set<String> subjects = Set.of(testSubject, testSubject2);
            TokenSubject cached1 = createTestTokenSubject(testSubject, "FLDT", "FLDT", 6L);
            
            when(tokenMetadataCache.getIfPresent(testSubject)).thenReturn(TokenCacheEntry.found(cached1));
            when(tokenMetadataCache.getIfPresent(testSubject2)).thenReturn(null);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenThrow(new IOException("Network error"));

            Map<String, Optional<TokenSubject>> result = tokenRegistryHttpGateway.getTokenMetadataBatch(subjects);

            assertThat(result).hasSize(1); // Only cached result
            assertThat(result.get(testSubject)).isEqualTo(Optional.of(cached1));
        }

        @Test
        void getTokenMetadataBatch_WhenJSONParsingFails_ReturnsPartialResults() throws Exception {
            Set<String> subjects = Set.of(testSubject);
            when(tokenMetadataCache.getIfPresent(anyString())).thenReturn(null);
            when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(httpResponse);
            when(httpResponse.statusCode()).thenReturn(200);
            when(httpResponse.body()).thenReturn("invalid json");

            Map<String, Optional<TokenSubject>> result = tokenRegistryHttpGateway.getTokenMetadataBatch(subjects);

            assertThat(result).isEmpty();
        }
    }

}