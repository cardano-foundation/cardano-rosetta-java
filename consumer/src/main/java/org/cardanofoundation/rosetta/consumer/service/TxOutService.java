package org.cardanofoundation.rosetta.consumer.service;

import org.cardanofoundation.rosetta.common.entity.StakeAddress;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.entity.TxOut;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTxIn;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTxOut;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface TxOutService {

  /**
   * Get all tx outs can use from aggregated tx ins
   *
   * @param txIns aggregated tx ins for tx outs selection
   * @return collection of all tx outs can use
   */
  Collection<TxOut> getTxOutCanUseByAggregatedTxIns(Collection<AggregatedTxIn> txIns);

  /**
   * Prepare all tx outs data from aggregated tx outs, including asset tx outputs
   *
   * @param aggregatedTxOutMap a multivalued map with key is tx hash, and value is a list of
   *                           associated aggregated tx outs
   * @param txMap              a map with key is tx hash and value is the respective tx entity
   * @param stakeAddressMap    a map of stake address entities associated with all entities inside
   *                           the current block batch
   * @return collection of prepared tx outs
   */
  Collection<TxOut> prepareTxOuts(
      Map<String, List<AggregatedTxOut>> aggregatedTxOutMap, Map<String, Tx> txMap,
      Map<String, StakeAddress> stakeAddressMap);

}
