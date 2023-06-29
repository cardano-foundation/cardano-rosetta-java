package org.cardanofoundation.rosetta.api.service.impl;

import static org.cardanofoundation.rosetta.api.common.constants.Constants.REDIS_TTL_MEMPOOL;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnicodeString;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.cardano.yaci.core.protocol.localtx.model.TxSubmissionRequest;
import com.bloxbean.cardano.yaci.helper.LocalTxSubmissionClient;
import com.bloxbean.cardano.yaci.helper.model.TxResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.api.common.constants.Constants;
import org.cardanofoundation.rosetta.api.common.enumeration.AddressType;
import org.cardanofoundation.rosetta.api.common.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.api.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.api.model.AccountIdentifierMetadata;
import org.cardanofoundation.rosetta.api.model.ConstructionPreprocessResponseOptions;
import org.cardanofoundation.rosetta.api.model.DepositParameters;
import org.cardanofoundation.rosetta.api.model.Operation;
import org.cardanofoundation.rosetta.api.model.ProtocolParameters;
import org.cardanofoundation.rosetta.api.model.PublicKey;
import org.cardanofoundation.rosetta.api.model.Signatures;
import org.cardanofoundation.rosetta.api.model.SigningPayload;
import org.cardanofoundation.rosetta.api.model.TransactionExtraData;
import org.cardanofoundation.rosetta.api.model.TransactionIdentifier;
import org.cardanofoundation.rosetta.api.model.TransactionParsed;
import org.cardanofoundation.rosetta.api.model.UnsignedTransaction;
import org.cardanofoundation.rosetta.api.model.rest.AccountIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionCombineRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionCombineResponse;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionDeriveRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionDeriveResponse;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionHashRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionMetadataRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionMetadataResponse;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionMetadataResponseMetadata;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionParseRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionParseResponse;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionPayloadsRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionPayloadsResponse;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionPreprocessRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionPreprocessResponse;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionSubmitRequest;
import org.cardanofoundation.rosetta.api.model.rest.TransactionIdentifierResponse;
import org.cardanofoundation.rosetta.api.service.CardanoService;
import org.cardanofoundation.rosetta.api.service.ConstructionApiService;
import org.cardanofoundation.rosetta.api.util.CardanoAddressUtils;
import org.cardanofoundation.rosetta.api.util.DataItemDecodeUtil;
import org.cardanofoundation.rosetta.api.util.DataItemEncodeUtil;
import org.cardanofoundation.rosetta.api.util.ParseConstructionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@NoArgsConstructor
@Setter
@Getter
public class ConstructionApiServiceImpl implements ConstructionApiService {

  @Autowired
  public CardanoService cardanoService;

  @Autowired
  private LocalTxSubmissionClient localTxSubmissionClient;

  @Autowired
  @Qualifier("redisTemplateString")
  private RedisTemplate<String, String> redisTemplate;

