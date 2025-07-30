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

    Page<BlockTransaction> blockTransactionsPage = searchService.searchTransaction(searchTransactionsRequest, offset, limit);

    long totalElementsCount = blockTransactionsPage.getTotalElements();
    List<BlockTransaction> blockTransactionList = blockTransactionsPage.getContent();
    Optional<Long> nextOffsetM = calculateNextOffset(offset, limit, totalElementsCount);
    SearchTransactionsResponse searchResponse = searchMapper.mapToSearchTransactionsResponse(blockTransactionList, nextOffsetM.orElse(null), totalElementsCount);

    return ResponseEntity.ok(searchResponse);
  }

  static Optional<Long> calculateNextOffset(long offset,
                                  long limit,
                                  long totalElements) {
    long nextOffset = offset + limit;
    
    // If the next offset would be beyond the total elements, there are no more pages
    if (nextOffset >= totalElements) {
      return Optional.empty();
    }

    return Optional.of(nextOffset);
  }

}
