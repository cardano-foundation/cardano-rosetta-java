package org.cardanofoundation.rosetta.client;

import org.cardanofoundation.rosetta.client.model.domain.TokenRegistryBatchRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for selective property fetching functionality in token registry integration
 */
class SelectivePropertyFetchingTest {

    @Test
    void tokenRegistryBatchRequest_HasCorrectPropertiesField() {
        // Given
        TokenRegistryBatchRequest request = TokenRegistryBatchRequest.builder()
                .subjects(List.of("test-subject"))
                .properties(List.of("name", "description", "ticker"))
                .build();

        // When & Then
        assertThat(request.getSubjects()).contains("test-subject");
        assertThat(request.getProperties()).containsExactly("name", "description", "ticker");
    }

    @Test
    void tokenRegistryBatchRequest_SupportsAllKnownProperties() {
        // Given
        List<String> allKnownProperties = List.of("name", "description", "ticker", "decimals", "url", "version", "logo");
        
        TokenRegistryBatchRequest request = TokenRegistryBatchRequest.builder()
                .subjects(List.of("subject1", "subject2"))
                .properties(allKnownProperties)
                .build();

        // When & Then
        assertThat(request.getSubjects()).hasSize(2);
        assertThat(request.getProperties()).containsExactlyElementsOf(allKnownProperties);
    }

    @Test
    void tokenRegistryBatchRequest_HandlesEmptyCollections() {
        // Given
        TokenRegistryBatchRequest request = TokenRegistryBatchRequest.builder()
                .subjects(List.of())
                .properties(List.of())
                .build();

        // When & Then
        assertThat(request.getSubjects()).isEmpty();
        assertThat(request.getProperties()).isEmpty();
    }
}