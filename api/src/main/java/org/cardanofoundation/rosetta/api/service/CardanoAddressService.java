package org.cardanofoundation.rosetta.api.service;

import com.bloxbean.cardano.client.common.model.Network;
import org.cardanofoundation.rosetta.api.model.PublicKey;
import org.cardanofoundation.rosetta.api.model.enums.AddressTypeEnum;
import org.cardanofoundation.rosetta.api.model.enums.NetworkEnum;

public interface CardanoAddressService {

    String getCardanoAddress(AddressTypeEnum addressType, PublicKey stakingCredential, PublicKey publicKey, NetworkEnum networkEnum) throws IllegalAccessException;
}
