package org.cardanofoundation.rosetta.consumer.service;

import org.cardanofoundation.rosetta.common.entity.ReferenceTxIn;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.entity.TxOut;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTxIn;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ReferenceInputService {

  /**
   * Handle reference tx inputs
   *
   * @param referenceTxInMap reference tx in map, with key is tx hash and value is associated tx's
   *                         reference inputs
   * @param txMap            a map with key is tx hash and value is the respective tx entity
   * @param newTxOutMap      a map of newly created txOut entities that are not inserted yet
   * @return a list of handled reference input entities
   */
  List<ReferenceTxIn> handleReferenceInputs(
      Map<String, Set<AggregatedTxIn>> referenceTxInMap, Map<String, Tx> txMap,
      Map<Pair<String, Short>, TxOut> newTxOutMap);
}
