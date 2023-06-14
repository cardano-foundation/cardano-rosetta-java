package org.cardanofoundation.rosetta.consumer.service;

import org.cardanofoundation.rosetta.common.entity.Redeemer;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.entity.TxOut;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.consumer.dto.RedeemerReference;
import org.springframework.data.util.Pair;

import java.util.Collection;
import java.util.Map;

public interface RedeemerService {

  /**
   * This method handles redeemers of a transaction
   *
   * @param txs          transaction bodies
   * @param txMap        transaction entity map
   * @param newTxOutMap  a map of newly created txOut entities that are not inserted yet
   * @return A map consists of:
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
  Map<RedeemerReference<?>, Redeemer> handleRedeemers( // NOSONAR
      Collection<AggregatedTx> txs, Map<String, Tx> txMap,
      Map<Pair<String, Short>, TxOut> newTxOutMap);
}
