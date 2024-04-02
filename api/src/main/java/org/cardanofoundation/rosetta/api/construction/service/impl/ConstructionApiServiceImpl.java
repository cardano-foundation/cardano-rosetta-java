package org.cardanofoundation.rosetta.api.construction.service.impl;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.UnicodeString;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.block.model.entity.ProtocolParams;
import org.cardanofoundation.rosetta.common.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.common.enumeration.AddressType;

import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionExtraData;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionParsed;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.UnsignedTransaction;
import org.cardanofoundation.rosetta.common.services.CardanoAddressService;
import org.cardanofoundation.rosetta.common.services.CardanoService;
import org.cardanofoundation.rosetta.api.construction.service.ConstructionApiService;
import org.cardanofoundation.rosetta.common.services.LedgerDataProviderService;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.cardanofoundation.rosetta.common.util.CborEncodeUtil;
import org.cardanofoundation.rosetta.common.mapper.CborMapToTransactionExtraData;
import org.openapitools.client.model.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConstructionApiServiceImpl implements ConstructionApiService {

  private final CardanoAddressService cardanoAddressService;
  private final CardanoService cardanoService;
  private final LedgerDataProviderService ledgerService;

  @Override
  public ConstructionDeriveResponse constructionDeriveService(
      ConstructionDeriveRequest constructionDeriveRequest) {
    PublicKey publicKey = constructionDeriveRequest.getPublicKey();
    log.info("Deriving address for public key: {}", publicKey);
    NetworkEnum network = Optional.ofNullable(NetworkEnum.fromValue(
        constructionDeriveRequest.getNetworkIdentifier().getNetwork())).orElseThrow(
        ExceptionFactory::invalidNetworkError);
    // casting unspecific rosetta specification to cardano specific metadata
    ConstructionDeriveMetadata metadata = Optional.ofNullable(constructionDeriveRequest.getMetadata()).orElse(new ConstructionDeriveMetadata());
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
    ConstructionPreprocessMetadata metadata = constructionPreprocessRequest.getMetadata();

    Double relativeTtl = cardanoService.checkOrReturnDefaultTtl(metadata.getRelativeTtl());
    Double transactionSize = cardanoService.calculateTxSize(
        NetworkIdentifierType.findByName(networkIdentifier.getNetwork()),
        constructionPreprocessRequest.getOperations(), 0,
        metadata.getDepositParameters());
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
    Long ttl = cardanoService.calculateTtl(relativeTtl.longValue());
    log.debug("[constructionMetadata] ttl is {}", ttl);
    log.debug("[constructionMetadata] updating tx size from {}", txSize);
    Long updatedTxSize = cardanoService.updateTxSize(txSize.longValue(), 0L, ttl);
    log.debug("[constructionMetadata] updated txSize size is ${updatedTxSize}");
    ProtocolParams protocolParams = ledgerService.findProtocolParametersFromIndexerAndConfig();
    log.debug("[constructionMetadata] received protocol parameters from block-service {}",
        protocolParams);
    Long suggestedFee = cardanoService.calculateTxMinimumFee(updatedTxSize, protocolParams);
    log.debug("[constructionMetadata] suggested fee is ${suggestedFee}");
    return DataMapper.mapToMetadataResponse(protocolParams, ttl, suggestedFee);
  }

  @Override
  public ConstructionPayloadsResponse constructionPayloadsService(
      ConstructionPayloadsRequest constructionPayloadsRequest) {
    int ttl = constructionPayloadsRequest.getMetadata().getTtl();
    List<Operation> operations = constructionPayloadsRequest.getOperations();

    checkOperationsHaveIdentifier(operations);

    NetworkIdentifierType networkIdentifier = NetworkIdentifierType.findByName(constructionPayloadsRequest.getNetworkIdentifier().getNetwork());
    log.info(operations + "[constuctionPayloads] Operations about to be processed");

    ProtocolParameters protocolParameters = constructionPayloadsRequest.getMetadata()
        .getProtocolParameters();
    String keyDeposit;
    String poolDeposit;
    // TODO need to convert OpenAPI ProcotolParameters to domain ProtocolParams. Then merge with the one from indexer/config
    if(protocolParameters != null) {
      keyDeposit = protocolParameters.getKeyDeposit();
      poolDeposit = protocolParameters.getPoolDeposit();
    } else {
      ProtocolParams protocolParametersFromIndexerAndConfig = ledgerService.findProtocolParametersFromIndexerAndConfig();
      keyDeposit = protocolParametersFromIndexerAndConfig.getKeyDeposit().toString();
      poolDeposit = protocolParametersFromIndexerAndConfig.getPoolDeposit().toString();
    }

    UnsignedTransaction unsignedTransaction = null;
    try {
      unsignedTransaction = cardanoService.createUnsignedTransaction(
          networkIdentifier, operations, ttl,
          new DepositParameters(keyDeposit,
              poolDeposit));
    } catch (IOException | CborSerializationException | AddressExcepion | CborException e) {
      throw ExceptionFactory.cantCreateUnsignedTransactionFromBytes();
    }
    List<SigningPayload> payloads = cardanoService.constructPayloadsForTransactionBody(
        unsignedTransaction.hash(), unsignedTransaction.addresses());
    String unsignedTransactionString = null;
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

    TransactionParsed result = cardanoService.parseTransaction(networkIdentifier,
        constructionParseRequest.getTransaction(), signed);

    return new ConstructionParseResponse(result.operations(), null,
        result.accountIdentifierSigners(), null);
  }

  @Override
  public ConstructionCombineResponse constructionCombineService(
      ConstructionCombineRequest constructionCombineRequest) {
    log.info("[constructionCombine] Request received to sign a transaction");
    Array array = cardanoService.decodeTransaction(
        constructionCombineRequest.getUnsignedTransaction());
    TransactionExtraData extraData = CborMapToTransactionExtraData.convertCborMapToTransactionExtraData(
        (co.nstant.in.cbor.model.Map) array.getDataItems().get(1));

    String signedTransaction = cardanoService.buildTransaction(
        ((UnicodeString) array.getDataItems().getFirst()).getString(),
        DataMapper.mapRosettaSignatureToSignaturesList(constructionCombineRequest.getSignatures()),
        extraData.transactionMetadataHex());
    try {
      return new ConstructionCombineResponse(CborEncodeUtil.encodeExtraData(signedTransaction, extraData.operations(), extraData.transactionMetadataHex()));
    } catch (CborException e) {
      throw new ApiException("Error while encoding signed transaction", e);
    }
  }

  @Override
  public TransactionIdentifierResponse constructionHashService(
      ConstructionHashRequest constructionHashRequest) {
    Array array = cardanoService.decodeTransaction(constructionHashRequest.getSignedTransaction());
    log.info("[constructionHash] About to get hash of signed transaction");
    String transactionHash = cardanoService.getHashOfSignedTransaction(
        ((UnicodeString) array.getDataItems().getFirst()).getString());
    log.info("[constructionHash] About to return hash of signed transaction");
    return new TransactionIdentifierResponse(new TransactionIdentifier(transactionHash), null);
  }

  @Override
  public TransactionIdentifierResponse constructionSubmitService(
      ConstructionSubmitRequest constructionSubmitRequest) {
    String signedTransaction = constructionSubmitRequest.getSignedTransaction();
    log.info("[constructionSubmit] About to submit signed transaction");
    String txHash = cardanoService.submitTransaction(signedTransaction);

    return new TransactionIdentifierResponse(new TransactionIdentifier(txHash), null);
  }
}
