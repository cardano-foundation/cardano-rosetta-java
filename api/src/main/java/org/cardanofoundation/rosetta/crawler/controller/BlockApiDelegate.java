package org.cardanofoundation.rosetta.crawler.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.cardanofoundation.rosetta.crawler.model.rest.BlockRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.BlockResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.BlockTransactionRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.BlockTransactionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 16:54
 */
@Validated
@Tag(name = "v1-api", description = "The Cardano Block API")
public interface BlockApiDelegate {
  @Operation(
          operationId = "getBlockByBlockIdentifier",
          summary = "Get a block by its Block Identifier",
          responses = {
                  @ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json")),
                  @ApiResponse(responseCode = "400", description = "Invalid `body`")
          }
  )
  @RequestMapping(
          method = RequestMethod.POST,
          value = "/block",
          produces = { "application/json;charset=utf-8" },
          consumes = { "application/json;charset=utf-8" }
  )
  ResponseEntity<BlockResponse> block(BlockRequest blockRequest);

  @Operation(
          operationId = "getBlockTransaction",
          summary = "Get a transaction in a block by its Transaction Identifier",
          responses = {
                  @ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json")),
                  @ApiResponse(responseCode = "400", description = "Invalid `body`")
          }
  )
  @RequestMapping(
          method = RequestMethod.POST,
          value = "/block/transaction",
          produces = { "application/json;charset=utf-8" },
          consumes = { "application/json;charset=utf-8" }
  )
  ResponseEntity<BlockTransactionResponse> blockTransaction(BlockTransactionRequest blockTransactionRequest);
}
