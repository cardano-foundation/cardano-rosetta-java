package org.cardanofoundation.rosetta.api.search.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.network.service.NetworkService;
import org.cardanofoundation.rosetta.api.search.mapper.SearchMapper;
import org.cardanofoundation.rosetta.api.search.service.SearchService;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.openapitools.client.api.SearchApi;
import org.openapitools.client.model.BlockTransaction;
import org.openapitools.client.model.SearchTransactionsRequest;
import org.openapitools.client.model.SearchTransactionsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SearchApiImpl implements SearchApi {

  private final NetworkService networkService;
  private final SearchService searchService;
  private final SearchMapper searchMapper;

  @Value("${cardano.rosetta.LIMIT}")
  Long LIMIT;

  @Value("${cardano.rosetta.OFFLINE_MODE}")
  boolean offlineMode;

  @Override
  public ResponseEntity<SearchTransactionsResponse> searchTransactions(
          SearchTransactionsRequest searchTransactionsRequest) {
    if (offlineMode) {
      throw ExceptionFactory.notSupportedInOfflineMode();
    }

    networkService.verifyNetworkRequest(searchTransactionsRequest.getNetworkIdentifier());

    Long limit = Optional.ofNullable(searchTransactionsRequest.getLimit())
            .orElse(LIMIT);

    if (limit > LIMIT) {
      log.warn("Requested limit {} exceeds maximum allowed size {}. Limiting to maximum size.", limit, LIMIT);
      throw ExceptionFactory.invalidLimitSize(limit, LIMIT);
    }

    long offset = Optional.ofNullable(searchTransactionsRequest.getOffset())
            .orElse(0L);


    SearchResults searchResults = performSearch(searchTransactionsRequest, limit, offset);

    long totalElementsCount = searchResults.blockTransactionsPage().getTotalElements();
    Optional<Long> nextOffsetM = calculateNextOffset(offset, limit, totalElementsCount);
    SearchTransactionsResponse searchResponse = searchMapper.mapToSearchTransactionsResponse(searchResults.blockTransactionList(), nextOffsetM.orElse(null), totalElementsCount);

    return ResponseEntity.ok(searchResponse);
  }

  SearchResults performSearch(SearchTransactionsRequest searchTransactionsRequest, Long limit, long offset) {
    Page<BlockTransaction> blockTransactionsPage;
    List<BlockTransaction> blockTransactionList;
    if (limit == 0) {
      // For limit=0, we still need to get the total count, so we query with a minimal limit
      blockTransactionsPage = searchService.searchTransaction(searchTransactionsRequest, offset, 1L);
      blockTransactionList = List.of(); // Return empty list
    } else {
      blockTransactionsPage = searchService.searchTransaction(searchTransactionsRequest, offset, limit);
      blockTransactionList = blockTransactionsPage.getContent();
    }

    return new SearchResults(blockTransactionsPage, blockTransactionList);
  }

  static Optional<Long> calculateNextOffset(long offset,
                                            long limit,
                                            long totalElements) {
    // If limit is 0, there's no next offset since no results are returned
    if (limit == 0) {
      return Optional.of(0L);
    }

    long nextOffset = offset + limit;

    // If the next offset would be beyond the total elements, there are no more pages
    if (nextOffset >= totalElements) {
      return Optional.empty();
    }

    return Optional.of(nextOffset);
  }

  record SearchResults(Page<BlockTransaction> blockTransactionsPage, List<BlockTransaction> blockTransactionList) {}

}
