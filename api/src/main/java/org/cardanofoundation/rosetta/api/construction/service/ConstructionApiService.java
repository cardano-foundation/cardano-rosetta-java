package org.cardanofoundation.rosetta.api.construction.service;


import org.openapitools.client.model.*;


public interface ConstructionApiService {
    ConstructionDeriveResponse constructionDeriveService(
            ConstructionDeriveRequest constructionDeriveRequest);
    ConstructionPreprocessResponse constructionPreprocessService(
            ConstructionPreprocessRequest constructionPreprocessRequest);

    ConstructionMetadataResponse constructionMetadataService(
            ConstructionMetadataRequest constructionMetadataRequest);

    ConstructionPayloadsResponse constructionPayloadsService(
            ConstructionPayloadsRequest constructionPayloadsRequest);

    ConstructionParseResponse constructionParseService(
            ConstructionParseRequest constructionParseRequest);

    ConstructionCombineResponse constructionCombineService(
            ConstructionCombineRequest constructionCombineRequest);

    TransactionIdentifierResponse constructionHashService(
            ConstructionHashRequest constructionHashRequest);

    TransactionIdentifierResponse constructionSubmitService(
            ConstructionSubmitRequest constructionSubmitRequest);
}
