package org.cardanofoundation.rosetta.api.construction.service;

import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import org.openapitools.client.model.ConstructionCombineRequest;
import org.openapitools.client.model.ConstructionCombineResponse;
import org.openapitools.client.model.ConstructionDeriveRequest;
import org.openapitools.client.model.ConstructionDeriveResponse;
import org.openapitools.client.model.ConstructionHashRequest;
import org.openapitools.client.model.ConstructionMetadataRequest;
import org.openapitools.client.model.ConstructionMetadataResponse;
import org.openapitools.client.model.ConstructionParseRequest;
import org.openapitools.client.model.ConstructionParseResponse;
import org.openapitools.client.model.ConstructionPayloadsRequest;
import org.openapitools.client.model.ConstructionPayloadsResponse;
import org.openapitools.client.model.ConstructionPreprocessRequest;
import org.openapitools.client.model.ConstructionPreprocessResponse;
import org.openapitools.client.model.ConstructionSubmitRequest;
import org.openapitools.client.model.TransactionIdentifierResponse;


public interface ConstructionApiService {

  ConstructionDeriveResponse constructionDeriveService(
      ConstructionDeriveRequest constructionDeriveRequest);

  ConstructionPreprocessResponse constructionPreprocessService(
      ConstructionPreprocessRequest constructionPreprocessRequest);

  ConstructionMetadataResponse constructionMetadataService(
      ConstructionMetadataRequest constructionMetadataRequest);

  ConstructionPayloadsResponse constructionPayloadsService(
      ConstructionPayloadsRequest constructionPayloadsRequest) throws CborDeserializationException, CborSerializationException;

  void verifyProtocolParameters(ConstructionPayloadsRequest constructionPayloadsRequest);

  ConstructionParseResponse constructionParseService(
      ConstructionParseRequest constructionParseRequest);

  ConstructionCombineResponse constructionCombineService(
      ConstructionCombineRequest constructionCombineRequest);

  TransactionIdentifierResponse constructionHashService(
      ConstructionHashRequest constructionHashRequest);

  TransactionIdentifierResponse constructionSubmitService(
      ConstructionSubmitRequest constructionSubmitRequest);
}
