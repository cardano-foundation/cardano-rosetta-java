package org.cardanofoundation.rosetta.crawler.service.construction;



import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.net.UnknownHostException;
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

public interface ConstructionApiService {
    ConstructionDeriveResponse constructionDeriveService(
        ConstructionDeriveRequest constructionDeriveRequest) throws IllegalAccessException, CborSerializationException;
    ConstructionPreprocessResponse constructionPreprocessService(
        ConstructionPreprocessRequest constructionPreprocessRequest)
        throws IOException, AddressExcepion, CborSerializationException, CborException;

    ConstructionMetadataResponse constructionMetadataService(
        ConstructionMetadataRequest constructionMetadataRequest) throws CborException, CborSerializationException;

    ConstructionPayloadsResponse constructionPayloadsService(
        ConstructionPayloadsRequest constructionPayloadsRequest) throws IOException, CborException, CborSerializationException, AddressExcepion;

    ConstructionParseResponse constructionParseService(
        ConstructionParseRequest constructionParseRequest)
        throws UnknownHostException, AddressExcepion, CborDeserializationException, JsonProcessingException;

    ConstructionCombineResponse constructionCombineService(
        ConstructionCombineRequest constructionCombineRequest) throws CborException, CborSerializationException, JsonProcessingException;

    TransactionIdentifierResponse constructionHashService(
        ConstructionHashRequest constructionHashRequest);

    TransactionIdentifierResponse constructionSubmitService(
        ConstructionSubmitRequest constructionSubmitRequest) throws CborDeserializationException, CborSerializationException;
}
