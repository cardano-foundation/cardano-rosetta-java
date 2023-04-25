package org.cardanofoundation.rosetta.api.controller;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.rosetta.api.addedRepo.BlockRepository;
import org.cardanofoundation.rosetta.api.constructionApiService.CheckService;
import org.cardanofoundation.rosetta.api.constructionApiService.ConstructionApiService;
import org.cardanofoundation.rosetta.api.constructionApiService.impl.CheckServiceImpl;
import org.cardanofoundation.rosetta.api.model.rest.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.io.IOException;


@RestController
public class ConstructionApiDelegateImplementation implements ConstructionApiDelegate {
    @Autowired
    ConstructionApiService constructionApiService;

    @Autowired
    CheckService checkService;
    @Override
    public ResponseEntity<ConstructionCombineResponse> constructionCombine(@RequestBody ConstructionCombineRequest constructionCombineRequest) throws CborException, CborSerializationException, JsonProcessingException {
        checkService.withNetworkValidation(constructionCombineRequest.getNetworkIdentifier());
        return ResponseEntity.ok(constructionApiService.constructionCombineService(constructionCombineRequest));
    }

    @GetMapping(value = "/")
    public String get(){
        return "ok";
    }
    @Override
    public ResponseEntity<ConstructionDeriveResponse> constructionDerive(@RequestBody ConstructionDeriveRequest constructionDeriveRequest) throws IllegalAccessException {
        checkService.withNetworkValidation(constructionDeriveRequest.getNetworkIdentifier());
        return ResponseEntity.ok(constructionApiService.constructionDeriveService(constructionDeriveRequest));
    }

    @Override
    public ResponseEntity<TransactionIdentifierResponse> constructionHash(@RequestBody ConstructionHashRequest constructionHashRequest) {
        checkService.withNetworkValidation(constructionHashRequest.getNetworkIdentifier());
        return ResponseEntity.ok(constructionApiService.constructionHashService(constructionHashRequest));
    }

    @Override
    public ResponseEntity<ConstructionMetadataResponse> constructionMetadata(@RequestBody ConstructionMetadataRequest constructionMetadataRequest) throws CborException, CborSerializationException {
        checkService.withNetworkValidation(constructionMetadataRequest.getNetworkIdentifier());
        return ResponseEntity.ok(constructionApiService.constructionMetadataService(constructionMetadataRequest));
    }

    @Override
    public ResponseEntity<ConstructionParseResponse> constructionParse(@RequestBody ConstructionParseRequest constructionParseRequest) {
        checkService.withNetworkValidation(constructionParseRequest.getNetworkIdentifier());
       return ResponseEntity.ok(constructionApiService.constructionParseService(constructionParseRequest));
    }

    @Override
    public ResponseEntity<ConstructionPayloadsResponse> constructionPayloads(@RequestBody ConstructionPayloadsRequest constructionPayloadsRequest) throws IOException, CborException, CborSerializationException, AddressExcepion {
        checkService.withNetworkValidation(constructionPayloadsRequest.getNetworkIdentifier());
        return ResponseEntity.ok(constructionApiService.constructionPayloadsService(constructionPayloadsRequest));
    }

    @Override
    public ResponseEntity<ConstructionPreprocessResponse> constructionPreprocess(@RequestBody ConstructionPreprocessRequest constructionPreprocessRequest) throws IOException, AddressExcepion, CborSerializationException {
        checkService.withNetworkValidation(constructionPreprocessRequest.getNetworkIdentifier());
        return ResponseEntity.ok(constructionApiService.constructionPreprocessService(constructionPreprocessRequest));
    }

    @Override
    public ResponseEntity<TransactionIdentifierResponse> constructionSubmit(@RequestBody ConstructionSubmitRequest constructionSubmitRequest) throws CborDeserializationException, CborSerializationException {
        checkService.withNetworkValidation(constructionSubmitRequest.getNetworkIdentifier());
        return ResponseEntity.ok(constructionApiService.constructionSubmitService(constructionSubmitRequest));
    }
}
