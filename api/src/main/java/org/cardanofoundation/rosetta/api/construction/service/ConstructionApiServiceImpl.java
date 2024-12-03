package org.cardanofoundation.rosetta.api.construction.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.UnicodeString;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import com.bloxbean.cardano.client.util.HexUtil;
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
import org.cardanofoundation.rosetta.api.construction.enumeration.AddressType;
import org.cardanofoundation.rosetta.api.construction.mapper.ConstructionMapper;
import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.mapper.CborMapToTransactionExtraData;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionExtraData;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.TransactionParsed;
import org.cardanofoundation.rosetta.common.model.cardano.transaction.UnsignedTransaction;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;
import org.cardanofoundation.rosetta.common.util.CborEncodeUtil;
import org.cardanofoundation.rosetta.common.util.Constants;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConstructionApiServiceImpl implements ConstructionApiService {

  private final CardanoConstructionService cardanoConstructionService;
  private final ProtocolParamService protocolParamService;
  private final ConstructionMapper constructionMapper;

  @Override
  public ConstructionDeriveResponse constructionDeriveService(
      ConstructionDeriveRequest constructionDeriveRequest) {
    PublicKey publicKey = constructionDeriveRequest.getPublicKey();
    log.info("Deriving address for public key: {}", publicKey);

    NetworkEnum network = NetworkEnum.findByName(
        constructionDeriveRequest.getNetworkIdentifier().getNetwork());
    if (network == null) {
      throw ExceptionFactory.invalidNetworkError();
    }

    // casting unspecific rosetta specification to cardano specific metadata
    ConstructionDeriveMetadata metadata = constructionDeriveRequest.getMetadata();
    if (metadata == null) {
      metadata = new ConstructionDeriveMetadata();
    }

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
    String address = cardanoConstructionService
        .getCardanoAddress(addressType, stakingCredential, publicKey, network);
    return new ConstructionDeriveResponse(null, new AccountIdentifier(address, null, null), null);
  }

  @Override
  public ConstructionPreprocessResponse constructionPreprocessService(
      ConstructionPreprocessRequest constructionPreprocessRequest) {

    NetworkIdentifier networkIdentifier = constructionPreprocessRequest.getNetworkIdentifier();
    Optional<ConstructionPreprocessMetadata> metadata = Optional.ofNullable(
        constructionPreprocessRequest.getMetadata());
    int relativeTtl;
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

    int transactionSize = cardanoConstructionService.calculateTxSize(
        Objects.requireNonNull(NetworkEnum.findByName(networkIdentifier.getNetwork())).getNetwork(),
        constructionPreprocessRequest.getOperations(), 0, depositParameters);
    Map<String, Integer> response = Map.of(Constants.RELATIVE_TTL, relativeTtl,
        Constants.TRANSACTION_SIZE, transactionSize);
    return new ConstructionPreprocessResponse(response, null);
  }

  @Override
  public ConstructionMetadataResponse constructionMetadataService(
      ConstructionMetadataRequest constructionMetadataRequest) {

    CompletableFuture<ProtocolParams> protocolParamsFuture = CompletableFuture
        .supplyAsync(protocolParamService::findProtocolParameters);

    ConstructionMetadataRequestOption options = constructionMetadataRequest.getOptions();
    Double relativeTtl = options.getRelativeTtl().doubleValue();
    Double txSize = options.getTransactionSize().doubleValue();
    log.debug("[constructionMetadata] Calculating ttl based on {} relative ttl", relativeTtl);

    Long ttl = cardanoConstructionService.calculateTtl(relativeTtl.longValue());
    log.debug("[constructionMetadata] ttl is {}", ttl);
    log.debug("[constructionMetadata] updating tx size from {}", txSize);

    Long updatedTxSize = cardanoConstructionService.updateTxSize(txSize.longValue(), 0L, ttl);
    log.debug("[constructionMetadata] updated txSize size is ${updatedTxSize}");

    ProtocolParams protocolParams = protocolParamsFuture.join();
    log.debug("[constructionMetadata] received protocol parameters from block-service {}",
        protocolParams);

    Long suggestedFee = cardanoConstructionService.calculateTxMinimumFee(updatedTxSize,
        protocolParams);
    log.debug("[constructionMetadata] suggested fee is ${suggestedFee}");
    return constructionMapper.mapToMetadataResponse(protocolParams, ttl, suggestedFee);
  }

  @Override
  public ConstructionPayloadsResponse constructionPayloadsService(
      ConstructionPayloadsRequest constructionPayloadsRequest) {

    List<Operation> operations = constructionPayloadsRequest.getOperations();

    log.info("{} [constuctionPayloads] Operations about to be processed", operations);
    ConstructionPayloadsRequestMetadata metadata = constructionPayloadsRequest.getMetadata();

    int ttl = metadata != null ?
        cardanoConstructionService.checkOrReturnDefaultTtl(metadata.getTtl()) :
        Constants.DEFAULT_RELATIVE_TTL;

    DepositParameters depositParameters = getDepositParameters(metadata);
    Network network = NetworkEnum.findByName(
        constructionPayloadsRequest.getNetworkIdentifier().getNetwork()).getNetwork();

    UnsignedTransaction unsignedTransaction = createUnsignedTransaction(network,
        operations, ttl, depositParameters);
    List<SigningPayload> payloads = cardanoConstructionService.constructPayloadsForTransactionBody(
        unsignedTransaction.hash(), unsignedTransaction.addresses());
    String unsignedTransactionString = encodeUnsignedTransaction(unsignedTransaction, operations);
    return new ConstructionPayloadsResponse(unsignedTransactionString, payloads);
  }

  @Override
  public void verifyProtocolParameters(ConstructionPayloadsRequest constructionPayloadsRequest) {
    if(constructionPayloadsRequest.getMetadata() == null || constructionPayloadsRequest.getMetadata().getTtl() == null) {
      throw ExceptionFactory.ttlMissingError();
    }
    if(constructionPayloadsRequest.getMetadata().getProtocolParameters() == null) {
      throw ExceptionFactory.protocolParametersMissingError();
    } else {
      if(constructionPayloadsRequest.getMetadata().getProtocolParameters().getCoinsPerUtxoSize() == null) {
        throw ExceptionFactory.coinsPerUtxoSizeMissingError();
      }
      if(constructionPayloadsRequest.getMetadata().getProtocolParameters().getMaxTxSize() == null) {
        throw ExceptionFactory.maxTxSizeMissingError();
      }
      if(constructionPayloadsRequest.getMetadata().getProtocolParameters().getMaxValSize() == null) {
        throw ExceptionFactory.maxValSizeMissingError();
      }
      if(constructionPayloadsRequest.getMetadata().getProtocolParameters().getKeyDeposit() == null) {
        throw ExceptionFactory.keyDepositMissingError();
      }
      if(constructionPayloadsRequest.getMetadata().getProtocolParameters().getMaxCollateralInputs() == null) {
        throw ExceptionFactory.maxCollateralInputsMissingError();
      }
      if(constructionPayloadsRequest.getMetadata().getProtocolParameters().getMinFeeCoefficient() == null) {
        throw ExceptionFactory.minFeeCoefficientMissingError();
      }
      if(constructionPayloadsRequest.getMetadata().getProtocolParameters().getMinFeeConstant() == null) {
        throw ExceptionFactory.minFeeConstantMissingError();
      }
      if(constructionPayloadsRequest.getMetadata().getProtocolParameters().getMinPoolCost() == null) {
        throw ExceptionFactory.minPoolCostMissingError();
      }
      if(constructionPayloadsRequest.getMetadata().getProtocolParameters().getPoolDeposit() == null) {
        throw ExceptionFactory.poolDepositMissingError();
      }
      if(constructionPayloadsRequest.getMetadata().getProtocolParameters().getProtocol() == null) {
        throw ExceptionFactory.protocolMissingError();
      }
    }
  }

  @Override
  public ConstructionParseResponse constructionParseService(
      ConstructionParseRequest constructionParseRequest) {
    Boolean signed = Optional.ofNullable(constructionParseRequest.getSigned()).orElseThrow(
        () -> ExceptionFactory.unspecifiedError("body should have required property signed."));

    Network network = Objects
            .requireNonNull(NetworkEnum.findByName(constructionParseRequest.getNetworkIdentifier().getNetwork()))
            .getNetwork();
    log.info(constructionParseRequest.getTransaction() + "[constructionParse] Processing");

    TransactionParsed result = cardanoConstructionService.parseTransaction(network,
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
        constructionMapper.mapRosettaSignatureToSignaturesList(constructionCombineRequest.getSignatures()),
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
    byte[] signedTransactionBytes = HexUtil.decodeHexString(((UnicodeString) array.getDataItems().getFirst()).getString());
    String transactionHash = TransactionUtil.getTxHash(signedTransactionBytes);
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

  private DepositParameters getDepositParameters(ConstructionPayloadsRequestMetadata metadata) {
    return metadata != null && metadata.getProtocolParameters() != null ?
        new DepositParameters(metadata.getProtocolParameters().getKeyDeposit(),
            metadata.getProtocolParameters().getPoolDeposit()) :
        cardanoConstructionService.getDepositParameters();
  }

  private UnsignedTransaction createUnsignedTransaction(Network network, List<Operation> operations, int ttl,
     DepositParameters depositParameters) {
    try {
      return cardanoConstructionService.createUnsignedTransaction(network, operations, ttl, depositParameters);
    } catch (IOException | CborSerializationException | AddressExcepion | CborException e) {
      log.error("Failed to create unsigned transaction: {}", e.getMessage());
      throw ExceptionFactory.cantCreateUnsignedTransactionFromBytes();
    }
  }

  private String encodeUnsignedTransaction(UnsignedTransaction unsignedTransaction,
      List<Operation> operations) {
    try {
      return CborEncodeUtil.encodeExtraData(unsignedTransaction.bytes(), operations,
          unsignedTransaction.metadata());
    } catch (CborException e) {
      log.error("Error encoding unsigned transaction: {}", e.getMessage());
      throw ExceptionFactory.cantEncodeExtraData();
    }
  }
}
