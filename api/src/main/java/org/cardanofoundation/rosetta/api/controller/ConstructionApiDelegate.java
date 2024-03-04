package org.cardanofoundation.rosetta.api.controller;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import java.net.UnknownHostException;

import org.cardanofoundation.rosetta.api.model.rest.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Sotatek-HoangNguyen9
 * @since 12/04/2023 17:11
 */


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
            ConstructionCombineRequest constructionCombineRequest) throws CborException, CborSerializationException, JsonProcessingException;

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
            ConstructionDeriveRequest constructionDeriveRequest) throws IllegalAccessException, CborSerializationException;

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
            ConstructionMetadataRequest constructionMetadataRequest) throws CborException, CborSerializationException;

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
            ConstructionParseRequest constructionParseRequest)
            throws Exception;

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
            ConstructionPayloadsRequest constructionPayloadsRequest) throws Exception;

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
            ConstructionPreprocessRequest constructionPreprocessRequest)
            throws IOException, AddressExcepion, CborSerializationException, CborException;

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
            ConstructionSubmitRequest constructionSubmitRequest)
            throws CborDeserializationException, CborSerializationException, InterruptedException;

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/construction/signingPayloads",
            produces = { "application/json;charset=utf-8" },
            consumes = { "application/json;charset=utf-8" }
    )
    ResponseEntity<SigningPayloadsResponse> constructionSigningPayloads(
            SigningPayloadsRequest constructionSubmitRequest);
}
