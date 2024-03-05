package org.cardanofoundation.rosetta.api.service;

import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import org.cardanofoundation.rosetta.api.model.PublicKey;
import org.cardanofoundation.rosetta.api.model.enums.AddressTypeEnum;
import org.cardanofoundation.rosetta.api.model.enums.NetworkEnum;

public interface CardanoAddressService {

    String getCardanoAddress(AddressTypeEnum addressType, PublicKey stakingCredential, PublicKey publicKey, NetworkEnum networkEnum) throws IllegalAccessException;
    HdPublicKey getHdPublicKeyFromRosettaKey(PublicKey publicKey);
}
