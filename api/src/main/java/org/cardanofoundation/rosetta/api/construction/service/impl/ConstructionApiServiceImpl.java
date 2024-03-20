package org.cardanofoundation.rosetta.api.construction.service.impl;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.UnicodeString;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.block.model.entity.ProtocolParams;
import org.cardanofoundation.rosetta.common.enumeration.NetworkIdentifierType;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.common.enumeration.AddressType;

import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;
import org.cardanofoundation.rosetta.common.services.CardanoAddressService;
import org.cardanofoundation.rosetta.common.services.CardanoService;
import org.cardanofoundation.rosetta.api.construction.service.ConstructionApiService;
import org.cardanofoundation.rosetta.common.services.LedgerDataProviderService;
import org.cardanofoundation.rosetta.common.util.Constants;
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
  private final DataMapper dataMapper;

  @Override
  public ConstructionDeriveResponse constructionDeriveService(
      ConstructionDeriveRequest constructionDeriveRequest)
      throws IllegalAccessException {
    PublicKey publicKey = constructionDeriveRequest.getPublicKey();
    log.info("Deriving address for public key: {}", publicKey);
    NetworkEnum network = Optional.ofNullable(NetworkEnum.fromValue(
        constructionDeriveRequest.getNetworkIdentifier().getNetwork())).orElseThrow(() -> new IllegalAccessException("Invalid network"));
    // casting unspecific rosetta specification to cardano specific metadata
    ConstructionDeriveMetadata metadata = constructionDeriveRequest.getMetadata();
    // Default address type is enterprise
    AddressType addressType =
        metadata.getAddressType() != null ? AddressType.findByValue(metadata.getAddressType())
            : null;
    addressType = addressType != null ? addressType : AddressType.ENTERPRISE;

    PublicKey stakingCredential = null;
    if (addressType == AddressType.BASE) {
      stakingCredential = Optional.ofNullable(metadata.getStakingCredential())
          .orElseThrow(() -> new IllegalAccessException("Staking credential is required for base address"));
    }
    String address = cardanoAddressService.getCardanoAddress(addressType, stakingCredential,
        publicKey, network);
    return new ConstructionDeriveResponse(null, new AccountIdentifier(address, null, null), null);
  }

  @Override
  public ConstructionPreprocessResponse constructionPreprocessService(
      ConstructionPreprocessRequest constructionPreprocessRequest)
      throws IOException, AddressExcepion, CborSerializationException, CborException {
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
      ConstructionMetadataRequest constructionMetadataRequest)
      throws CborException, CborSerializationException {
    Map<String, Object> options = (Map<String, Object>) constructionMetadataRequest.getOptions();
    Double relativeTtl = (Double) options.get(Constants.RELATIVE_TTL);
    Double txSize = (Double) options.get(Constants.TRANSACTION_SIZE);
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
    return null;
  }

  @Override
  public ConstructionParseResponse constructionParseService(
      ConstructionParseRequest constructionParseRequest) {
    return null;
  }

  @Override
  public ConstructionCombineResponse constructionCombineService(
      ConstructionCombineRequest constructionCombineRequest) {
    return null;
  }

  @Override
  public TransactionIdentifierResponse constructionHashService(
      ConstructionHashRequest constructionHashRequest) {
    Array array = cardanoService.decodeExtraData(constructionHashRequest.getSignedTransaction());
    log.info("[constructionHash] About to get hash of signed transaction");
    String transactionHash = cardanoService.getHashOfSignedTransaction(
        ((UnicodeString) array.getDataItems().getFirst()).getString());
    log.info("[constructionHash] About to return hash of signed transaction");
    return new TransactionIdentifierResponse(new TransactionIdentifier(transactionHash), null);
  }

  @Override
  public TransactionIdentifierResponse constructionSubmitService(
      ConstructionSubmitRequest constructionSubmitRequest) {
    return null;
  }
}
