package org.cardanofoundation.rosetta.api.search.controller;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.openapitools.client.api.SearchApi;
import org.openapitools.client.model.BlockTransaction;
import org.openapitools.client.model.SearchTransactionsRequest;
import org.openapitools.client.model.SearchTransactionsResponse;

import org.cardanofoundation.rosetta.api.network.service.NetworkService;
import org.cardanofoundation.rosetta.api.search.mapper.SearchMapper;
import org.cardanofoundation.rosetta.api.search.service.SearchService;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;

@RestController
@RequiredArgsConstructor
public class SearchApiImpl implements SearchApi {

  private final NetworkService networkService;
  private final SearchService searchService;
  private final SearchMapper searchMapper;

  @Value("${cardano.rosetta.SEARCH_PAGE_SIZE}")
  private Long PAGE_SIZE;

  @Value("${cardano.rosetta.OFFLINE_MODE}")
  private boolean offlineMode;

  @Override
  public ResponseEntity<SearchTransactionsResponse> searchTransactions(
      SearchTransactionsRequest searchTransactionsRequest) {
    if (offlineMode) {
      throw ExceptionFactory.notSupportedInOfflineMode();
    }
    networkService.verifyNetworkRequest(searchTransactionsRequest.getNetworkIdentifier());

    Long pageSize = Optional.ofNullable(searchTransactionsRequest.getLimit()).orElse(PAGE_SIZE);
    pageSize = pageSize > PAGE_SIZE ? PAGE_SIZE : pageSize;
    Long offset = Optional.ofNullable(searchTransactionsRequest.getOffset()).orElse(0L);
    Long nextOffset = offset + pageSize;

    List<BlockTransaction> blockTransactions = searchService.searchTransaction(
        searchTransactionsRequest, offset, pageSize);

    return ResponseEntity.ok(searchMapper.mapToSearchTransactionsResponse(
        blockTransactions, nextOffset));
  }

}
