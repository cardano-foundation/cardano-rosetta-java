package org.cardanofoundation.rosetta.api.controller;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.UnknownHostException;
import org.cardanofoundation.rosetta.api.service.ConstructionApiService;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionCombineRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionCombineResponse;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionDeriveRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionDeriveResponse;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionHashRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionMetadataRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionMetadataResponse;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionParseRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionParseResponse;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionPayloadsRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionPayloadsResponse;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionPreprocessRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionPreprocessResponse;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionSubmitRequest;
import org.cardanofoundation.rosetta.api.model.rest.TransactionIdentifierResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


@RestController
public class ConstructionApiDelegateImplementation implements ConstructionApiDelegate {
    @Autowired
    ConstructionApiService constructionApiService;
    @Override
    public ResponseEntity<ConstructionCombineResponse> constructionCombine(@RequestBody ConstructionCombineRequest constructionCombineRequest) throws CborException, CborSerializationException, JsonProcessingException {
        return ResponseEntity.ok(constructionApiService.constructionCombineService(constructionCombineRequest));
    }

    @Override
    public ResponseEntity<ConstructionDeriveResponse> constructionDerive(@RequestBody ConstructionDeriveRequest constructionDeriveRequest) throws IllegalAccessException, CborSerializationException {
       return ResponseEntity.ok(constructionApiService.constructionDeriveService(constructionDeriveRequest));
    }

    @Override
    public ResponseEntity<TransactionIdentifierResponse> constructionHash(@RequestBody ConstructionHashRequest constructionHashRequest) {
       return ResponseEntity.ok(constructionApiService.constructionHashService(constructionHashRequest));
    }

    @Override
    public ResponseEntity<ConstructionMetadataResponse> constructionMetadata(@RequestBody ConstructionMetadataRequest constructionMetadataRequest) throws CborException, CborSerializationException {
       return ResponseEntity.ok(constructionApiService.constructionMetadataService(constructionMetadataRequest));
    }

    @Override
    public ResponseEntity<ConstructionParseResponse> constructionParse(@RequestBody ConstructionParseRequest constructionParseRequest)
        throws UnknownHostException, AddressExcepion, CborDeserializationException, JsonProcessingException {
      return ResponseEntity.ok(constructionApiService.constructionParseService(constructionParseRequest));
    }

    @Override
    public ResponseEntity<ConstructionPayloadsResponse> constructionPayloads(@RequestBody ConstructionPayloadsRequest constructionPayloadsRequest) throws IOException, CborException, CborSerializationException, AddressExcepion {
       return ResponseEntity.ok(constructionApiService.constructionPayloadsService(constructionPayloadsRequest));
    }

    @Override
    public ResponseEntity<ConstructionPreprocessResponse> constructionPreprocess(@RequestBody ConstructionPreprocessRequest constructionPreprocessRequest)
        throws IOException, AddressExcepion, CborSerializationException, CborException {
       return ResponseEntity.ok(constructionApiService.constructionPreprocessService(constructionPreprocessRequest));
    }

    @Override
    public ResponseEntity<TransactionIdentifierResponse> constructionSubmit(@RequestBody ConstructionSubmitRequest constructionSubmitRequest) throws CborDeserializationException, CborSerializationException {
        return ResponseEntity.ok(constructionApiService.constructionSubmitService(constructionSubmitRequest));
    }
}
