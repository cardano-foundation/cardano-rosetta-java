package org.cardanofoundation.rosetta.crawler.controller;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.cardanofoundation.rosetta.crawler.service.construction.CheckService;
import org.cardanofoundation.rosetta.crawler.service.construction.ConstructionApiService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

    @Override
    public ResponseEntity<ConstructionDeriveResponse> constructionDerive(@RequestBody ConstructionDeriveRequest constructionDeriveRequest) throws IllegalAccessException, CborSerializationException {
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
//        checkService.withNetworkValidation(constructionPayloadsRequest.getNetworkIdentifier());
        return ResponseEntity.ok(constructionApiService.constructionPayloadsService(constructionPayloadsRequest));
    }

    @Override
    public ResponseEntity<ConstructionPreprocessResponse> constructionPreprocess(@RequestBody ConstructionPreprocessRequest constructionPreprocessRequest)
        throws IOException, AddressExcepion, CborSerializationException, CborException {
        checkService.withNetworkValidation(constructionPreprocessRequest.getNetworkIdentifier());
        return ResponseEntity.ok(constructionApiService.constructionPreprocessService(constructionPreprocessRequest));
    }

    @Override
    public ResponseEntity<TransactionIdentifierResponse> constructionSubmit(@RequestBody ConstructionSubmitRequest constructionSubmitRequest) throws CborDeserializationException, CborSerializationException {
        checkService.withNetworkValidation(constructionSubmitRequest.getNetworkIdentifier());
        return ResponseEntity.ok(constructionApiService.constructionSubmitService(constructionSubmitRequest));
    }
}
