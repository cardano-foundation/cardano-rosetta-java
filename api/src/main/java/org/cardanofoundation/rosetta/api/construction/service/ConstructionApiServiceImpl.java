package org.cardanofoundation.rosetta.api.construction.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import jakarta.annotation.Nullable;

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
import org.openapitools.client.model.*;

import org.cardanofoundation.rosetta.api.block.model.domain.ProcessOperations;
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
import org.cardanofoundation.rosetta.common.time.OfflineSlotService;
import org.cardanofoundation.rosetta.common.util.CborEncodeUtil;
import org.cardanofoundation.rosetta.common.util.Constants;

import static com.bloxbean.cardano.client.util.HexUtil.decodeHexString;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConstructionApiServiceImpl implements ConstructionApiService {

  private final CardanoConstructionService cardanoConstructionService;
  private final ProtocolParamService protocolParamService;
  private final ConstructionMapper constructionMapper;
  private final OfflineSlotService offlineSlotService;
  private final ProtocolParamsConverter protocolParamsConverter;

  @Override
  public ConstructionDeriveResponse constructionDeriveService(
          ConstructionDeriveRequest constructionDeriveRequest) {
    PublicKey publicKey = constructionDeriveRequest.getPublicKey();
    log.info("Deriving address for public key: {}", publicKey);

    NetworkEnum networkEnum = NetworkEnum.findByName(
                    constructionDeriveRequest.getNetworkIdentifier().getNetwork())
            .orElseThrow(ExceptionFactory::invalidNetworkError);

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
            .getCardanoAddress(addressType, stakingCredential, publicKey, networkEnum);

    return new ConstructionDeriveResponse(null, new AccountIdentifier(address, null, null), null);
  }

  @Override
  public ConstructionPreprocessResponse constructionPreprocessService(
          ConstructionPreprocessRequest constructionPreprocessRequest) {
    NetworkIdentifier networkIdentifier = constructionPreprocessRequest.getNetworkIdentifier();

    Optional<ConstructionPreprocessMetadata> metadata = Optional.ofNullable(
            constructionPreprocessRequest.getMetadata());

    int relativeTtl = calculateRelativeTtl(metadata);

    long currentSlotBasedOnTime = offlineSlotService.getCurrentSlotBasedOnTimeWithFallback() + relativeTtl;

    NetworkEnum networkEnum = NetworkEnum.findByName(
                    networkIdentifier.getNetwork())
            .orElseThrow(ExceptionFactory::invalidNetworkError);
    Network network = networkEnum.getNetwork();

    List<Operation> operations = constructionPreprocessRequest.getOperations();

    int transactionSize = cardanoConstructionService.calculateTxSize(
            network,
            operations,
            currentSlotBasedOnTime
    );

    Map<String, Integer> response = Map.of(
            Constants.RELATIVE_TTL, relativeTtl,
            Constants.TRANSACTION_SIZE, transactionSize
    );

    return new ConstructionPreprocessResponse(response, null);
  }

  private int calculateRelativeTtl(Optional<ConstructionPreprocessMetadata> metadata) {
    return metadata.map(ConstructionPreprocessMetadata::getRelativeTtl)
            .map(cardanoConstructionService::checkOrReturnDefaultTtl)
            .orElse(Constants.DEFAULT_RELATIVE_TTL);
  }

  @Override
  public ConstructionMetadataResponse constructionMetadataService(
          ConstructionMetadataRequest constructionMetadataRequest) {

    CompletableFuture<ProtocolParams> protocolParamsFuture = CompletableFuture
            .supplyAsync(protocolParamService::findProtocolParameters);
    ConstructionMetadataRequestOption options = constructionMetadataRequest.getOptions();

    int relativeTtl = options.getRelativeTtl().intValue();
    long txSize = options.getTransactionSize().longValue();

    log.debug("[constructionMetadata] Calculating ttl based on {} relative ttl", relativeTtl);

    Long ttl = cardanoConstructionService.calculateTtl((long) relativeTtl);

    log.debug("[constructionMetadata] ttl is {}", ttl);
    log.debug("[constructionMetadata] updating tx size from {}", txSize);
    log.debug("[constructionMetadata] updated txSize size is ${updatedTxSize}");

    ProtocolParams protocolParams = protocolParamsFuture.join();
    log.debug("[constructionMetadata] received protocol parameters from block-service {}",
            protocolParams);

    Long suggestedFee = cardanoConstructionService.calculateTxMinimumFee(
            txSize,
            protocolParams
    );

    log.debug("[constructionMetadata] suggested fee is ${suggestedFee}");

    return constructionMapper.mapToMetadataResponse(protocolParams, ttl, suggestedFee);
  }

  @Override
  public ConstructionPayloadsResponse constructionPayloadsService(
          ConstructionPayloadsRequest constructionPayloadsRequest) {

    List<Operation> operations = constructionPayloadsRequest.getOperations();

    log.info("{} [constructionPayloads] Operations about to be processed", operations);

    ConstructionPayloadsRequestMetadata metadata = constructionPayloadsRequest.getMetadata();
    DepositParameters depositParameters = getDepositParameters(metadata);

    long ttl = calculateTtl(metadata);

    NetworkEnum networkEnum = NetworkEnum.findByName(
                    constructionPayloadsRequest.getNetworkIdentifier().getNetwork())
            .orElseThrow(ExceptionFactory::invalidNetworkError);

    Network network = networkEnum.getNetwork();

    long txSize = cardanoConstructionService.calculateTxSize(network, operations, ttl);

    long calculatedMinFee = cardanoConstructionService.calculateTxMinimumFee(
            txSize,
            convertProtocolParams(metadata)
    );

    ProcessOperations processOperations = cardanoConstructionService.convertRosettaOperations(network, operations);

    double refundsSum = processOperations.getStakeKeyDeRegistrationsCount() * Long.parseLong(
            depositParameters.getKeyDeposit());

    Map<String, Double> depositsSumMap = cardanoConstructionService.getDepositsSumMap(depositParameters, processOperations, refundsSum);

    long calculatedRosettaFee = cardanoConstructionService.calculateRosettaSpecificTransactionFee(processOperations.getInputAmounts(),
            processOperations.getOutputAmounts(),
            processOperations.getWithdrawalAmounts(), depositsSumMap);

    if (calculatedRosettaFee < calculatedMinFee) {
      //throw ExceptionFactory.feeBelowMinimumError(calculatedFee, calculatedMinFee);
      log.warn("CalculatedRosettaFee is below minimum calculatedMinFee (based on tx size). Consider adjusting inputs and outputs to set higher fee, calculatedRosettaFee: {} to minFee: {}", calculatedRosettaFee, calculatedMinFee);

      // TODO throw error in the future?
    }

    UnsignedTransaction unsignedTransaction = createUnsignedTransaction
            (network,
                    operations,
                    ttl,
                    calculatedRosettaFee
            );

    List<SigningPayload> payloads = cardanoConstructionService.constructPayloadsForTransactionBody(
            unsignedTransaction.hash(), unsignedTransaction.addresses()
    );

    String unsignedTransactionString = encodeUnsignedTransaction(unsignedTransaction, operations);

    return new ConstructionPayloadsResponse(unsignedTransactionString, payloads);
  }

  private long calculateTtl(ConstructionPayloadsRequestMetadata metadata) {
    return metadata != null ?
            cardanoConstructionService.checkOrReturnDefaultTtl(metadata.getTtl()) :
            offlineSlotService.getCurrentSlotBasedOnTimeWithFallback() + Constants.DEFAULT_RELATIVE_TTL;
  }

  private ProtocolParams convertProtocolParams(@Nullable ConstructionPayloadsRequestMetadata metadata) {
    if (metadata == null) {
      return protocolParamService.findProtocolParameters();
    }

    return metadata.getProtocolParameters() == null ? protocolParamService.findProtocolParameters() :
            protocolParamsConverter.convert(metadata.getProtocolParameters());
  }

  @Override
  public void verifyProtocolParameters(ConstructionPayloadsRequest constructionPayloadsRequest) {
    if (constructionPayloadsRequest.getMetadata() == null || constructionPayloadsRequest.getMetadata().getTtl() == null) {
      throw ExceptionFactory.ttlMissingError();
    }
    if (constructionPayloadsRequest.getMetadata().getProtocolParameters() == null) {
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

    NetworkEnum networkEnum = NetworkEnum.findByName(
                    constructionParseRequest.getNetworkIdentifier().getNetwork())
            .orElseThrow(ExceptionFactory::invalidNetworkError);
    Network network = networkEnum.getNetwork();

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
    byte[] signedTransactionBytes = decodeHexString(((UnicodeString) array.getDataItems().getFirst()).getString());
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

  private UnsignedTransaction createUnsignedTransaction(Network network,
                                                        List<Operation> operations,
                                                        long ttl,
                                                        Long calculatedFee) {
    try {
      return cardanoConstructionService.createUnsignedTransaction(network, operations, ttl, calculatedFee);
    } catch (IOException | CborSerializationException | AddressExcepion | CborException e) {
      log.error("Failed to create unsigned transaction: {}", e.getMessage());

      throw ExceptionFactory.cantCreateUnsignedTransactionFromBytes();
    }
  }

  private String encodeUnsignedTransaction(UnsignedTransaction unsignedTransaction,
                                           List<Operation> operations) {
    try {
      return CborEncodeUtil.encodeExtraData(
              unsignedTransaction.bytes(),
              operations,
              unsignedTransaction.metadata()
      );
    } catch (CborException e) {
      log.error("Error encoding unsigned transaction: {}", e.getMessage());
      throw ExceptionFactory.cantEncodeExtraData();
    }
  }

  private DepositParameters getDepositParameters(ConstructionPayloadsRequestMetadata metadata) {
    return metadata != null && metadata.getProtocolParameters() != null ?
            new DepositParameters(metadata.getProtocolParameters().getKeyDeposit(),
                    metadata.getProtocolParameters().getPoolDeposit()) :

            cardanoConstructionService.getDepositParameters();
  }

}
