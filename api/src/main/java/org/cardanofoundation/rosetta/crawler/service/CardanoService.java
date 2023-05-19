package org.cardanofoundation.rosetta.crawler.service;

import org.cardanofoundation.rosetta.crawler.common.enumeration.EraAddressType;

public interface CardanoService {

  EraAddressType getEraAddressType(String address);

  boolean isStakeAddress(String address);

}
