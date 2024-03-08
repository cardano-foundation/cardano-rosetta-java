package org.cardanofoundation.rosetta.api.service;


import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.openapitools.client.model.*;

import java.io.IOException;

public interface ConstructionApiService {
    ConstructionDeriveResponse constructionDeriveService(
            ConstructionDeriveRequest constructionDeriveRequest) throws IllegalAccessException, CborSerializationException;
    ConstructionPreprocessResponse constructionPreprocessService(
            ConstructionPreprocessRequest constructionPreprocessRequest)
            throws IOException, AddressExcepion, CborSerializationException, CborException;

    ConstructionMetadataResponse constructionMetadataService(
            ConstructionMetadataRequest constructionMetadataRequest) throws CborException, CborSerializationException;

    ConstructionPayloadsResponse constructionPayloadsService(
            ConstructionPayloadsRequest constructionPayloadsRequest) throws Exception;

    ConstructionParseResponse constructionParseService(
            ConstructionParseRequest constructionParseRequest)
            throws Exception;

    ConstructionCombineResponse constructionCombineService(
            ConstructionCombineRequest constructionCombineRequest) throws CborException, CborSerializationException, JsonProcessingException;

    TransactionIdentifierResponse constructionHashService(
            ConstructionHashRequest constructionHashRequest);

    TransactionIdentifierResponse constructionSubmitService(
            ConstructionSubmitRequest constructionSubmitRequest)
            throws CborDeserializationException, CborSerializationException, InterruptedException;
}
