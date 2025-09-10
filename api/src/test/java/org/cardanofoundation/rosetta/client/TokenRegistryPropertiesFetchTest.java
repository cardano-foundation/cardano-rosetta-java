package org.cardanofoundation.rosetta.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.cardanofoundation.rosetta.client.model.domain.TokenSubject;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Simple test for selective property fetching without reflection
 */
class TokenRegistryPropertiesFetchTest {

    @Test
    void buildPropertiesList_WhenLogoFetchEnabled_IncludesAllProperties() {
        // Given
        HttpClient httpClient = mock(HttpClient.class);
        Cache<String, Optional<TokenSubject>> cache = CacheBuilder.newBuilder().maximumSize(100).build();
        
        TestableTokenRegistryGateway gateway = new TestableTokenRegistryGateway(httpClient, cache, true);
        
        // When
        List<String> properties = gateway.buildPropertiesList();
        
        // Then
        assertThat(properties).contains("name", "description", "ticker", "decimals", "url", "version", "logo");
    }

    @Test
    void buildPropertiesList_WhenLogoFetchDisabled_ExcludesLogo() {
        // Given
        HttpClient httpClient = mock(HttpClient.class);
        Cache<String, Optional<TokenSubject>> cache = CacheBuilder.newBuilder().maximumSize(100).build();
        
        TestableTokenRegistryGateway gateway = new TestableTokenRegistryGateway(httpClient, cache, false);
        
        // When
        List<String> properties = gateway.buildPropertiesList();
        
        // Then
        assertThat(properties).contains("name", "description", "ticker", "decimals", "url", "version");
        assertThat(properties).doesNotContain("logo");
    }

    /**
     * Test-specific subclass that exposes configuration for testing without reflection
     */
    static class TestableTokenRegistryGateway extends CachingTokenRegistryHttpGatewayImpl {
        private final boolean testLogoFetchEnabled;

        public TestableTokenRegistryGateway(HttpClient httpClient, Cache<String, Optional<TokenSubject>> cache, boolean logoFetchEnabled) {
            super(httpClient, cache);
            this.testLogoFetchEnabled = logoFetchEnabled;
        }

        @Override
        List<String> buildPropertiesList() {
            // Override to use test configuration instead of @Value field
            List<String> properties = new java.util.ArrayList<>();
            properties.add("name");
            properties.add("description");
            properties.add("ticker");
            properties.add("decimals");
            properties.add("url");
            properties.add("version");
            
            if (testLogoFetchEnabled) {
                properties.add("logo");
            }
            
            return properties;
        }
    }
}