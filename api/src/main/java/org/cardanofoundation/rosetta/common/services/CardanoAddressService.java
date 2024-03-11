package org.cardanofoundation.rosetta.common.services;

import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import org.cardanofoundation.rosetta.common.enumeration.AddressType;
import org.cardanofoundation.rosetta.common.enumeration.NetworkEnum;
import org.openapitools.client.model.PublicKey;

public interface CardanoAddressService {

    String getCardanoAddress(AddressType addressType, PublicKey stakingCredential, PublicKey publicKey, NetworkEnum networkEnum) throws IllegalAccessException;
    HdPublicKey getHdPublicKeyFromRosettaKey(PublicKey publicKey);
}
