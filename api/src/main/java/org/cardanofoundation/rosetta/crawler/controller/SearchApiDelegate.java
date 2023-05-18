package org.cardanofoundation.rosetta.crawler.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.cardanofoundation.rosetta.crawler.model.rest.SearchTransactionsRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.SearchTransactionsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:37
 */
@Tag(name = "v1-api", description = "The Cardano Search API")
public interface SearchApiDelegate {

  @Operation(
          operationId = "searchTransactions",
          summary = "/search/transactions allows the caller to search for transactions that meet certain conditions.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json")),
                  @ApiResponse(responseCode = "400", description = "Invalid `body`")
          }
  )
  @RequestMapping(
          method = RequestMethod.POST,
          value = "/search/transactions",
          produces = { "application/json;charset=utf-8" },
          consumes = { "application/json;charset=utf-8" }
  )
  ResponseEntity<SearchTransactionsResponse> searchTransactions(
      SearchTransactionsRequest searchTransactionsRequest);
}
