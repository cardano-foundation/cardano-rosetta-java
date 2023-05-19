package org.cardanofoundation.rosetta.api.controller;

import lombok.extern.log4j.Log4j2;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
public class ConstructionApiDelegateImplementation implements ConstructionApiDelegate {
    @Override
    public ResponseEntity<ConstructionCombineResponse> constructionCombine(
        ConstructionCombineRequest constructionCombineRequest) {
        return null;
    }

    @Override
    public ResponseEntity<ConstructionDeriveResponse> constructionDerive(
        ConstructionDeriveRequest constructionDeriveRequest) {
        return null;
    }

    @Override
    public ResponseEntity<TransactionIdentifierResponse> constructionHash(
        ConstructionHashRequest constructionHashRequest) {
        return null;
    }

    @Override
    public ResponseEntity<ConstructionMetadataResponse> constructionMetadata(
        ConstructionMetadataRequest constructionMetadataRequest) {
        return null;
    }

    @Override
    public ResponseEntity<ConstructionParseResponse> constructionParse(
        ConstructionParseRequest constructionParseRequest) {
        return null;
    }

    @Override
    public ResponseEntity<ConstructionPayloadsResponse> constructionPayloads(
        ConstructionPayloadsRequest constructionPayloadsRequest) {
        return null;
    }

    @Override
    public ResponseEntity<ConstructionPreprocessResponse> constructionPreprocess(
        ConstructionPreprocessRequest constructionPreprocessRequest) {
        return null;
    }

    @Override
    public ResponseEntity<TransactionIdentifierResponse> constructionSubmit(
        ConstructionSubmitRequest constructionSubmitRequest) {
        return null;
    }
}
