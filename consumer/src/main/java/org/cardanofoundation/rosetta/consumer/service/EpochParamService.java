package org.cardanofoundation.rosetta.consumer.service;

import org.cardanofoundation.rosetta.common.entity.EpochParam;
import org.springframework.transaction.annotation.Transactional;

public interface EpochParamService {

  void setDefShelleyEpochParam(
          EpochParam defShelleyEpochParam);

  void setDefAlonzoEpochParam(
          EpochParam defAlonzoEpochParam);

  /**
   * Handle epoch params
   */
  @Transactional
  void handleEpochParams();
}
