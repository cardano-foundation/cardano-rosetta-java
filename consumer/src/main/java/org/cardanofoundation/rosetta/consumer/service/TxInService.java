package org.cardanofoundation.rosetta.consumer.service;

import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTxIn;
import org.cardanofoundation.rosetta.common.entity.Redeemer;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.consumer.dto.RedeemerReference;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface TxInService {

  /**
   * Handle all tx ins data
   *
   * @param txs          collection of aggregated txs with tx ins data
   * @param txInMap      a multivalued tx in map with key is tx hash and value is
   *                     a set of associated tx ins
   * @param txMap        a map with key is tx hash and value is the respective tx entity
   * @param redeemersMap A map consists of:
   * <ul>
   *   <li>
   * - The key is a redeemer reference (a pair of redeemer tag - the
   * redeemer's purpose, and the target object that is considered a redeemer - can be a transaction
   * input, minting policy id, reward account or certificate)
   *   </li>
   *   <li>
   * - The value is the redeemer entity that was related to the redeemer reference
   *   </li>
   * </ul>
   */
  void handleTxIns(Collection<AggregatedTx> txs, Map<String, Set<AggregatedTxIn>> txInMap,
      Map<String, Tx> txMap, Map<RedeemerReference<?>, Redeemer> redeemersMap);
}
