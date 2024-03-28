package org.cardanofoundation.rosetta.api.construction.service;


import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import org.openapitools.client.model.*;

import java.io.IOException;

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
