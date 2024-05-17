package org.cardanofoundation.rosetta.common.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import com.bloxbean.cardano.client.util.HexUtil;
import org.openapitools.client.model.PublicKey;

import org.cardanofoundation.rosetta.common.enumeration.AddressType;
import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.services.CardanoAddressService;

@Service
@Slf4j
@RequiredArgsConstructor
public class CardanoAddressServiceImpl implements CardanoAddressService {

    public String getCardanoAddress(AddressType addressType, PublicKey stakingCredential, PublicKey publicKey, NetworkEnum networkEnum) {
        if(publicKey == null)
            throw ExceptionFactory.publicKeyMissing();
        String address;
        switch (addressType) {
            case BASE:
                log.debug("Deriving base address");
                if(stakingCredential == null)
                    throw ExceptionFactory.missingStakingKeyError();
                log.debug("Deriving base address with staking credential: {}", stakingCredential);
                Address baseAddress = AddressProvider.getBaseAddress(getHdPublicKeyFromRosettaKey(publicKey), getHdPublicKeyFromRosettaKey(stakingCredential), networkEnum.getNetwork());
                address = baseAddress.getAddress();
                break;
            case REWARD:
                log.debug("Deriving reward address");
                Address rewardAddress;
                if(stakingCredential == null) {
                    rewardAddress= AddressProvider.getRewardAddress(getHdPublicKeyFromRosettaKey(publicKey), networkEnum.getNetwork());
                    log.debug("Deriving reward address with staking credential: {}", publicKey);
                } else {
                    rewardAddress= AddressProvider.getRewardAddress(getHdPublicKeyFromRosettaKey(stakingCredential), networkEnum.getNetwork());
                    log.debug("Deriving reward address with staking credential: {}", stakingCredential);
                }
                address = rewardAddress.getAddress();
                break;
            case ENTERPRISE:
                log.info("Deriving enterprise address");
                Address enterpriseAddress = AddressProvider.getEntAddress(getHdPublicKeyFromRosettaKey(publicKey), networkEnum.getNetwork());
                address = enterpriseAddress.getAddress();
                break;
            default:
                log.error("Invalid address type: {}", addressType);
                throw ExceptionFactory.invalidAddressTypeError();
        }
        return address;
    }

    public HdPublicKey getHdPublicKeyFromRosettaKey(PublicKey publicKey) {
        byte[] pubKeyBytes = HexUtil.decodeHexString(publicKey.getHexBytes());
        HdPublicKey pubKey;
        if(pubKeyBytes.length == 32) {
            pubKey = new HdPublicKey();
            pubKey.setKeyData(pubKeyBytes);
        } else if(pubKeyBytes.length == 64) {
            pubKey = HdPublicKey.fromBytes(pubKeyBytes);
        } else {
            log.error("Invalid public key length: {}", pubKeyBytes.length);
            throw new IllegalArgumentException("Invalid public key length");
        }
        return pubKey;
    }
}
