package org.cardanofoundation.rosetta.api.service;

import org.cardanofoundation.rosetta.api.common.enumeration.EraAddressType;
import org.cardanofoundation.rosetta.api.model.ProtocolParameters;
import org.cardanofoundation.rosetta.api.projection.EpochParamProjection;

public interface CardanoService {
    EraAddressType getEraAddressType(String address);
    boolean isStakeAddress(String address);
    ProtocolParameters getProtocolParameters();
}
