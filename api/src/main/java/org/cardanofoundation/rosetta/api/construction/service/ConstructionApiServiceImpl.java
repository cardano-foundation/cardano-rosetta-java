package org.cardanofoundation.rosetta.api.construction.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.UnicodeString;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.ConstructionCombineRequest;
import org.openapitools.client.model.ConstructionCombineResponse;
import org.openapitools.client.model.ConstructionDeriveMetadata;
import org.openapitools.client.model.ConstructionDeriveRequest;
import org.openapitools.client.model.ConstructionDeriveResponse;
import org.openapitools.client.model.ConstructionHashRequest;
import org.openapitools.client.model.ConstructionMetadataRequest;
import org.openapitools.client.model.ConstructionMetadataRequestOption;
import org.openapitools.client.model.ConstructionMetadataResponse;
import org.openapitools.client.model.ConstructionParseRequest;
import org.openapitools.client.model.ConstructionParseResponse;
import org.openapitools.client.model.ConstructionPayloadsRequest;
import org.openapitools.client.model.ConstructionPayloadsRequestMetadata;
import org.openapitools.client.model.ConstructionPayloadsResponse;
import org.openapitools.client.model.ConstructionPreprocessMetadata;
import org.openapitools.client.model.ConstructionPreprocessRequest;
import org.openapitools.client.model.ConstructionPreprocessResponse;
import org.openapitools.client.model.ConstructionSubmitRequest;
import org.openapitools.client.model.DepositParameters;
import org.openapitools.client.model.NetworkIdentifier;
import org.openapitools.client.model.Operation;
import org.openapitools.client.model.PublicKey;
import org.openapitools.client.model.SigningPayload;
import org.openapitools.client.model.TransactionIdentifier;
import org.openapitools.client.model.TransactionIdentifierResponse;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.common.enumeration.AddressType;
import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;
import org.cardanofoundation.rosetta.common.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.mapper.CborMapToTransactionExtraData;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionExtraData;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionParsed;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.UnsignedTransaction;
import org.cardanofoundation.rosetta.common.services.CardanoAddressService;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;
import org.cardanofoundation.rosetta.common.util.CborEncodeUtil;
import org.cardanofoundation.rosetta.common.util.Constants;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConstructionApiServiceImpl implements ConstructionApiService {

  private final CardanoAddressService cardanoAddressService;
  private final CardanoConstructionService cardanoConstructionService;
  private final ProtocolParamService protocolParamService;
  private final DataMapper dataMapper;

  @Override
  public ConstructionDeriveResponse constructionDeriveService(
      ConstructionDeriveRequest constructionDeriveRequest) {
    PublicKey publicKey = constructionDeriveRequest.getPublicKey();
    log.info("Deriving address for public key: {}", publicKey);
    NetworkEnum network = Optional.ofNullable(NetworkEnum.fromValue(
        constructionDeriveRequest.getNetworkIdentifier().getNetwork())).orElseThrow(
        ExceptionFactory::invalidNetworkError);
    // casting unspecific rosetta specification to cardano specific metadata
    ConstructionDeriveMetadata metadata = Optional.ofNullable(
        constructionDeriveRequest.getMetadata()).orElse(new ConstructionDeriveMetadata());
    // Default address type is enterprise
    AddressType addressType =
        metadata.getAddressType() != null ? AddressType.findByValue(metadata.getAddressType())
            : null;
    addressType = addressType != null ? addressType : AddressType.ENTERPRISE;

    PublicKey stakingCredential = null;
    if (addressType == AddressType.BASE) {
      stakingCredential = Optional.ofNullable(metadata.getStakingCredential())
          .orElseThrow(ExceptionFactory::missingStakingKeyError);
    }
    String address = cardanoAddressService.getCardanoAddress(addressType, stakingCredential,
        publicKey, network);
    return new ConstructionDeriveResponse(null, new AccountIdentifier(address, null, null), null);
  }

  @Override
  public ConstructionPreprocessResponse constructionPreprocessService(
      ConstructionPreprocessRequest constructionPreprocessRequest) {

    NetworkIdentifier networkIdentifier = constructionPreprocessRequest.getNetworkIdentifier();
    Optional<ConstructionPreprocessMetadata> metadata = Optional.ofNullable(
        constructionPreprocessRequest.getMetadata());
    Double relativeTtl;
    DepositParameters depositParameters;
    if (metadata.isPresent()) {
      relativeTtl = cardanoConstructionService.checkOrReturnDefaultTtl(
          metadata.get().getRelativeTtl());
      depositParameters = Optional.ofNullable(metadata.get().getDepositParameters()).orElse(
          cardanoConstructionService.getDepositParameters());
    } else {
      relativeTtl = Constants.DEFAULT_RELATIVE_TTL;
      depositParameters = cardanoConstructionService.getDepositParameters();
    }

    Double transactionSize = cardanoConstructionService.calculateTxSize(
        NetworkIdentifierType.findByName(networkIdentifier.getNetwork()),
        constructionPreprocessRequest.getOperations(), 0, depositParameters);
    Map<String, Double> response = Map.of(Constants.RELATIVE_TTL, relativeTtl,
        Constants.TRANSACTION_SIZE,
        transactionSize);
    return new ConstructionPreprocessResponse(response, null);
  }

  @Override
  public ConstructionMetadataResponse constructionMetadataService(
      ConstructionMetadataRequest constructionMetadataRequest) {

    ConstructionMetadataRequestOption options = constructionMetadataRequest.getOptions();
    Double relativeTtl = options.getRelativeTtl().doubleValue();
    Double txSize = options.getTransactionSize().doubleValue();
    log.debug("[constructionMetadata] Calculating ttl based on {} relative ttl", relativeTtl);
    Long ttl = cardanoConstructionService.calculateTtl(relativeTtl.longValue());
    log.debug("[constructionMetadata] ttl is {}", ttl);
    log.debug("[constructionMetadata] updating tx size from {}", txSize);
    Long updatedTxSize = cardanoConstructionService.updateTxSize(txSize.longValue(), 0L, ttl);
    log.debug("[constructionMetadata] updated txSize size is ${updatedTxSize}");
    ProtocolParams protocolParams =
        protocolParamService.findProtocolParametersFromIndexerAndConfig();
    log.debug("[constructionMetadata] received protocol parameters from block-service {}",
        protocolParams);
    Long suggestedFee = cardanoConstructionService.calculateTxMinimumFee(updatedTxSize,
        protocolParams);
    log.debug("[constructionMetadata] suggested fee is ${suggestedFee}");
    return dataMapper.mapToMetadataResponse(protocolParams, ttl, suggestedFee);
  }

  @Override
  public ConstructionPayloadsResponse constructionPayloadsService(
      ConstructionPayloadsRequest constructionPayloadsRequest) {

    List<Operation> operations = constructionPayloadsRequest.getOperations();

    checkOperationsHaveIdentifier(operations);

    NetworkIdentifierType networkIdentifier = NetworkIdentifierType.findByName(
        constructionPayloadsRequest.getNetworkIdentifier().getNetwork());
    log.info(operations + "[constuctionPayloads] Operations about to be processed");
    Optional<ConstructionPayloadsRequestMetadata> metadata = Optional.ofNullable(
        constructionPayloadsRequest.getMetadata());
    int ttl;
    DepositParameters depositParameters;
    if (metadata.isPresent()) {
      ttl = cardanoConstructionService.checkOrReturnDefaultTtl(metadata.get().getTtl()).intValue();
      depositParameters = Optional.ofNullable(metadata.get().getProtocolParameters()).map(
          protocolParameters -> new DepositParameters(protocolParameters.getKeyDeposit(),
              protocolParameters.getPoolDeposit())).orElse(
          cardanoConstructionService.getDepositParameters());
    } else {
      ttl = Constants.DEFAULT_RELATIVE_TTL.intValue();
      depositParameters = cardanoConstructionService.getDepositParameters();
    }

    UnsignedTransaction unsignedTransaction;
    try {
      unsignedTransaction = cardanoConstructionService.createUnsignedTransaction(
          networkIdentifier, operations, ttl,
          depositParameters);
    } catch (IOException | CborSerializationException | AddressExcepion | CborException e) {
      throw ExceptionFactory.cantCreateUnsignedTransactionFromBytes();
    }
    List<SigningPayload> payloads = cardanoConstructionService.constructPayloadsForTransactionBody(
        unsignedTransaction.hash(), unsignedTransaction.addresses());
    String unsignedTransactionString;
    try {
      unsignedTransactionString = CborEncodeUtil.encodeExtraData(
          unsignedTransaction.bytes(),
          constructionPayloadsRequest.getOperations(),
          unsignedTransaction.metadata());
    } catch (CborException e) {
      throw ExceptionFactory.cantEncodeExtraData();
    }
    return new ConstructionPayloadsResponse(unsignedTransactionString, payloads);
  }

  private static void checkOperationsHaveIdentifier(List<Operation> operations) {
    for (int i = 0; i < operations.size(); i++) {
      if (operations.get(i).getOperationIdentifier() == null) {
        throw ExceptionFactory.unspecifiedError(
            "body[" + i + "]" + " should have required property operation_identifier");
      }
    }
  }

  @Override
  public ConstructionParseResponse constructionParseService(
      ConstructionParseRequest constructionParseRequest) {
    Boolean signed = Optional.ofNullable(constructionParseRequest.getSigned()).orElseThrow(
        () -> ExceptionFactory.unspecifiedError("body should have required property signed."));

    NetworkIdentifierType networkIdentifier = NetworkIdentifierType.findByName(
        constructionParseRequest.getNetworkIdentifier().getNetwork());
    log.info(constructionParseRequest.getTransaction() + "[constructionParse] Processing");

    TransactionParsed result = cardanoConstructionService.parseTransaction(networkIdentifier,
        constructionParseRequest.getTransaction(), signed);

    return new ConstructionParseResponse(result.operations(), null,
        result.accountIdentifierSigners(), null);
  }

  @Override
  public ConstructionCombineResponse constructionCombineService(
      ConstructionCombineRequest constructionCombineRequest) {
    log.info("[constructionCombine] Request received to sign a transaction");
    Array array = cardanoConstructionService.decodeTransaction(
        constructionCombineRequest.getUnsignedTransaction());
    TransactionExtraData extraData = CborMapToTransactionExtraData.convertCborMapToTransactionExtraData(
        (co.nstant.in.cbor.model.Map) array.getDataItems().get(1));

    String signedTransaction = cardanoConstructionService.buildTransaction(
        ((UnicodeString) array.getDataItems().getFirst()).getString(),
        DataMapper.mapRosettaSignatureToSignaturesList(constructionCombineRequest.getSignatures()),
        extraData.transactionMetadataHex());
    try {
      return new ConstructionCombineResponse(
          CborEncodeUtil.encodeExtraData(signedTransaction, extraData.operations(),
              extraData.transactionMetadataHex()));
    } catch (CborException e) {
      throw ExceptionFactory.cantEncodeExtraData();
    }
  }

  @Override
  public TransactionIdentifierResponse constructionHashService(
      ConstructionHashRequest constructionHashRequest) {
    Array array = cardanoConstructionService.decodeTransaction(
        constructionHashRequest.getSignedTransaction());
    log.info("[constructionHash] About to get hash of signed transaction");
    String transactionHash = cardanoConstructionService.getHashOfSignedTransaction(
        ((UnicodeString) array.getDataItems().getFirst()).getString());
    log.info("[constructionHash] About to return hash of signed transaction");
    return new TransactionIdentifierResponse(new TransactionIdentifier(transactionHash), null);
  }

  @Override
  public TransactionIdentifierResponse constructionSubmitService(
      ConstructionSubmitRequest constructionSubmitRequest) {
    String signedTransaction = constructionSubmitRequest.getSignedTransaction();
    log.info("[constructionSubmit] About to submit signed transaction");
    String tx = cardanoConstructionService.extractTransactionIfNeeded(signedTransaction);
    String txHash = cardanoConstructionService.submitTransaction(tx);

    return new TransactionIdentifierResponse(new TransactionIdentifier(txHash), null);
  }
}
