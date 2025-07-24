package org.cardanofoundation.rosetta.api.search.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SearchApiImpl Unit Tests")
class SearchApiImplTest {

    @Nested
    @DisplayName("calculateNextOffset Method Tests")
    class CalculateNextOffsetTests {

        @Test
        @DisplayName("Should return next offset when more elements available")
        void shouldReturnNextOffsetWhenMoreElementsAvailable() {
            // Given
            long offset = 0L;
            long limit = 10L;
            long totalElements = 100L; // 100 total elements

            // When
            Optional<Long> result = SearchApiImpl.calculateNextOffset(offset, limit, totalElements);

            // Then
            assertThat(result).contains(10L);
        }

        @Test
        @DisplayName("Should return next offset from middle page")
        void shouldReturnNextOffsetFromMiddlePage() {
            // Given
            long offset = 50L;
            long limit = 10L;
            long totalElements = 100L; // 100 total elements

            // When
            Optional<Long> result = SearchApiImpl.calculateNextOffset(offset, limit, totalElements);

            // Then
            assertThat(result).contains(60L);
        }

        @Test
        @DisplayName("Should return empty when next offset equals total elements")
        void shouldReturnEmptyWhenNextOffsetEqualsTotal() {
            // Given
            long offset = 90L;
            long limit = 10L;
            long totalElements = 100L; // 100 total, next would be 100

            // When
            Optional<Long> result = SearchApiImpl.calculateNextOffset(offset, limit, totalElements);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty when next offset exceeds total elements")
        void shouldReturnEmptyWhenNextOffsetExceedsTotal() {
            // Given
            long offset = 95L;
            long limit = 10L;
            long totalElements = 100L; // 100 total, next would be 105

            // When
            Optional<Long> result = SearchApiImpl.calculateNextOffset(offset, limit, totalElements);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty for single page with exact match")
        void shouldReturnEmptyForSinglePageExactMatch() {
            // Given
            long offset = 0L;
            long limit = 10L;
            long totalElements = 10L; // Exactly 10 total elements

            // When
            Optional<Long> result = SearchApiImpl.calculateNextOffset(offset, limit, totalElements);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty for single page with fewer elements")
        void shouldReturnEmptyForSinglePageWithFewerElements() {
            // Given
            long offset = 0L;
            long limit = 10L;
            long totalElements = 5L; // Only 5 total elements

            // When
            Optional<Long> result = SearchApiImpl.calculateNextOffset(offset, limit, totalElements);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle edge case with limit 1")
        void shouldHandleEdgeCaseWithLimit1() {
            // Given
            long offset = 0L;
            long limit = 1L;
            long totalElements = 5L; // 5 total elements

            // When
            Optional<Long> result = SearchApiImpl.calculateNextOffset(offset, limit, totalElements);

            // Then
            assertThat(result).contains(1L);
        }

        @Test
        @DisplayName("Should handle edge case with large offset")
        void shouldHandleEdgeCaseWithLargeOffset() {
            // Given
            long offset = 9990L;
            long limit = 10L;
            long totalElements = 10000L; // 10000 total elements

            // When
            Optional<Long> result = SearchApiImpl.calculateNextOffset(offset, limit, totalElements);

            // Then
            assertThat(result).isEmpty(); // Next would be 10000, which equals total
        }

        @Test
        @DisplayName("Should return correct offset for partial last page")
        void shouldReturnCorrectOffsetForPartialLastPage() {
            // Given
            long offset = 95L;
            long limit = 10L;
            long totalElements = 97L; // 97 total elements

            // When
            Optional<Long> result = SearchApiImpl.calculateNextOffset(offset, limit, totalElements);

            // Then
            assertThat(result).isEmpty(); // Next would be 105, which exceeds 97
        }

        @Test
        @DisplayName("Should handle zero total elements")
        void shouldHandleZeroTotalElements() {
            // Given
            long offset = 0L;
            long limit = 10L;
            long totalElements = 0L; // No elements

            // When
            Optional<Long> result = SearchApiImpl.calculateNextOffset(offset, limit, totalElements);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle large numbers correctly")
        void shouldHandleLargeNumbers() {
            // Given
            long offset = 1000000L;
            long limit = 50L;
            long totalElements = 1000040L; // Large number of total elements

            // When
            Optional<Long> result = SearchApiImpl.calculateNextOffset(offset, limit, totalElements);

            // Then
            assertThat(result).isEmpty(); // Next offset (1000050) would exceed total (1000040)
        }

        @Test
        @DisplayName("Should handle minimum valid values")
        void shouldHandleMinimumValidValues() {
            // Given
            long offset = 0L;
            long limit = 1L;
            long totalElements = 1L; // Single element

            // When
            Optional<Long> result = SearchApiImpl.calculateNextOffset(offset, limit, totalElements);

            // Then
            assertThat(result).isEmpty(); // Next offset would be 1, which equals total
        }

        @Test
        @DisplayName("Should return next offset for multi-page scenario")
        void shouldReturnNextOffsetForMultiPageScenario() {
            // Given
            long offset = 20L;
            long limit = 5L;
            long totalElements = 50L; // Multiple pages available

            // When
            Optional<Long> result = SearchApiImpl.calculateNextOffset(offset, limit, totalElements);

            // Then
            assertThat(result).contains(25L);
        }

        @Test
        @DisplayName("Should handle near-boundary conditions")
        void shouldHandleNearBoundaryConditions() {
            // Given
            long offset = 95L;
            long limit = 5L;
            long totalElements = 99L; // Next offset would be exactly at boundary

            // When
            Optional<Long> result = SearchApiImpl.calculateNextOffset(offset, limit, totalElements);

            // Then
            assertThat(result).isEmpty(); // Next would be 100, which exceeds 99
        }

        @Test
        @DisplayName("Should return next offset when exactly one more page available")
        void shouldReturnNextOffsetWhenExactlyOneMorePageAvailable() {
            // Given
            long offset = 90L;
            long limit = 5L;
            long totalElements = 99L; // Exactly one more page of data

            // When
            Optional<Long> result = SearchApiImpl.calculateNextOffset(offset, limit, totalElements);

            // Then
            assertThat(result).contains(95L); // Should return next offset
        }
    }
}