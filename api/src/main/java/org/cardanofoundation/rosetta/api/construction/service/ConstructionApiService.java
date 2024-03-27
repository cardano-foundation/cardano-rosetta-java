package org.cardanofoundation.rosetta.api.construction.service;


import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import org.openapitools.client.model.*;

import java.io.IOException;

public interface ConstructionApiService {
    ConstructionDeriveResponse constructionDeriveService(
            ConstructionDeriveRequest constructionDeriveRequest) throws IllegalAccessException;
    ConstructionPreprocessResponse constructionPreprocessService(
            ConstructionPreprocessRequest constructionPreprocessRequest)
            throws IOException, AddressExcepion, CborSerializationException, CborException;

    ConstructionMetadataResponse constructionMetadataService(
            ConstructionMetadataRequest constructionMetadataRequest) throws CborException, CborSerializationException;

    ConstructionPayloadsResponse constructionPayloadsService(
            ConstructionPayloadsRequest constructionPayloadsRequest) throws CborException, AddressExcepion, IOException, CborSerializationException;

    ConstructionParseResponse constructionParseService(
            ConstructionParseRequest constructionParseRequest);

    ConstructionCombineResponse constructionCombineService(
            ConstructionCombineRequest constructionCombineRequest) throws CborException;

    TransactionIdentifierResponse constructionHashService(
            ConstructionHashRequest constructionHashRequest);

    TransactionIdentifierResponse constructionSubmitService(
            ConstructionSubmitRequest constructionSubmitRequest);
}
