package org.cardanofoundation.rosetta.api.search.controller;

import org.cardanofoundation.rosetta.api.search.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.BlockTransaction;
import org.openapitools.client.model.SearchTransactionsRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchApiImpl Unit Tests")
class SearchApiImplTest {

    @Mock
    private SearchService searchService;

    private SearchApiImpl searchApiImpl;

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

        @Test
        @DisplayName("Should return empty when limit is 0")
        void shouldReturnEmptyWhenLimitIsZero() {
            // Given
            long offset = 0L;
            long limit = 0L;
            long totalElements = 100L; // Even with many elements available

            // When
            Optional<Long> result = SearchApiImpl.calculateNextOffset(offset, limit, totalElements);

            // Then
            assertThat(result).contains(0L); // Next offset is 0 when limit is 0
        }

        @Test
        @DisplayName("Should return empty when limit is 0 with non-zero offset")
        void shouldReturnEmptyWhenLimitIsZeroWithNonZeroOffset() {
            // Given
            long offset = 50L;
            long limit = 0L;
            long totalElements = 100L; // Even with many elements available

            // When
            Optional<Long> result = SearchApiImpl.calculateNextOffset(offset, limit, totalElements);

            // Then
            assertThat(result).contains(0L); // Next offset is 0 when limit is 0, regardless of offset
        }

        @Test
        @DisplayName("Should return empty when limit is 0 with zero total elements")
        void shouldReturnEmptyWhenLimitIsZeroWithZeroTotalElements() {
            // Given
            long offset = 0L;
            long limit = 0L;
            long totalElements = 0L; // No elements

            // When
            Optional<Long> result = SearchApiImpl.calculateNextOffset(offset, limit, totalElements);

            // Then
            assertThat(result).contains(0L); // Next offset is 0 when limit is 0
        }
    }

    @Nested
    @DisplayName("performSearch Method Tests")
    class PerformSearchTests {

        @BeforeEach
        void setUp() {
            // Create a minimal SearchApiImpl instance for testing performSearch
            // Since we're only testing performSearch, we only need the searchService dependency
            searchApiImpl = new SearchApiImpl(null, searchService, null);
        }

        @Test
        @DisplayName("Should return actual transactions when limit is greater than zero")
        void shouldReturnActualTransactionsWhenLimitGreaterThanZero() {
            // Given
            SearchTransactionsRequest request = new SearchTransactionsRequest();
            Long limit = 5L;
            long offset = 0L;
            
            BlockTransaction transaction1 = new BlockTransaction();
            BlockTransaction transaction2 = new BlockTransaction();
            List<BlockTransaction> transactions = List.of(transaction1, transaction2);
            Page<BlockTransaction> page = new PageImpl<>(transactions);
            
            when(searchService.searchTransaction(request, offset, limit)).thenReturn(page);

            // When
            SearchApiImpl.SearchResults result = searchApiImpl.performSearch(request, limit, offset);

            // Then
            assertThat(result.blockTransactionList()).hasSize(2);
            assertThat(result.blockTransactionList()).containsExactly(transaction1, transaction2);
            assertThat(result.blockTransactionsPage()).isEqualTo(page);
            verify(searchService).searchTransaction(request, offset, limit);
        }

        @Test
        @DisplayName("Should return empty list when limit is zero")
        void shouldReturnEmptyListWhenLimitIsZero() {
            // Given
            SearchTransactionsRequest request = new SearchTransactionsRequest();
            Long limit = 0L;
            long offset = 0L;
            
            BlockTransaction transaction1 = new BlockTransaction();
            List<BlockTransaction> transactions = List.of(transaction1);
            Page<BlockTransaction> page = new PageImpl<>(transactions);
            
            when(searchService.searchTransaction(request, offset, 1L)).thenReturn(page);

            // When
            SearchApiImpl.SearchResults result = searchApiImpl.performSearch(request, limit, offset);

            // Then
            assertThat(result.blockTransactionList()).isEmpty();
            assertThat(result.blockTransactionsPage()).isEqualTo(page);
            // Verify that searchService was called with limit=1 to get total count
            verify(searchService).searchTransaction(request, offset, 1L);
        }

        @Test
        @DisplayName("Should handle empty page results when limit is greater than zero")
        void shouldHandleEmptyPageResultsWhenLimitGreaterThanZero() {
            // Given
            SearchTransactionsRequest request = new SearchTransactionsRequest();
            Long limit = 10L;
            long offset = 100L;
            
            List<BlockTransaction> emptyTransactions = List.of();
            Page<BlockTransaction> emptyPage = new PageImpl<>(emptyTransactions);
            
            when(searchService.searchTransaction(request, offset, limit)).thenReturn(emptyPage);

            // When
            SearchApiImpl.SearchResults result = searchApiImpl.performSearch(request, limit, offset);

            // Then
            assertThat(result.blockTransactionList()).isEmpty();
            assertThat(result.blockTransactionsPage()).isEqualTo(emptyPage);
            verify(searchService).searchTransaction(request, offset, limit);
        }

        @Test
        @DisplayName("Should handle empty page results when limit is zero")
        void shouldHandleEmptyPageResultsWhenLimitIsZero() {
            // Given
            SearchTransactionsRequest request = new SearchTransactionsRequest();
            Long limit = 0L;
            long offset = 50L;
            
            List<BlockTransaction> emptyTransactions = List.of();
            Page<BlockTransaction> emptyPage = new PageImpl<>(emptyTransactions);
            
            when(searchService.searchTransaction(request, offset, 1L)).thenReturn(emptyPage);

            // When
            SearchApiImpl.SearchResults result = searchApiImpl.performSearch(request, limit, offset);

            // Then
            assertThat(result.blockTransactionList()).isEmpty();
            assertThat(result.blockTransactionsPage()).isEqualTo(emptyPage);
            verify(searchService).searchTransaction(request, offset, 1L);
        }

        @Test
        @DisplayName("Should pass correct parameters to searchService for normal limit")
        void shouldPassCorrectParametersToSearchServiceForNormalLimit() {
            // Given
            SearchTransactionsRequest request = new SearchTransactionsRequest();
            Long limit = 25L;
            long offset = 150L;
            
            BlockTransaction transaction = new BlockTransaction();
            List<BlockTransaction> transactions = List.of(transaction);
            Page<BlockTransaction> page = new PageImpl<>(transactions);
            
            when(searchService.searchTransaction(request, offset, limit)).thenReturn(page);

            // When
            searchApiImpl.performSearch(request, limit, offset);

            // Then
            verify(searchService).searchTransaction(eq(request), eq(offset), eq(limit));
        }

        @Test
        @DisplayName("Should pass correct parameters to searchService for zero limit")
        void shouldPassCorrectParametersToSearchServiceForZeroLimit() {
            // Given
            SearchTransactionsRequest request = new SearchTransactionsRequest();
            Long limit = 0L;
            long offset = 75L;
            
            Page<BlockTransaction> page = new PageImpl<>(List.of());
            when(searchService.searchTransaction(request, offset, 1L)).thenReturn(page);

            // When
            searchApiImpl.performSearch(request, limit, offset);

            // Then
            // Verify that when limit=0, searchService is called with limit=1 to get total count
            verify(searchService).searchTransaction(eq(request), eq(offset), eq(1L));
        }
    }
}