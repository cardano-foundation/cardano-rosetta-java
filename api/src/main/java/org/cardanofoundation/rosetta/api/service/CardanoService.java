package org.cardanofoundation.rosetta.api.service;

import org.cardanofoundation.rosetta.api.common.enumeration.EraAddressType;

public interface CardanoService {

  EraAddressType getEraAddressType(String address);

  boolean isStakeAddress(String address);

}
