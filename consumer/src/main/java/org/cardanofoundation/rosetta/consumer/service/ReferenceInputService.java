package org.cardanofoundation.rosetta.consumer.service;

import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTxIn;
import org.cardanofoundation.rosetta.common.entity.ReferenceTxIn;
import org.cardanofoundation.rosetta.common.entity.Tx;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ReferenceInputService {

  /**
   * Handle reference tx inputs
   *
   * @param referenceTxInMap reference tx in map, with key is tx hash and value is
   *                         associated tx's reference inputs
   * @param txMap            a map with key is tx hash and value is the respective
   *                         tx entity
   * @return                 a list of handled reference input entities
   */
  List<ReferenceTxIn> handleReferenceInputs(
      Map<String, Set<AggregatedTxIn>> referenceTxInMap, Map<String, Tx> txMap);
}
