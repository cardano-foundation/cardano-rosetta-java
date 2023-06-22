package org.cardanofoundation.rosetta.api.service.impl;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.UnicodeString;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.cardano.yaci.core.protocol.localtx.model.TxSubmissionRequest;
import com.bloxbean.cardano.yaci.helper.LocalTxSubmissionClient;
import com.bloxbean.cardano.yaci.helper.model.TxResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
import org.cardanofoundation.rosetta.common.ledgersync.constant.Constant;
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
  private CardanoService cardanoService;

  @Autowired
  private LocalTxSubmissionClient localTxSubmissionClient;

  @Autowired
  @Qualifier("redisTemplateString")
  private RedisTemplate<String, String> redisTemplate;

//  public ConstructionApiServiceImpl(CardanoService cardanoService,
//      LocalTxSubmissionClient localTxSubmissionClient,
//      @Qualifier("redisTemplateString") RedisTemplate<String, String> redisTemplateString) {
//    this.cardanoService = cardanoService;
//    this.localTxSubmissionClient = localTxSubmissionClient;
//    this.redisTemplate = redisTemplateString;
//  }

  @Override
  public ConstructionDeriveResponse constructionDeriveService(
      ConstructionDeriveRequest constructionDeriveRequest)
      throws IllegalAccessException, CborSerializationException {
    PublicKey publicKey = constructionDeriveRequest.getPublicKey();
    NetworkIdentifierType networkIdentifier = cardanoService.getNetworkIdentifierByRequestParameters(
        constructionDeriveRequest.getNetworkIdentifier());

    log.info("[constructionDerive] About to check if public key has valid length and curve type");
    if (!cardanoService.isKeyValid(publicKey.getHexBytes(), publicKey.getCurveType())) {
      log.info("[constructionDerive] Public key has an invalid format");
      throw ExceptionFactory.invalidPublicKeyFormat();
    }
    log.info("[constructionDerive] Public key has a valid format");

    // eslint-disable-next-line camelcase
    PublicKey stakingCredential =
        ObjectUtils.isEmpty(constructionDeriveRequest.getMetadata()) ? null
            : constructionDeriveRequest.getMetadata().getStakingCredential();
    if (!ObjectUtils.isEmpty(stakingCredential)) {
      log.info(
          "[constructionDerive] About to check if staking credential has valid length and curve type");
      if (!cardanoService.isKeyValid(stakingCredential.getHexBytes(),
          stakingCredential.getCurveType())) {
        log.info("[constructionDerive] Staking credential has an invalid format");
        throw ExceptionFactory.invalidStakingKeyFormat();
      }
      log.info("[constructionDerive] Staking credential key has a valid format");
    }

    // eslint-disable-next-line camelcase
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
    String address = cardanoService.generateAddress(
        networkIdentifier,
        publicKey.getHexBytes(),
        // eslint-disable-next-line camelcase
        ObjectUtils.isEmpty(stakingCredential) ? null : stakingCredential.getHexBytes(),
        AddressType.findByValue(addressType)
    );
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
    // eslint-disable-next-line camelcase
    Double relativeTtl = cardanoService.calculateRelativeTtl(
        !ObjectUtils.isEmpty(constructionPreprocessRequest.getMetadata())
            ? constructionPreprocessRequest.getMetadata().getRelativeTtl() : null);
    Double transactionSize = cardanoService.calculateTxSize(
        networkIdentifier,
        (ArrayList<Operation>) constructionPreprocessRequest.getOperations(),
        0,
        ObjectUtils.isEmpty(constructionPreprocessRequest.getMetadata()) ? null
            : constructionPreprocessRequest.getMetadata().getDepositParameters()
    );
    // eslint-disable-next-line camelcase
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
    // eslint-disable-next-line camelcase
    ProtocolParameters protocol_parameters = new ProtocolParameters();
    protocol_parameters.setCoinsPerUtxoSize(
        protocolParametersResponse.getCoinsPerUtxoSize().toString());
    protocol_parameters.setMaxTxSize(protocolParametersResponse.getMaxTxSize());
    protocol_parameters.setMaxValSize(protocolParametersResponse.getMaxValSize());
    protocol_parameters.setKeyDeposit(protocolParametersResponse.getKeyDeposit().toString());
    protocol_parameters.setMaxCollateralInputs(protocolParametersResponse.getMaxCollateralInputs());
    protocol_parameters.setMinFeeConstant(protocolParametersResponse.getMinFeeConstant());
    protocol_parameters.setMinFeeCoefficient(protocolParametersResponse.getMinFeeCoefficient());
    protocol_parameters.setMinPoolCost(protocolParametersResponse.getMinPoolCost().toString());
    protocol_parameters.setPoolDeposit(protocolParametersResponse.getPoolDeposit().toString());
    protocol_parameters.setProtocol(protocolParametersResponse.getProtocol());
    return new ConstructionMetadataResponse(
        new ConstructionMetadataResponseMetadata(ttl.toString(), protocol_parameters),
        new ArrayList<>(
            List.of(cardanoService.mapAmount(suggestedFee.toString(), null, null, null))));
  }

  @Override
  public ConstructionPayloadsResponse constructionPayloadsService(
      ConstructionPayloadsRequest constructionPayloadsRequest)
      throws IOException, CborException, CborSerializationException, AddressExcepion {
    String ttl = constructionPayloadsRequest.getMetadata().getTtl();
    List<Operation> operations = constructionPayloadsRequest.getOperations();
    NetworkIdentifierType networkIdentifier = cardanoService.getNetworkIdentifierByRequestParameters(
        constructionPayloadsRequest.getNetworkIdentifier());
    log.info(operations + "[constuctionPayloads] Operations about to be processed");
    ProtocolParameters protocolParameters = constructionPayloadsRequest.getMetadata()
        .getProtocolParameters();
    UnsignedTransaction unsignedTransaction = cardanoService.createUnsignedTransaction(
        networkIdentifier,
        operations,
        Integer.parseInt(ttl),
        new DepositParameters(protocolParameters.getKeyDeposit(),
            protocolParameters.getPoolDeposit())
    );
    List<SigningPayload> payloads = cardanoService.constructPayloadsForTransactionBody(
        unsignedTransaction.getHash(), unsignedTransaction.getAddresses());
    String unsignedTransactionString = cardanoService.encodeExtraData(
        unsignedTransaction.getBytes(),
        new TransactionExtraData(constructionPayloadsRequest.getOperations(),
            unsignedTransaction.getMetadata()));
    return new ConstructionPayloadsResponse(unsignedTransactionString, payloads);
  }

  @Override
  public ConstructionParseResponse constructionParseService(
      ConstructionParseRequest constructionParseRequest)
      throws Exception {
    Boolean signed = constructionParseRequest.getSigned();
    if (signed == null) {
      throw new Exception("body should have required property signed");
    }
    NetworkIdentifierType networkIdentifier = cardanoService.getNetworkIdentifierByRequestParameters(
        constructionParseRequest.getNetworkIdentifier());
    log.info(constructionParseRequest.getTransaction() + "[constructionParse] Processing");
    Array array = cardanoService.decodeExtraData(constructionParseRequest.getTransaction());
    TransactionExtraData extraData = cardanoService.changeFromMaptoObject(
        (Map) array.getDataItems().get(1));
    log.info(array + "[constructionParse] Decoded");
    TransactionParsed result;
    if (signed) {
      result = cardanoService.parseSignedTransaction(networkIdentifier,
          ((UnicodeString) array.getDataItems().get(0)).getString(), extraData);
      return new ConstructionParseResponse(null, result.getOperations(),
          result.getAccount_identifier_signers());
    }
    result = cardanoService.parseUnsignedTransaction(networkIdentifier,
        ((UnicodeString) array.getDataItems().get(0)).getString(), extraData);
    return new ConstructionParseResponse(null, result.getOperations(),
        result.getAccount_identifier_signers());
  }

  @Override
  public ConstructionCombineResponse constructionCombineService(
      ConstructionCombineRequest constructionCombineRequest)
      throws CborException, CborSerializationException, JsonProcessingException {
    log.info("[constructionCombine] Request received to sign a transaction");
    Array array = cardanoService.decodeExtraData(
        constructionCombineRequest.getUnsignedTransaction());
    TransactionExtraData extraData = cardanoService.changeFromMaptoObject(
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
          return new Signatures(signature.getHexBytes(),
              signature.getPublicKey().getHexBytes(),
              chainCode, address);
        }).collect(Collectors.toList()),
        extraData.getTransactionMetadataHex()
    );
    log.info(signedTransaction + "[constructionCombine] About to return signed transaction");
    // eslint-disable-next-line camelcase
    return new ConstructionCombineResponse(
        cardanoService.encodeExtraData(signedTransaction, extraData));
  }

  @Override
  public TransactionIdentifierResponse constructionHashService(
      ConstructionHashRequest constructionHashRequest) {
    Array array = cardanoService.decodeExtraData(constructionHashRequest.getSignedTransaction());
    log.info("[constructionHash] About to get hash of signed transaction");
    String transactionHash = cardanoService.getHashOfSignedTransaction(
        ((UnicodeString) array.getDataItems().get(0)).getString());
    log.info("[constructionHash] About to return hash of signed transaction");
    // eslint-disable-next-line camelcase
    return cardanoService.mapToConstructionHashResponse(transactionHash);
  }

  @Override
  public TransactionIdentifierResponse constructionSubmitService(
      @NotNull ConstructionSubmitRequest constructionSubmitRequest)
      throws CborDeserializationException, CborSerializationException {
    Array array = cardanoService.decodeExtraData(constructionSubmitRequest.getSignedTransaction());
    byte[] signedTransactionBytes = HexUtil.decodeHexString(
        ((UnicodeString) array.getDataItems().get(0)).getString());
    Transaction parsed = Transaction.deserialize(signedTransactionBytes);
    TxSubmissionRequest txnRequest = new TxSubmissionRequest(signedTransactionBytes);
      redisTemplate
              .opsForValue()
              .set(Constants.REDIS_PREFIX_PENDING + txnRequest.getTxHash(), constructionSubmitRequest.getSignedTransaction());
    TxResult txResult = localTxSubmissionClient.submitTx(txnRequest).block();
    if (!txResult.isAccepted()) {
      throw ExceptionFactory.submitRejected();
    }
    return cardanoService.mapToConstructionHashResponse(txResult.getTxHash());

  }
}
