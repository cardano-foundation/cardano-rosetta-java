package org.cardanofoundation.rosetta.api.service.impl;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.model.PublicKey;
import org.cardanofoundation.rosetta.api.model.enums.AddressTypeEnum;
import org.cardanofoundation.rosetta.api.model.enums.NetworkEnum;
import org.cardanofoundation.rosetta.api.service.CardanoAddressService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CardanoAddressServiceImpl implements CardanoAddressService {

    public String getCardanoAddress(AddressTypeEnum addressType, PublicKey stakingCredential, PublicKey publicKey, NetworkEnum networkEnum) throws IllegalAccessException {
        if(publicKey == null)
            throw new IllegalAccessException("Public key is required");
        String address;
        switch (addressType) {
            case BASE:
                log.debug("Deriving base address");
                if(stakingCredential == null)
                    throw new IllegalAccessException("Staking credential is required for base address");
                log.debug("Deriving base address with staking credential: {}", stakingCredential);
                Address baseAddress = AddressProvider.getBaseAddress(HdPublicKey.fromBytes(publicKey.getHexBytes().getBytes()), HdPublicKey.fromBytes(stakingCredential.getHexBytes().getBytes()), networkEnum.getNetwork());
                address = baseAddress.getAddress();
                break;
            case REWARD:
                log.debug("Deriving reward address");
                Address rewardAddress;
                if(stakingCredential == null) {
                    rewardAddress= AddressProvider.getRewardAddress(HdPublicKey.fromBytes(publicKey.getHexBytes().getBytes()), networkEnum.getNetwork());
                    log.debug("Deriving reward address with staking credential: {}", publicKey);
                } else {
                    rewardAddress= AddressProvider.getRewardAddress(HdPublicKey.fromBytes(stakingCredential.getHexBytes().getBytes()), networkEnum.getNetwork());
                    log.debug("Deriving reward address with staking credential: {}", stakingCredential);
                }
                address = rewardAddress.getAddress();
                break;
            case ENTERPRISE:
                log.info("Deriving enterprise address");
                Address enterpriseAddress = AddressProvider.getEntAddress(HdPublicKey.fromBytes(publicKey.getHexBytes().getBytes()), networkEnum.getNetwork());
                address = enterpriseAddress.getAddress();
                break;
            default:
                log.error("Invalid address type: {}", addressType);
                throw new IllegalAccessException("Invalid address type");
        }
        return address;
    }
}
