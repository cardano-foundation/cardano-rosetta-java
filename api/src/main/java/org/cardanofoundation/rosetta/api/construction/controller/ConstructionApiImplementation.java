package org.cardanofoundation.rosetta.api.construction.controller;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.openapitools.client.api.ConstructionApi;
import org.openapitools.client.model.*;

import org.cardanofoundation.rosetta.api.construction.service.ConstructionApiService;
import org.cardanofoundation.rosetta.api.network.service.NetworkService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ConstructionApiImplementation implements ConstructionApi {

    private final ConstructionApiService constructionApiService;
    private final NetworkService networkService;

    @Override
    public ResponseEntity<ConstructionCombineResponse> constructionCombine(@RequestBody ConstructionCombineRequest constructionCombineRequest) {
        networkService.verifyNetworkRequest(constructionCombineRequest.getNetworkIdentifier());
        return ResponseEntity.ok(constructionApiService.constructionCombineService(constructionCombineRequest));
    }

    @Override
    public ResponseEntity<ConstructionDeriveResponse> constructionDerive(@RequestBody ConstructionDeriveRequest constructionDeriveRequest) {
        networkService.verifyNetworkRequest(constructionDeriveRequest.getNetworkIdentifier());
        return ResponseEntity.ok(constructionApiService.constructionDeriveService(constructionDeriveRequest));
    }

    @Override
    public ResponseEntity<TransactionIdentifierResponse> constructionHash(@RequestBody ConstructionHashRequest constructionHashRequest) {
        networkService.verifyNetworkRequest(constructionHashRequest.getNetworkIdentifier());
        return ResponseEntity.ok(constructionApiService.constructionHashService(constructionHashRequest));
    }

    @Override
    public ResponseEntity<ConstructionMetadataResponse> constructionMetadata(@RequestBody ConstructionMetadataRequest constructionMetadataRequest)  {
        networkService.verifyNetworkRequest(constructionMetadataRequest.getNetworkIdentifier());
        return ResponseEntity.ok(constructionApiService.constructionMetadataService(constructionMetadataRequest));
    }

    @Override
    public ResponseEntity<ConstructionParseResponse> constructionParse(@RequestBody @Valid ConstructionParseRequest constructionParseRequest) {
        networkService.verifyNetworkRequest(constructionParseRequest.getNetworkIdentifier());
        return ResponseEntity.ok(constructionApiService.constructionParseService(constructionParseRequest));
    }

    @Override
    public ResponseEntity<ConstructionPayloadsResponse> constructionPayloads(@RequestBody @Validated ConstructionPayloadsRequest constructionPayloadsRequest) {
        networkService.verifyNetworkRequest(constructionPayloadsRequest.getNetworkIdentifier());
        constructionApiService.verifyProtocolParameters(constructionPayloadsRequest);

        return ResponseEntity.ok(constructionApiService.constructionPayloadsService(constructionPayloadsRequest));
    }

    @Override
    public ResponseEntity<ConstructionPreprocessResponse> constructionPreprocess(@RequestBody ConstructionPreprocessRequest constructionPreprocessRequest) {
        networkService.verifyNetworkRequest(constructionPreprocessRequest.getNetworkIdentifier());
        return ResponseEntity.ok(constructionApiService.constructionPreprocessService(constructionPreprocessRequest));
    }

    @Override
    public ResponseEntity<TransactionIdentifierResponse> constructionSubmit(@RequestBody ConstructionSubmitRequest constructionSubmitRequest) {
        networkService.verifyNetworkRequest(constructionSubmitRequest.getNetworkIdentifier());
        return ResponseEntity.ok(constructionApiService.constructionSubmitService(constructionSubmitRequest));
    }


}
