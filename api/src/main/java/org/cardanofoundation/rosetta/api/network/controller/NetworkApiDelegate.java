package org.cardanofoundation.rosetta.api.network.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.openapitools.client.model.*;

import org.cardanofoundation.rosetta.common.exception.ServerException;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:33
 */
@Tag(name = "v1-api", description = "The Cardano Network API")
public interface NetworkApiDelegate {

  @Operation(
          operationId = "networkList",
          summary = "Network endpoints are used when first connecting to a Rosetta endpoint to determine which network and sub-networks are supported.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json")),
                  @ApiResponse(responseCode = "400", description = "Invalid `body`")
          }
  )
  @PostMapping(
          value = "/network/list",
          produces = { "application/json;charset=utf-8" },
          consumes = { "application/json;charset=utf-8" }
  )
  ResponseEntity<NetworkListResponse> networkList(MetadataRequest metadataRequest)
      throws IOException;

  @Operation(
          operationId = "networkOptions",
          summary = "This endpoint returns the version information and allowed network-specific types for a NetworkIdentifier.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json")),
                  @ApiResponse(responseCode = "400", description = "Invalid `body`")
          }
  )
  @PostMapping(
          value = "/network/options",
          produces = { "application/json;charset=utf-8" },
          consumes = { "application/json;charset=utf-8" }
  )
  ResponseEntity<NetworkOptionsResponse> networkOptions(NetworkRequest networkRequest)
      throws IOException, InterruptedException;

  @Operation(
          operationId = "networkStatus",
          summary = "This endpoint returns the current status of the network requested.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json")),
                  @ApiResponse(responseCode = "400", description = "Invalid `body`")
          }
  )
  @PostMapping(
          value = "/network/status",
          produces = { "application/json;charset=utf-8" },
          consumes = { "application/json;charset=utf-8" }
  )
  ResponseEntity<NetworkStatusResponse> networkStatus(NetworkRequest networkRequest)
      throws ServerException, IOException;
}