  @Override
  public ConstructionDeriveResponse constructionDeriveService(
      ConstructionDeriveRequest constructionDeriveRequest) {
    PublicKey publicKey = constructionDeriveRequest.getPublicKey();
    NetworkIdentifierType networkIdentifier = cardanoService.getNetworkIdentifierByRequestParameters(
        constructionDeriveRequest.getNetworkIdentifier());

    log.info("[constructionDerive] About to check if public key has valid length and curve type");
    if (Boolean.FALSE.equals(
        CardanoAddressUtils.isKeyValid(publicKey.getHexBytes(), publicKey.getCurveType()))) {
      log.info("[constructionDerive] Public key has an invalid format");
      throw ExceptionFactory.invalidPublicKeyFormat();
    }
    log.info("[constructionDerive] Public key has a valid format");

    PublicKey stakingCredential =
        ObjectUtils.isEmpty(constructionDeriveRequest.getMetadata()) ? null
            : constructionDeriveRequest.getMetadata().getStakingCredential();
    if (stakingCredential != null) {
      log.info(
          "[constructionDerive] About to check if staking credential has valid length and curve type");
      if (Boolean.FALSE.equals(CardanoAddressUtils.isKeyValid(stakingCredential.getHexBytes(),
          stakingCredential.getCurveType()))) {
        log.info("[constructionDerive] Staking credential has an invalid format");
        throw ExceptionFactory.invalidStakingKeyFormat();
      }
      log.info("[constructionDerive] Staking credential key has a valid format");
    }

    String addressType = ObjectUtils.isEmpty(constructionDeriveRequest.getMetadata()) ? null
        : constructionDeriveRequest.getMetadata().getAddressType();
    if (addressType != null) {
      log.info("[constructionDerive] About to check if address type is valid");
      if (!cardanoService.isAddressTypeValid(addressType)) {
        log.info("[constructionDerive] Address type has an invalid value");
        throw ExceptionFactory.invalidAddressTypeError();
      }
      log.info("[constructionDerive] Address type has a valid value");
    }

    log.info("[constructionDerive] About to generate address");
    String address = CardanoAddressUtils.generateAddress(networkIdentifier, publicKey.getHexBytes(),
        
        stakingCredential == null ? null : stakingCredential.getHexBytes(),
        AddressType.findByValue(addressType));
    if (address == null) {
      log.error("[constructionDerive] There was an error generating address");
      throw ExceptionFactory.addressGenerationError();
    }
    log.info("[constructionDerive] new address is {}", address);
    return new ConstructionDeriveResponse(new AccountIdentifier(address));
  }

  @Override
  public ConstructionPreprocessResponse constructionPreprocessService(
      ConstructionPreprocessRequest constructionPreprocessRequest)
      throws IOException, AddressExcepion, CborSerializationException, CborException {
    NetworkIdentifierType networkIdentifier = NetworkIdentifierType.findByName(
        constructionPreprocessRequest.getNetworkIdentifier().getNetwork());
    
    Double relativeTtl = cardanoService.calculateRelativeTtl(
        !ObjectUtils.isEmpty(constructionPreprocessRequest.getMetadata())
            ? constructionPreprocessRequest.getMetadata().getRelativeTtl() : null);
    Double transactionSize = cardanoService.calculateTxSize(networkIdentifier,
        (ArrayList<Operation>) constructionPreprocessRequest.getOperations(), 0,
        ObjectUtils.isEmpty(constructionPreprocessRequest.getMetadata()) ? null
            : constructionPreprocessRequest.getMetadata().getDepositParameters());
    
    return new ConstructionPreprocessResponse(
        new ConstructionPreprocessResponseOptions(relativeTtl, transactionSize), null);
  }


