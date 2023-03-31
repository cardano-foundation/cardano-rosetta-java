package org.cardanofoundation.rosetta.api.controller;

import lombok.extern.log4j.Log4j2;
import org.cardanofoundation.rosetta.api.model.rest.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class ConstructionApiDelegateImplementation implements ConstructionApiDelegate {
    @Override
    public ResponseEntity<ConstructionCombineResponse> constructionCombine(ConstructionCombineRequest constructionCombineRequest) {
        return ConstructionApiDelegate.super.constructionCombine(constructionCombineRequest);
    }

    @Override
    public ResponseEntity<ConstructionDeriveResponse> constructionDerive(ConstructionDeriveRequest constructionDeriveRequest) {
        return ConstructionApiDelegate.super.constructionDerive(constructionDeriveRequest);
    }

    @Override
    public ResponseEntity<TransactionIdentifierResponse> constructionHash(ConstructionHashRequest constructionHashRequest) {
        return ConstructionApiDelegate.super.constructionHash(constructionHashRequest);
    }

    @Override
    public ResponseEntity<ConstructionMetadataResponse> constructionMetadata(ConstructionMetadataRequest constructionMetadataRequest) {
        return ConstructionApiDelegate.super.constructionMetadata(constructionMetadataRequest);
    }

    @Override
    public ResponseEntity<ConstructionParseResponse> constructionParse(ConstructionParseRequest constructionParseRequest) {
        return ConstructionApiDelegate.super.constructionParse(constructionParseRequest);
    }

    @Override
    public ResponseEntity<ConstructionPayloadsResponse> constructionPayloads(ConstructionPayloadsRequest constructionPayloadsRequest) {
        return ConstructionApiDelegate.super.constructionPayloads(constructionPayloadsRequest);
    }

    @Override
    public ResponseEntity<ConstructionPreprocessResponse> constructionPreprocess(ConstructionPreprocessRequest constructionPreprocessRequest) {
        return ConstructionApiDelegate.super.constructionPreprocess(constructionPreprocessRequest);
    }

    @Override
    public ResponseEntity<TransactionIdentifierResponse> constructionSubmit(ConstructionSubmitRequest constructionSubmitRequest) {
        return ConstructionApiDelegate.super.constructionSubmit(constructionSubmitRequest);
    }
}
