package org.cardanofoundation.rosetta.common.services;

import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import org.openapitools.client.model.PublicKey;

import org.cardanofoundation.rosetta.common.enumeration.AddressType;
import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;

public interface CardanoAddressService {

    String getCardanoAddress(AddressType addressType, PublicKey stakingCredential, PublicKey publicKey, NetworkEnum networkEnum);
    HdPublicKey getHdPublicKeyFromRosettaKey(PublicKey publicKey);
}
