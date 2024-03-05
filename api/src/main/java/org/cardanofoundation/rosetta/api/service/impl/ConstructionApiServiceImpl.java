package org.cardanofoundation.rosetta.api.service.impl;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.address.util.AddressEncoderDecoderUtil;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.model.ConstructionDeriveRequestMetadata;

import org.cardanofoundation.rosetta.api.model.DepositParameters;
import org.cardanofoundation.rosetta.api.model.Operation;
import org.cardanofoundation.rosetta.api.model.PublicKey;
import org.cardanofoundation.rosetta.api.model.enums.AddressTypeEnum;
import org.cardanofoundation.rosetta.api.model.enums.NetworkEnum;
import org.cardanofoundation.rosetta.api.model.rest.*;
import org.cardanofoundation.rosetta.api.service.CardanoAddressService;
import org.cardanofoundation.rosetta.api.service.ConstructionApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConstructionApiServiceImpl implements ConstructionApiService {

    private final CardanoAddressService cardanoAddressService;

    @Override
    public ConstructionDeriveResponse constructionDeriveService(ConstructionDeriveRequest constructionDeriveRequest) throws IllegalAccessException, CborSerializationException {
        PublicKey publicKey = constructionDeriveRequest.getPublicKey();
        log.info("Deriving address for public key: {}", publicKey);
        NetworkEnum network = NetworkEnum.fromValue(constructionDeriveRequest.getNetworkIdentifier().getNetwork());
        if (network == null)
            throw new IllegalAccessException("Invalid network");
        ConstructionDeriveRequestMetadata metadata = constructionDeriveRequest.getMetadata();
        // Default address type is enterprise
        AddressTypeEnum addressType = metadata != null ? AddressTypeEnum.fromValue(metadata.getAddressType()) : null;
        addressType = addressType != null ? addressType : AddressTypeEnum.ENTERPRISE;

        PublicKey stakingCredential = null;
        if(addressType == AddressTypeEnum.BASE) {
            if(metadata == null || metadata.getStakingCredential() == null)
                throw new IllegalAccessException("Staking credential is required for base address");
            stakingCredential = metadata.getStakingCredential();
        }
        String address = cardanoAddressService.getCardanoAddress(addressType, stakingCredential, publicKey, network);
        return new ConstructionDeriveResponse(new AccountIdentifier(address));
    }

    @Override
    public ConstructionPreprocessResponse constructionPreprocessService(ConstructionPreprocessRequest constructionPreprocessRequest) throws IOException, AddressExcepion, CborSerializationException, CborException {
        NetworkIdentifier networkIdentifier = constructionPreprocessRequest.getNetworkIdentifier();
        Double relativeTtl = constructionPreprocessRequest.getMetadata().getRelativeTtl();
        DepositParameters depositParameters = constructionPreprocessRequest.getMetadata().getDepositParameters();
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

        return null;
    }

    @Override
    public TransactionIdentifierResponse constructionSubmitService(ConstructionSubmitRequest constructionSubmitRequest) throws CborDeserializationException, CborSerializationException, InterruptedException {
        return null;
    }
}
