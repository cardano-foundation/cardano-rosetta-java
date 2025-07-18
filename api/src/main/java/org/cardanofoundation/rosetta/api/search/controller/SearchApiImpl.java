package org.cardanofoundation.rosetta.api.search.controller;

import jakarta.annotation.Nullable;
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
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SearchApiImpl implements SearchApi {

  private final NetworkService networkService;
  private final SearchService searchService;
  private final SearchMapper searchMapper;

  @Value("${cardano.rosetta.SEARCH_PAGE_SIZE}")
  Long PAGE_SIZE;

  @Value("${cardano.rosetta.OFFLINE_MODE}")
  boolean offlineMode;

  @Override
  public ResponseEntity<SearchTransactionsResponse> searchTransactions(
      SearchTransactionsRequest searchTransactionsRequest) {
    if (offlineMode) {
      throw ExceptionFactory.notSupportedInOfflineMode();
    }

    networkService.verifyNetworkRequest(searchTransactionsRequest.getNetworkIdentifier());

    Long pageSize = Optional.ofNullable(searchTransactionsRequest.getLimit())
            .orElse(PAGE_SIZE);

    if (pageSize > PAGE_SIZE) {
      log.warn("Requested pageSize {} exceeds maximum allowed size {}. Limiting to maximum size.", pageSize, PAGE_SIZE);
        throw ExceptionFactory.invalidPageSize(pageSize, PAGE_SIZE);
    }

    Long offset = Optional.ofNullable(searchTransactionsRequest.getOffset())
            .orElse(0L);

    Page<BlockTransaction> blockTransactionsSlice = searchService.searchTransaction(searchTransactionsRequest, offset, pageSize);

    Long nextOffset = calculateNextOffset(offset, pageSize, blockTransactionsSlice);

    long totalElementsCount = blockTransactionsSlice.getTotalElements();

    SearchTransactionsResponse searchResponse = searchMapper.mapToSearchTransactionsResponse(blockTransactionsSlice.getContent(), nextOffset, totalElementsCount);

    return ResponseEntity.ok(searchResponse);
  }

  @Nullable
  private static Long calculateNextOffset(Long offset, Long pageSize, Slice<BlockTransaction> blockTransactionsSlice) {
    if (blockTransactionsSlice.isLast()) {
      return null;
    }

    return offset + pageSize;
  }

}
