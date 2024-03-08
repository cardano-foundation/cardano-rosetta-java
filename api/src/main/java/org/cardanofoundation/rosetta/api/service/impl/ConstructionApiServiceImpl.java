package org.cardanofoundation.rosetta.api.service.impl;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.UnicodeString;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.mapper.DataMapper;
import org.cardanofoundation.rosetta.api.model.entity.ProtocolParams;
import org.cardanofoundation.rosetta.api.model.enumeration.AddressType;
import org.cardanofoundation.rosetta.api.model.cardano.ConstructionDeriveRequestMetadata;

import org.cardanofoundation.rosetta.api.model.enumeration.NetworkEnum;
import org.cardanofoundation.rosetta.api.service.CardanoAddressService;
import org.cardanofoundation.rosetta.api.service.CardanoService;
import org.cardanofoundation.rosetta.api.service.ConstructionApiService;
import org.cardanofoundation.rosetta.api.service.LedgerDataProviderService;
import org.openapitools.client.model.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
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
    public ConstructionDeriveResponse constructionDeriveService(ConstructionDeriveRequest constructionDeriveRequest) throws IllegalAccessException, CborSerializationException {
        PublicKey publicKey = constructionDeriveRequest.getPublicKey();
        log.info("Deriving address for public key: {}", publicKey);
        NetworkEnum network = NetworkEnum.fromValue(constructionDeriveRequest.getNetworkIdentifier().getNetwork());
        if (network == null)
            throw new IllegalAccessException("Invalid network");

        ConstructionDeriveRequestMetadata metadata = ConstructionDeriveRequestMetadata.fromHashMap((HashMap<String,Object>) constructionDeriveRequest.getMetadata());
        // Default address type is enterprise
        AddressType addressType = metadata != null ? AddressType.findByValue(metadata.getAddressType()) : null;
        addressType = addressType != null ? addressType : AddressType.ENTERPRISE;

        PublicKey stakingCredential = null;
        if(addressType == AddressType.BASE) {
            if(metadata.getStakingCredential() == null)
                throw new IllegalAccessException("Staking credential is required for base address");
            stakingCredential = metadata.getStakingCredential();
        }
        String address = cardanoAddressService.getCardanoAddress(addressType, stakingCredential, publicKey, network);
        return new ConstructionDeriveResponse(null, new AccountIdentifier(address, null, null), null);
    }

    @Override
    public ConstructionPreprocessResponse constructionPreprocessService(ConstructionPreprocessRequest constructionPreprocessRequest) throws IOException, AddressExcepion, CborSerializationException, CborException {
        return null;
    }

    @Override
    public ConstructionMetadataResponse constructionMetadataService(ConstructionMetadataRequest constructionMetadataRequest) throws CborException, CborSerializationException {
        Map<String, Object> options = (Map<String, Object>) constructionMetadataRequest.getOptions();
        Double relativeTtl = (Double) options.get("relative_ttl");
        Double txSize = (Double) options.get("transaction_size");
        log.debug("[constructionMetadata] Calculating ttl based on {} relative ttl", relativeTtl);
        Long ttl = cardanoService.calculateTtl(relativeTtl.longValue());
        log.debug("[constructionMetadata] ttl is {}", ttl);
        log.debug("[constructionMetadata] updating tx size from {}", txSize);
        Long updatedTxSize = cardanoService.updateTxSize(txSize.longValue(), 0L, ttl);
        log.debug("[constructionMetadata] updated txSize size is ${updatedTxSize}");
        ProtocolParams protocolParams = ledgerService.findProtocolParameters();
        log.debug("[constructionMetadata] received protocol parameters from block-service {}",
                protocolParams);
        Long suggestedFee = cardanoService.calculateTxMinimumFee(updatedTxSize,
                protocolParams);
        log.debug("[constructionMetadata] suggested fee is ${suggestedFee}");
        return DataMapper.mapToMetadataResponse(protocolParams, ttl, suggestedFee);
    }

    @Override
    public ConstructionPayloadsResponse constructionPayloadsService(ConstructionPayloadsRequest constructionPayloadsRequest) {
        return null;
    }

    @Override
    public ConstructionParseResponse constructionParseService(ConstructionParseRequest constructionParseRequest) {
        return null;
    }

    @Override
    public ConstructionCombineResponse constructionCombineService(ConstructionCombineRequest constructionCombineRequest) {
        return null;
    }

    @Override
    public TransactionIdentifierResponse constructionHashService(ConstructionHashRequest constructionHashRequest) {
        Array array = cardanoService.decodeExtraData(constructionHashRequest.getSignedTransaction());
        log.info("[constructionHash] About to get hash of signed transaction");
        String transactionHash = cardanoService.getHashOfSignedTransaction(
                ((UnicodeString) array.getDataItems().get(0)).getString());
        log.info("[constructionHash] About to return hash of signed transaction");
        return new TransactionIdentifierResponse(new TransactionIdentifier(transactionHash), null);
    }

    @Override
    public TransactionIdentifierResponse constructionSubmitService(ConstructionSubmitRequest constructionSubmitRequest)  {
        return null;
    }
}
