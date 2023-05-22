package org.cardanofoundation.rosetta.consumer.service;

import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.common.entity.Block;
import java.math.BigInteger;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

public interface EpochService {

  /**
   * Handle epoch data of aggregated block
   *
   * @param aggregatedBlock aggregated block in process
   */
  @Transactional
  void handleEpoch(AggregatedBlock aggregatedBlock);

  /**
   * Add fee to existing epoch's total fee. This method is called when fee
   * is calculated later in tx ins handling service (currently used only for
   * Byron era)
   *
   * @param aggregatedBlock aggregated block containing target epoch no
   * @param fee             amount of fees to add
   */
  void addFee(AggregatedBlock aggregatedBlock, BigInteger fee);

  /**
   * Rollback epoch statistics from a list of blocks being rolled back
   *
   * @param rollbackBlocks list of blocks being rolled back
   */
  void rollbackEpochStats(List<Block> rollbackBlocks);
}
