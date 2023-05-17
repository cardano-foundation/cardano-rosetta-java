package org.cardanofoundation.rosetta.consumer.service;

import java.util.Collection;
import java.util.Map;
import org.cardanofoundation.rosetta.common.entity.Redeemer;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.consumer.dto.RedeemerReference;

public interface WithdrawalsService {

  /**
   * Handle all withdrawals data
   *
   * @param successTxs   collection of success txs containing withdrawals data
   * @param txMap        a map with key is tx hash and value is the respective tx entity
   * @param redeemersMap redeemers map with key is the redeemer reference (in this case is the
   *                     reward account), and value is associated redeemer entity
   */
  void handleWithdrawal(Collection<AggregatedTx> successTxs,
      Map<String, Tx> txMap, Map<RedeemerReference<?>, Redeemer> redeemersMap);
}
