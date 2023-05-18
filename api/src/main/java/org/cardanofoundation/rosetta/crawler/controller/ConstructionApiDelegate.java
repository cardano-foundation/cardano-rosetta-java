package org.cardanofoundation.rosetta.crawler.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionCombineRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionCombineResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionDeriveRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionDeriveResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionHashRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionMetadataRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionMetadataResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionParseRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionParseResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionPayloadsRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionPayloadsResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionPreprocessRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionPreprocessResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionSubmitRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.TransactionIdentifierResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:11
 */
@Tag(name = "v1-api", description = "The Cardano Construction API")
public interface ConstructionApiDelegate {
  @Operation(
          operationId = "constructionCombine",
          summary = "Combine creates a network-specific transaction from an unsigned transaction and an array of provided signatures.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json")),
                  @ApiResponse(responseCode = "400", description = "Invalid `body`")
          }
  )
  @RequestMapping(
          method = RequestMethod.POST,
          value = "/construction/combine",
          produces = { "application/json;charset=utf-8" },
          consumes = { "application/json;charset=utf-8" }
  )
  ResponseEntity<ConstructionCombineResponse> constructionCombine(
      ConstructionCombineRequest constructionCombineRequest);

  @Operation(
          operationId = "constructionDerive",
          summary = "Derive returns the AccountIdentifier associated with a public key.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json")),
                  @ApiResponse(responseCode = "400", description = "Invalid `body`")
          }
  )
  @RequestMapping(
          method = RequestMethod.POST,
          value = "/construction/derive",
          produces = { "application/json;charset=utf-8" },
          consumes = { "application/json;charset=utf-8" }
  )
  ResponseEntity<ConstructionDeriveResponse> constructionDerive(
      ConstructionDeriveRequest constructionDeriveRequest);

  @Operation(
          operationId = "constructionHash",
          summary = "TransactionHash returns the network-specific transaction hash for a signed transaction..",
          responses = {
                  @ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json")),
                  @ApiResponse(responseCode = "400", description = "Invalid `body`")
          }
  )
  @RequestMapping(
          method = RequestMethod.POST,
          value = "/construction/hash",
          produces = { "application/json;charset=utf-8" },
          consumes = { "application/json;charset=utf-8" }
  )
  ResponseEntity<TransactionIdentifierResponse> constructionHash(
      ConstructionHashRequest constructionHashRequest);

  @Operation(
          operationId = "constructionMetadata",
          summary = "Get any information required to construct a transaction for a specific network.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json")),
                  @ApiResponse(responseCode = "400", description = "Invalid `body`")
          }
  )
  @RequestMapping(
          method = RequestMethod.POST,
          value = "/construction/metadata",
          produces = { "application/json;charset=utf-8" },
          consumes = { "application/json;charset=utf-8" }
  )
  ResponseEntity<ConstructionMetadataResponse> constructionMetadata(
      ConstructionMetadataRequest constructionMetadataRequest);

  @Operation(
          operationId = "constructionParse",
          summary = "Parse is called on both unsigned and signed transactions to understand the intent of the formulated transaction.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json")),
                  @ApiResponse(responseCode = "400", description = "Invalid `body`")
          }
  )
  @RequestMapping(
          method = RequestMethod.POST,
          value = "/construction/parse",
          produces = { "application/json;charset=utf-8" },
          consumes = { "application/json;charset=utf-8" }
  )
  ResponseEntity<ConstructionParseResponse> constructionParse(
      ConstructionParseRequest constructionParseRequest);

  @Operation(
          operationId = "constructionPayloads",
          summary = "Payloads is called with an array of operations and the response from /construction/metadata.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json")),
                  @ApiResponse(responseCode = "400", description = "Invalid `body`")
          }
  )
  @RequestMapping(
          method = RequestMethod.POST,
          value = "/construction/payloads",
          produces = { "application/json;charset=utf-8" },
          consumes = { "application/json;charset=utf-8" }
  )
  ResponseEntity<ConstructionPayloadsResponse> constructionPayloads(
      ConstructionPayloadsRequest constructionPayloadsRequest);

  @Operation(
          operationId = "constructionPreprocess",
          summary = "Preprocess is called prior to /construction/payloads to construct a request for any metadata that is needed for transaction construction given (i.e. account nonce).",
          responses = {
                  @ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json")),
                  @ApiResponse(responseCode = "400", description = "Invalid `body`")
          }
  )
  @RequestMapping(
          method = RequestMethod.POST,
          value = "/construction/preprocess",
          produces = { "application/json;charset=utf-8" },
          consumes = { "application/json;charset=utf-8" }
  )
  ResponseEntity<ConstructionPreprocessResponse> constructionPreprocess(
      ConstructionPreprocessRequest constructionPreprocessRequest);

  @Operation(
          operationId = "constructionSubmit",
          summary = "Submit a pre-signed transaction to the node.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json")),
                  @ApiResponse(responseCode = "400", description = "Invalid `body`")
          }
  )
  @RequestMapping(
          method = RequestMethod.POST,
          value = "/construction/submit",
          produces = { "application/json;charset=utf-8" },
          consumes = { "application/json;charset=utf-8" }
  )
  ResponseEntity<TransactionIdentifierResponse> constructionSubmit(
      ConstructionSubmitRequest constructionSubmitRequest);
}
