package org.cardanofoundation.rosetta.crawler.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.crawler.common.enumeration.EraAddressType;
import org.cardanofoundation.rosetta.crawler.service.CardanoService;
import org.cardanofoundation.rosetta.crawler.service.LedgerDataProviderService;
import org.cardanofoundation.rosetta.crawler.util.CardanoAddressUtils;
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
