package org.cardanofoundation.rosetta.consumer.service;

import org.springframework.transaction.annotation.Transactional;

public interface GenesisDataService {

  @Transactional
  void setupData();

}
