package org.cardanofoundation.rosetta.crawler.service;

import org.cardanofoundation.rosetta.crawler.common.enumeration.EraAddressType;
import org.cardanofoundation.rosetta.crawler.projection.EpochParamProjection;

public interface CardanoService {
    EraAddressType getEraAddressType(String address);
    boolean isStakeAddress(String address);
    EpochParamProjection getProtocolParameters();
}
