package org.cardanofoundation.rosetta.api.controller;

import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.rosetta.api.constructionApiService.ConstructionApiService;
import org.cardanofoundation.rosetta.api.model.rest.*;
import org.cardanofoundation.rosetta.api.model.rest.Error;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.io.IOException;

@Log4j2
@RestController
public class ConstructionApiDelegateImplementation implements ConstructionApiDelegate {
    private final NativeWebRequest request;

    private final ConstructionApiService constructionApiService;


    @Autowired
    public ConstructionApiDelegateImplementation(NativeWebRequest request,ConstructionApiService constructionApiService1) {
        this.request = request;
        this.constructionApiService = constructionApiService1;
    }
    @Override
    public ResponseEntity<ConstructionCombineResponse> constructionCombine(ConstructionCombineRequest constructionCombineRequest) {
        return null;
    }

    @Override
    public ResponseEntity<ConstructionDeriveResponse> constructionDerive(@RequestBody ConstructionDeriveRequest constructionDeriveRequest) throws IllegalAccessException {
        return ResponseEntity.ok(constructionApiService.constructionDeriveService(constructionDeriveRequest));
    }

    @Override
    public ResponseEntity<TransactionIdentifierResponse> constructionHash(ConstructionHashRequest constructionHashRequest) {
        return null;
    }

    @Override
    public ResponseEntity<ConstructionMetadataResponse> constructionMetadata(ConstructionMetadataRequest constructionMetadataRequest) {
        return null;
    }

    @Override
    public ResponseEntity<ConstructionParseResponse> constructionParse(ConstructionParseRequest constructionParseRequest) {
        return null;
    }

    @Override
    public ResponseEntity<ConstructionPayloadsResponse> constructionPayloads(ConstructionPayloadsRequest constructionPayloadsRequest) {
        return null;
    }

    @Override
    public ResponseEntity<ConstructionPreprocessResponse> constructionPreprocess(ConstructionPreprocessRequest constructionPreprocessRequest) throws IOException {
        return ResponseEntity.ok(constructionApiService.constructionPreprocessService(constructionPreprocessRequest));
    }

    @Override
    public ResponseEntity<TransactionIdentifierResponse> constructionSubmit(ConstructionSubmitRequest constructionSubmitRequest) {
        return null;
    }
}
