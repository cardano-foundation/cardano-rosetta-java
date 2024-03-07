package org.cardanofoundation.rosetta.api.service.impl;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.UnicodeString;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.common.enumeration.AddressType;
import org.cardanofoundation.rosetta.api.model.ConstructionDeriveRequestMetadata;
import org.cardanofoundation.rosetta.api.model.PublicKey;
import org.cardanofoundation.rosetta.api.common.enumeration.NetworkEnum;
import org.cardanofoundation.rosetta.api.model.TransactionIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.*;
import org.cardanofoundation.rosetta.api.service.CardanoAddressService;
import org.cardanofoundation.rosetta.api.service.CardanoService;
import org.cardanofoundation.rosetta.api.service.ConstructionApiService;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConstructionApiServiceImpl implements ConstructionApiService {

    private final CardanoAddressService cardanoAddressService;
    private final CardanoService cardanoService;

    @Override
    public ConstructionDeriveResponse constructionDeriveService(ConstructionDeriveRequest constructionDeriveRequest) throws IllegalAccessException, CborSerializationException {
        PublicKey publicKey = constructionDeriveRequest.getPublicKey();
        log.info("Deriving address for public key: {}", publicKey);
        NetworkEnum network = NetworkEnum.fromValue(constructionDeriveRequest.getNetworkIdentifier().getNetwork());
        if (network == null)
            throw new IllegalAccessException("Invalid network");
        ConstructionDeriveRequestMetadata metadata = constructionDeriveRequest.getMetadata();
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
        return new ConstructionDeriveResponse(new AccountIdentifier(address));
    }

    @Override
    public ConstructionPreprocessResponse constructionPreprocessService(ConstructionPreprocessRequest constructionPreprocessRequest) throws IOException, AddressExcepion, CborSerializationException, CborException {
        return null;
    }

    @Override
    public ConstructionMetadataResponse constructionMetadataService(ConstructionMetadataRequest constructionMetadataRequest) throws CborException, CborSerializationException {
        return null;
    }

    @Override
    public ConstructionPayloadsResponse constructionPayloadsService(ConstructionPayloadsRequest constructionPayloadsRequest) throws Exception {
        return null;
    }

    @Override
    public ConstructionParseResponse constructionParseService(ConstructionParseRequest constructionParseRequest) throws Exception {
        return null;
    }

    @Override
    public ConstructionCombineResponse constructionCombineService(ConstructionCombineRequest constructionCombineRequest) throws CborException, CborSerializationException, JsonProcessingException {
        return null;
    }

    @Override
    public TransactionIdentifierResponse constructionHashService(ConstructionHashRequest constructionHashRequest) {
        Array array = cardanoService.decodeExtraData(constructionHashRequest.getSignedTransaction());
        log.info("[constructionHash] About to get hash of signed transaction");
        String transactionHash = cardanoService.getHashOfSignedTransaction(
                ((UnicodeString) array.getDataItems().get(0)).getString());
        log.info("[constructionHash] About to return hash of signed transaction");
        return new TransactionIdentifierResponse(new TransactionIdentifier(transactionHash));
    }

    @Override
    public TransactionIdentifierResponse constructionSubmitService(ConstructionSubmitRequest constructionSubmitRequest) throws CborDeserializationException, CborSerializationException, InterruptedException {
        return null;
    }
}