  @Override
  public ConstructionMetadataResponse constructionMetadataService(
      ConstructionMetadataRequest constructionMetadataRequest)
      throws CborException, CborSerializationException {
    Double ttlOffset = constructionMetadataRequest.getOptions().getRelativeTtl();
    Double txSize = constructionMetadataRequest.getOptions().getTransactionSize();
    log.debug("[constructionMetadata] Calculating ttl based on {} relative ttl", ttlOffset);
    Long ttl = cardanoService.calculateTtl(ttlOffset.longValue());
    log.debug("[constructionMetadata] ttl is {}", ttl);
    // As we have calculated tx assuming ttl as 0, we need to properly update the size
    // now that we have it already
    log.debug("[constructionMetadata] updating tx size from {}", txSize);
    Long updatedTxSize = cardanoService.updateTxSize(txSize.longValue(), 0L, ttl);
    log.debug("[constructionMetadata] updated txSize size is ${updatedTxSize}");
    ProtocolParameters protocolParametersResponse = cardanoService.getProtocolParameters();
    log.debug("[constructionMetadata] received protocol parameters from block-service {}",
        protocolParametersResponse);
    Long suggestedFee = cardanoService.calculateTxMinimumFee(updatedTxSize,
        protocolParametersResponse);
    log.debug("[constructionMetadata] suggested fee is ${suggestedFee}");
    
    ProtocolParameters protocolParameters = new ProtocolParameters();
    protocolParameters.setCoinsPerUtxoSize(protocolParametersResponse.getCoinsPerUtxoSize());
    protocolParameters.setMaxTxSize(protocolParametersResponse.getMaxTxSize());
    protocolParameters.setMaxValSize(protocolParametersResponse.getMaxValSize());
    protocolParameters.setKeyDeposit(protocolParametersResponse.getKeyDeposit());
    protocolParameters.setMaxCollateralInputs(protocolParametersResponse.getMaxCollateralInputs());
    protocolParameters.setMinFeeConstant(protocolParametersResponse.getMinFeeConstant());
    protocolParameters.setMinFeeCoefficient(protocolParametersResponse.getMinFeeCoefficient());
    protocolParameters.setMinPoolCost(protocolParametersResponse.getMinPoolCost());
    protocolParameters.setPoolDeposit(protocolParametersResponse.getPoolDeposit());
    protocolParameters.setProtocol(protocolParametersResponse.getProtocol());
    return new ConstructionMetadataResponse(
        new ConstructionMetadataResponseMetadata(ttl.toString(), protocolParameters),
        new ArrayList<>(
            List.of(CardanoAddressUtils.mapAmount(suggestedFee.toString(), null, null, null))));
  }

  @Override
  public ConstructionPayloadsResponse constructionPayloadsService(
      ConstructionPayloadsRequest constructionPayloadsRequest) throws Exception {
    String ttl = constructionPayloadsRequest.getMetadata().getTtl();
    List<Operation> operations = constructionPayloadsRequest.getOperations();
    for (int i = 0; i < operations.size(); i++) {
      if (operations.get(0).getOperationIdentifier() == null) {
        throw ExceptionFactory.unspecifiedError(
            "body[" + i + "]" + " should have required property operation_identifier");
      }
    }
    NetworkIdentifierType networkIdentifier = cardanoService.getNetworkIdentifierByRequestParameters(
        constructionPayloadsRequest.getNetworkIdentifier());
    log.info(operations + "[constuctionPayloads] Operations about to be processed");
    ProtocolParameters protocolParameters = constructionPayloadsRequest.getMetadata()
        .getProtocolParameters();
    UnsignedTransaction unsignedTransaction = cardanoService.createUnsignedTransaction(
        networkIdentifier, operations, Integer.parseInt(ttl),
        new DepositParameters(protocolParameters.getKeyDeposit(),
            protocolParameters.getPoolDeposit()));
    List<SigningPayload> payloads = cardanoService.constructPayloadsForTransactionBody(
        unsignedTransaction.getHash(), unsignedTransaction.getAddresses());
    String unsignedTransactionString = DataItemEncodeUtil.encodeExtraData(
        unsignedTransaction.getBytes(),
        new TransactionExtraData(constructionPayloadsRequest.getOperations(),
            unsignedTransaction.getMetadata()));
    return new ConstructionPayloadsResponse(unsignedTransactionString, payloads);
  }

  @Override
  public ConstructionParseResponse constructionParseService(
      ConstructionParseRequest constructionParseRequest) {
    Boolean signed = constructionParseRequest.getSigned();
    if (signed == null) {
      throw ExceptionFactory.unspecifiedError("body should have required property signed");
    }
    NetworkIdentifierType networkIdentifier = cardanoService.getNetworkIdentifierByRequestParameters(
        constructionParseRequest.getNetworkIdentifier());
    log.info(constructionParseRequest.getTransaction() + "[constructionParse] Processing");
    Array array = cardanoService.decodeExtraData(constructionParseRequest.getTransaction());
    TransactionExtraData extraData = DataItemDecodeUtil.changeFromMaptoObject(
        (Map) array.getDataItems().get(1));
    log.info(array + "[constructionParse] Decoded");
    TransactionParsed result;
    if (signed) {
      result = ParseConstructionUtils.parseSignedTransaction(networkIdentifier,
          ((UnicodeString) array.getDataItems().get(0)).getString(), extraData);
      return new ConstructionParseResponse(null, result.getOperations(),
          result.getAccount_identifier_signers());
    }
    result = ParseConstructionUtils.parseUnsignedTransaction(networkIdentifier,
        ((UnicodeString) array.getDataItems().get(0)).getString(), extraData);
    return new ConstructionParseResponse(null, result.getOperations(),
        result.getAccount_identifier_signers());
  }

