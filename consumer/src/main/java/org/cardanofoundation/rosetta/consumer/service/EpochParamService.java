package org.cardanofoundation.rosetta.consumer.service;

import org.springframework.transaction.annotation.Transactional;

public interface EpochParamService {

  /**
   * Handle epoch params
   */
  @Transactional
  void handleEpochParams();
}
