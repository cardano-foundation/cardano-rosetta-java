package org.cardanofoundation.rosetta.api.service;

import com.bloxbean.cardano.client.crypto.bip32.key.HdPublicKey;
import org.cardanofoundation.rosetta.api.model.enumeration.AddressType;
import org.cardanofoundation.rosetta.api.model.enumeration.NetworkEnum;
import org.openapitools.client.model.PublicKey;

public interface CardanoAddressService {

    String getCardanoAddress(AddressType addressType, PublicKey stakingCredential, PublicKey publicKey, NetworkEnum networkEnum) throws IllegalAccessException;
    HdPublicKey getHdPublicKeyFromRosettaKey(PublicKey publicKey);
}