  @Override
  public ConstructionCombineResponse constructionCombineService(
      ConstructionCombineRequest constructionCombineRequest) throws CborException {
    log.info("[constructionCombine] Request received to sign a transaction");
    Array array = cardanoService.decodeExtraData(
        constructionCombineRequest.getUnsignedTransaction());
    TransactionExtraData extraData = DataItemDecodeUtil.changeFromMaptoObject(
        (Map) array.getDataItems().get(1));
    String signedTransaction = cardanoService.buildTransaction(
        ((UnicodeString) array.getDataItems().get(0)).getString(),
        constructionCombineRequest.getSignatures().stream().map(signature -> {
          String chainCode = null;
          String address = null;
          AccountIdentifier accountIdentifier = signature.getSigningPayload()
              .getAccountIdentifier();
          if (!ObjectUtils.isEmpty(accountIdentifier)) {
            AccountIdentifierMetadata accountIdentifierMetadata = accountIdentifier.getMetadata();
            if (!ObjectUtils.isEmpty(accountIdentifierMetadata)) {
              chainCode = accountIdentifierMetadata.getChainCode();
            }
            address = accountIdentifier.getAddress();
          }
          return new Signatures(signature.getHexBytes(), signature.getPublicKey().getHexBytes(),
              chainCode, address);
        }).toList(), extraData.getTransactionMetadataHex());
    log.info(signedTransaction + "[constructionCombine] About to return signed transaction");
    
    return new ConstructionCombineResponse(
        DataItemEncodeUtil.encodeExtraData(signedTransaction, extraData));
  }

  @Override
  public TransactionIdentifierResponse constructionHashService(
      ConstructionHashRequest constructionHashRequest) {
    Array array = cardanoService.decodeExtraData(constructionHashRequest.getSignedTransaction());
    log.info("[constructionHash] About to get hash of signed transaction");
    String transactionHash = cardanoService.getHashOfSignedTransaction(
        ((UnicodeString) array.getDataItems().get(0)).getString());
    log.info("[constructionHash] About to return hash of signed transaction");
    
    return new TransactionIdentifierResponse(new TransactionIdentifier(transactionHash));
  }

  @Override
  public TransactionIdentifierResponse constructionSubmitService(
      @NotNull ConstructionSubmitRequest constructionSubmitRequest) {
    Array array = cardanoService.decodeExtraData(constructionSubmitRequest.getSignedTransaction());
    byte[] signedTransactionBytes = HexUtil.decodeHexString(
        ((UnicodeString) array.getDataItems().get(0)).getString());
    TxSubmissionRequest txnRequest = new TxSubmissionRequest(signedTransactionBytes);
    redisTemplate.opsForValue().set(Constants.REDIS_PREFIX_PENDING + txnRequest.getTxHash(),
        constructionSubmitRequest.getSignedTransaction(), REDIS_TTL_MEMPOOL);
    TxResult txResult = localTxSubmissionClient.submitTx(txnRequest).block();
    if (txResult != null && !txResult.isAccepted()) {
      throw ExceptionFactory.submitRejected(txResult.getErrorCbor());
    }
    return new TransactionIdentifierResponse(new TransactionIdentifier(txResult.getTxHash()));

  }
}
