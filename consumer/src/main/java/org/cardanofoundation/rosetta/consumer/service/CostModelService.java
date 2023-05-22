package org.cardanofoundation.rosetta.consumer.service;

import org.cardanofoundation.rosetta.common.entity.CostModel;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.springframework.transaction.annotation.Transactional;

public interface CostModelService {

  String PLUTUS_V1_KEY = "PlutusV1";
  String PLUTUS_V2_KEY = "PlutusV2";

  CostModel getGenesisCostModel();

  @Transactional
  void handleCostModel(AggregatedTx tx);

  CostModel findCostModelByHash(String hash);
}
