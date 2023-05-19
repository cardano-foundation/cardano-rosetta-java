package org.cardanofoundation.rosetta.api.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.common.enumeration.EraAddressType;
import org.cardanofoundation.rosetta.api.service.CardanoService;
import org.cardanofoundation.rosetta.api.service.LedgerDataProviderService;
import org.cardanofoundation.rosetta.api.util.CardanoAddressUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CardanoServiceImpl implements CardanoService {


  @Autowired
  LedgerDataProviderService ledgerDataProviderService;
  @Override
  public EraAddressType getEraAddressType(String address) {
    try {
      return CardanoAddressUtils.getEraAddressType(address);
    } catch (Exception e) {
      return null;
    }
  }
  @Override
  public boolean isStakeAddress(String address) {
    return CardanoAddressUtils.isStakeAddress(address);
  }


}
