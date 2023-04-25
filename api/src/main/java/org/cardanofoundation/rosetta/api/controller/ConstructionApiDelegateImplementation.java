package org.cardanofoundation.rosetta.api.controller;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.rosetta.api.addedRepo.BlockRepository;
import org.cardanofoundation.rosetta.api.constructionApiService.ConstructionApiService;
import org.cardanofoundation.rosetta.api.constructionApiService.impl.CheckServiceImpl;
import org.cardanofoundation.rosetta.api.model.rest.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.io.IOException;

@Log4j2
@RestController
public class ConstructionApiDelegateImplementation implements ConstructionApiDelegate {
    private final CheckServiceImpl checkService;

    private final ConstructionApiService constructionApiService;

    @Autowired
    public ConstructionApiDelegateImplementation(CheckServiceImpl checkService, ConstructionApiService constructionApiService1) {
        this.checkService = checkService;
        this.constructionApiService = constructionApiService1;
    }
    @Override
    public ResponseEntity<ConstructionCombineResponse> constructionCombine(ConstructionCombineRequest constructionCombineRequest) throws CborException, CborSerializationException, JsonProcessingException {
        checkService.withNetworkValidation(constructionCombineRequest.getNetworkIdentifier());
        return ResponseEntity.ok(constructionApiService.constructionCombineService(constructionCombineRequest));
    }

    @Override
    public ResponseEntity<ConstructionDeriveResponse> constructionDerive(@RequestBody ConstructionDeriveRequest constructionDeriveRequest) throws IllegalAccessException {
        checkService.withNetworkValidation(constructionDeriveRequest.getNetworkIdentifier());
        return ResponseEntity.ok(constructionApiService.constructionDeriveService(constructionDeriveRequest));
    }

    @Override
    public ResponseEntity<TransactionIdentifierResponse> constructionHash(ConstructionHashRequest constructionHashRequest) {
        checkService.withNetworkValidation(constructionHashRequest.getNetworkIdentifier());
        return ResponseEntity.ok(constructionApiService.constructionHashService(constructionHashRequest));
    }

    @Override
    public ResponseEntity<ConstructionMetadataResponse> constructionMetadata(ConstructionMetadataRequest constructionMetadataRequest) throws CborException, CborSerializationException {
        checkService.withNetworkValidation(constructionMetadataRequest.getNetworkIdentifier());
        return ResponseEntity.ok(constructionApiService.constructionMetadataService(constructionMetadataRequest));
    }

    @Override
    public ResponseEntity<ConstructionParseResponse> constructionParse(ConstructionParseRequest constructionParseRequest) {
        checkService.withNetworkValidation(constructionParseRequest.getNetworkIdentifier());
       return ResponseEntity.ok(constructionApiService.constructionParseService(constructionParseRequest));
    }

    @Override
    public ResponseEntity<ConstructionPayloadsResponse> constructionPayloads(ConstructionPayloadsRequest constructionPayloadsRequest) throws IOException, CborException, CborSerializationException, AddressExcepion {
        checkService.withNetworkValidation(constructionPayloadsRequest.getNetworkIdentifier());
        return ResponseEntity.ok(constructionApiService.constructionPayloadsService(constructionPayloadsRequest));
    }

    @Override
    public ResponseEntity<ConstructionPreprocessResponse> constructionPreprocess(ConstructionPreprocessRequest constructionPreprocessRequest) throws IOException, AddressExcepion, CborSerializationException {
        checkService.withNetworkValidation(constructionPreprocessRequest.getNetworkIdentifier());
        return ResponseEntity.ok(constructionApiService.constructionPreprocessService(constructionPreprocessRequest));
    }

    @Override
    public ResponseEntity<TransactionIdentifierResponse> constructionSubmit(ConstructionSubmitRequest constructionSubmitRequest) throws CborDeserializationException, CborSerializationException {
        checkService.withNetworkValidation(constructionSubmitRequest.getNetworkIdentifier());
        return ResponseEntity.ok(constructionApiService.constructionSubmitService(constructionSubmitRequest));
    }
}
