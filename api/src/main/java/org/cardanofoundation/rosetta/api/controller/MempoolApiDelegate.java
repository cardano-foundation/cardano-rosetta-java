package org.cardanofoundation.rosetta.api.controller;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.cardanofoundation.rosetta.api.model.rest.MempoolResponse;
import org.cardanofoundation.rosetta.api.model.rest.MempoolTransactionRequest;
import org.cardanofoundation.rosetta.api.model.rest.MempoolTransactionResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.net.UnknownHostException;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:31
 */
@Tag(name = "v1-api", description = "The Cardano Mempool API")
public interface MempoolApiDelegate {

  @Operation(
          operationId = "mempool",
          summary = "Mempool endpoints are used to fetch any data stored in the mempool.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json")),
                  @ApiResponse(responseCode = "400", description = "Invalid `body`")
          }
  )
  @RequestMapping(
          method = RequestMethod.POST,
          value = "/mempool",
          produces = { "application/json;charset=utf-8" },
          consumes = { "application/json;charset=utf-8" }
  )
  ResponseEntity<MempoolResponse> mempool(NetworkRequest networkRequest);

  @Operation(
          operationId = "mempoolTransaction",
          summary = "Get a transaction in the mempool by its Transaction Identifier.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json")),
                  @ApiResponse(responseCode = "400", description = "Invalid `body`")
          }
  )
  @RequestMapping(
          method = RequestMethod.POST,
          value = "/mempool/transaction",
          produces = { "application/json;charset=utf-8" },
          consumes = { "application/json;charset=utf-8" }
  )
  ResponseEntity<MempoolTransactionResponse> mempoolTransaction(
      MempoolTransactionRequest mempoolTransactionRequest)
        throws UnknownHostException, CborException, AddressExcepion,
          CborDeserializationException, CborSerializationException,
          JsonProcessingException;
}
