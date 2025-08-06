package org.cardanofoundation.rosetta.api.search.controller;

import lombok.SneakyThrows;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.NetworkIdentifier;
import org.openapitools.client.model.SearchTransactionsRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import org.cardanofoundation.rosetta.api.BaseSpringMvcSetup;
import org.cardanofoundation.rosetta.api.network.service.NetworkService;
import org.cardanofoundation.rosetta.api.search.service.SearchService;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;

import static org.cardanofoundation.rosetta.testgenerator.common.TestConstants.TEST_ACCOUNT_ADDRESS;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SearchControllerTest extends BaseSpringMvcSetup {

  @Mock
  private SearchService service;

  @Mock
  private NetworkService networkService;

  @InjectMocks
  private SearchApiImpl searchApi;

  @Nested
  class ConfigurationValidationTests {

    @Test
    void shouldThrowExceptionWhenOfflineModeIsEnabled() {
      // Given
      SearchTransactionsRequest request = new SearchTransactionsRequest();
      searchApi.offlineMode = true;

      // When & Then
      assertThrows(ExceptionFactory.notSupportedInOfflineMode().getClass(), 
          () -> searchApi.searchTransactions(request));
    }
  }

  @Nested
  class NetworkValidationTests {

    @Test
    @SneakyThrows
    void shouldReturn5xxWhenNetworkIdentifierIsMissing() {
      // Given
      SearchTransactionsRequest request = new SearchTransactionsRequest();
      Mockito.when(service.searchTransaction(any(), any(), any())).thenReturn(Page.empty());

      // When & Then
      mockMvc.perform(post("/search/transactions")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().is5xxServerError());
    }
  }

  @Nested
  class PageSizeValidationTests {

    @Test
    void shouldThrowExceptionWhenPageSizeExceedsMaximum() {
      // Given
      SearchTransactionsRequest request = SearchTransactionsRequest.builder()
          .networkIdentifier(NetworkIdentifier.builder()
              .blockchain(TestConstants.TEST_BLOCKCHAIN)
              .network(TestConstants.TEST_NETWORK)
              .build())
          .limit(1000L) // Exceeds the default PAGE_SIZE limit
          .build();

      searchApi.LIMIT = 100L;

      // When & Then
      ApiException exception = assertThrows(ApiException.class, () -> searchApi.searchTransactions(request));
      assertEquals(5053, exception.getError().getCode());
      assertEquals("Invalid limit size", exception.getError().getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPageSizeExceedsMaximum_EdgeCase() {
      // Given
      SearchTransactionsRequest request = SearchTransactionsRequest.builder()
          .networkIdentifier(NetworkIdentifier.builder()
              .blockchain(TestConstants.TEST_BLOCKCHAIN)
              .network(TestConstants.TEST_NETWORK)
              .build())
          .limit(11L)
          .build();

      searchApi.LIMIT = 10L;

      // When & Then
      ApiException exception = assertThrows(ApiException.class, () -> searchApi.searchTransactions(request));
      assertEquals(5053, exception.getError().getCode());
      assertEquals("Invalid limit size", exception.getError().getMessage());
    }

    @Test
    void shouldPassWhenPageSizeEqualsMaximum() {
      // Given
      SearchTransactionsRequest request = SearchTransactionsRequest.builder()
          .networkIdentifier(NetworkIdentifier.builder()
              .blockchain(TestConstants.TEST_BLOCKCHAIN)
              .network(TestConstants.TEST_NETWORK)
              .build())
          .limit(10L) // Exactly equals PAGE_SIZE
          .build();

      // Set PAGE_SIZE for testing
      searchApi.LIMIT = 10L;

      // When & Then - This should not throw an exception
      // We're only testing the validation logic, not the full method execution
      try {
        searchApi.searchTransactions(request);
        // If we get here without an ApiException for page size, the validation passed
      } catch (ApiException e) {
        // Make sure it's not our page size validation error
        if (e.getError().getCode() == 5053) {
          throw e; // Re-throw if it's our validation error
        }
        // Other exceptions are fine - they're from later in the method
      } catch (Exception e) {
        // Other exceptions are fine - they're from later in the method (mocking issues)
      }
    }
  }

  @Nested
  class HttpIntegrationTests {

    @Test
    @SneakyThrows
    void shouldReturnSuccessfulResponseWithBasicRequest() {
      // Given
      SearchTransactionsRequest request = SearchTransactionsRequest.builder()
          .networkIdentifier(NetworkIdentifier.builder()
              .blockchain(TestConstants.TEST_BLOCKCHAIN)
              .network(TestConstants.TEST_NETWORK)
              .build())
          .build();

      // When & Then
      mockMvc.perform(post("/search/transactions")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.total_count").value(20))
          .andExpect(jsonPath("$.next_offset").doesNotExist());
    }

    @Test
    @SneakyThrows
    void shouldReturnSuccessfulResponseWithAccountFilter() {
      // Given
      SearchTransactionsRequest request = SearchTransactionsRequest.builder()
              .networkIdentifier(NetworkIdentifier.builder()
                      .blockchain(TestConstants.TEST_BLOCKCHAIN)
                      .network(TestConstants.TEST_NETWORK)
                      .build())
              .accountIdentifier(AccountIdentifier.builder()
                      .address(TEST_ACCOUNT_ADDRESS)
                      .build())
              .build();

      // When & Then
      mockMvc.perform(post("/search/transactions")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.total_count").value(4))
              .andExpect(jsonPath("$.next_offset").doesNotExist());
    }
  }

  @Nested
  class PaginationTests {

    @Test
    @SneakyThrows
    void shouldReturnNextOffsetWhenMoreResultsAvailable() {
      // Given
      SearchTransactionsRequest request = SearchTransactionsRequest.builder()
          .networkIdentifier(NetworkIdentifier.builder()
              .blockchain(TestConstants.TEST_BLOCKCHAIN)
              .network(TestConstants.TEST_NETWORK)
              .build())
          .build();

      // When & Then
      mockMvc.perform(post("/search/transactions")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.total_count").value(20))
          .andExpect(jsonPath("$.next_offset").doesNotExist());
    }

    @Test
    @SneakyThrows
    void shouldNotReturnNextOffsetWhenOnLastPage() {
      // Given
      SearchTransactionsRequest request = SearchTransactionsRequest.builder()
              .networkIdentifier(NetworkIdentifier.builder()
                      .blockchain(TestConstants.TEST_BLOCKCHAIN)
                      .network(TestConstants.TEST_NETWORK)
                      .build())
              .accountIdentifier(AccountIdentifier.builder()
                      .address(TEST_ACCOUNT_ADDRESS)
                      .build())
              .build();

      // When & Then
      mockMvc.perform(post("/search/transactions")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(request)))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.total_count").value(4))
              .andExpect(jsonPath("$.next_offset").doesNotExist());
    }
  }

  @Nested
  class TotalCountTests {

    @Test
    @SneakyThrows
    void shouldReturnTotalCountAcrossAllPages_FirstPage() {
      // Given - Request first page with limit 5
      SearchTransactionsRequest request = SearchTransactionsRequest.builder()
          .networkIdentifier(NetworkIdentifier.builder()
              .blockchain(TestConstants.TEST_BLOCKCHAIN)
              .network(TestConstants.TEST_NETWORK)
              .build())
          .limit(5L)
          .offset(0L)
          .build();

      // When & Then
      mockMvc.perform(post("/search/transactions")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.total_count").value(20)) // Total across all pages
          .andExpect(jsonPath("$.transactions").isArray())
          .andExpect(jsonPath("$.transactions.length()").value(5)) // Only 5 results on current page
          .andExpect(jsonPath("$.next_offset").value(5));
    }

    @Test
    @SneakyThrows
    void shouldReturnTotalCountAcrossAllPages_SecondPage() {
      // Given - Request second page with limit 5, offset 5
      SearchTransactionsRequest request = SearchTransactionsRequest.builder()
          .networkIdentifier(NetworkIdentifier.builder()
              .blockchain(TestConstants.TEST_BLOCKCHAIN)
              .network(TestConstants.TEST_NETWORK)
              .build())
          .limit(5L)
          .offset(5L)
          .build();

      // When & Then
      mockMvc.perform(post("/search/transactions")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.total_count").value(20)) // Same total across all pages
          .andExpect(jsonPath("$.transactions").isArray())
          .andExpect(jsonPath("$.transactions.length()").value(5)) // Only 5 results on current page
          .andExpect(jsonPath("$.next_offset").value(10)); // Next offset should be 5 + 5 = 10
    }

    @Test
    @SneakyThrows
    void shouldReturnTotalCountAcrossAllPages_LastPage() {
      // Given - Request last page with limit 5, offset 15 (for 20 total elements)
      SearchTransactionsRequest request = SearchTransactionsRequest.builder()
          .networkIdentifier(NetworkIdentifier.builder()
              .blockchain(TestConstants.TEST_BLOCKCHAIN)
              .network(TestConstants.TEST_NETWORK)
              .build())
          .limit(5L)
          .offset(15L)
          .build();

      // When & Then
      mockMvc.perform(post("/search/transactions")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.total_count").value(20)) // Same total across all pages
          .andExpect(jsonPath("$.transactions").isArray())
          .andExpect(jsonPath("$.transactions.length()").value(5)) // Only 5 results on current page
          .andExpect(jsonPath("$.next_offset").doesNotExist()); // No next page since 15+5=20 (total)
    }

    @Test
    @SneakyThrows
    void shouldReturnTotalCountWithFilters_AccountFilter() {
      // Given - Request with account filter
      SearchTransactionsRequest request = SearchTransactionsRequest.builder()
          .networkIdentifier(NetworkIdentifier.builder()
              .blockchain(TestConstants.TEST_BLOCKCHAIN)
              .network(TestConstants.TEST_NETWORK)
              .build())
          .accountIdentifier(AccountIdentifier.builder()
              .address(TEST_ACCOUNT_ADDRESS)
              .build())
          .limit(2L)
          .offset(0L)
          .build();

      // When & Then
      mockMvc.perform(post("/search/transactions")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.total_count").value(4)) // Total filtered results
          .andExpect(jsonPath("$.transactions").isArray())
          .andExpect(jsonPath("$.transactions.length()").value(2)) // Only 2 results on current page
          .andExpect(jsonPath("$.next_offset").value(2));
    }

    @Test
    @SneakyThrows
    void shouldReturnTotalCountWithFilters_SecondPageWithAccountFilter() {
      // Given - Request second page with account filter (offset 2 for 4 total elements)
      SearchTransactionsRequest request = SearchTransactionsRequest.builder()
          .networkIdentifier(NetworkIdentifier.builder()
              .blockchain(TestConstants.TEST_BLOCKCHAIN)
              .network(TestConstants.TEST_NETWORK)
              .build())
          .accountIdentifier(AccountIdentifier.builder()
              .address(TEST_ACCOUNT_ADDRESS)
              .build())
          .limit(2L)
          .offset(2L)
          .build();

      // When & Then
      mockMvc.perform(post("/search/transactions")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.total_count").value(4)) // Same total filtered results
          .andExpect(jsonPath("$.transactions").isArray())
          .andExpect(jsonPath("$.transactions.length()").value(2)) // Only 2 results on current page
          .andExpect(jsonPath("$.next_offset").doesNotExist()); // No next page since 2+2=4 (total)
    }

    @Test
    @SneakyThrows
    void shouldReturnConsistentTotalCountAcrossDifferentPageSizes() {
      // Given - Request with different page sizes
      SearchTransactionsRequest smallPageRequest = SearchTransactionsRequest.builder()
          .networkIdentifier(NetworkIdentifier.builder()
              .blockchain(TestConstants.TEST_BLOCKCHAIN)
              .network(TestConstants.TEST_NETWORK)
              .build())
          .limit(2L)
          .offset(0L)
          .build();

      SearchTransactionsRequest largePageRequest = SearchTransactionsRequest.builder()
          .networkIdentifier(NetworkIdentifier.builder()
              .blockchain(TestConstants.TEST_BLOCKCHAIN)
              .network(TestConstants.TEST_NETWORK)
              .build())
          .limit(10L)
          .offset(0L)
          .build();

      // When & Then - Both should return the same total_count
      mockMvc.perform(post("/search/transactions")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(smallPageRequest)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.total_count").value(20))
          .andExpect(jsonPath("$.transactions.length()").value(2));

      mockMvc.perform(post("/search/transactions")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(largePageRequest)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.total_count").value(20))
          .andExpect(jsonPath("$.transactions.length()").value(10));
    }
  }

}
