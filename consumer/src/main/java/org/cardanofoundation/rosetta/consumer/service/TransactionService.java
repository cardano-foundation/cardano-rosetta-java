package org.cardanofoundation.rosetta.consumer.service;

import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.common.entity.Block;
import org.cardanofoundation.rosetta.common.entity.Tx;
import java.util.Map;

public interface TransactionService {

  /**
   * Prepare txs within a block and some of tx contents within each tx.
   * This method is used to initialize tx entities for other methods to
   * assign each tx content to its specific tx
   *
   * @param block             target block entity
   * @param aggregatedBlock   aggregated block object
   * @return                  a map with key is tx hash and value is the
   *                          respective tx entity
   */
  Map<byte[], Tx> prepareTxs(Block block, AggregatedBlock aggregatedBlock);

  /**
   * Handle all tx contents. Everything related to tx that needs to be
   * processed is done here
   *
   * @param txMap             a map with key is tx hash and value is
   *                          the respective tx entity
   */
  void handleTxs(Map<byte[], Tx> txMap);
}
