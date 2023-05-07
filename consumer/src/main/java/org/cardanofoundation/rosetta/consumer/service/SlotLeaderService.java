package org.cardanofoundation.rosetta.consumer.service;

import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedSlotLeader;
import org.cardanofoundation.rosetta.common.entity.SlotLeader;
import org.cardanofoundation.rosetta.common.ledgersync.Block;
import org.cardanofoundation.rosetta.common.ledgersync.byron.ByronMainBlock;

public interface SlotLeaderService {
  /**
   * find slot leader after Byron era with issuerVkey,
   *
   * @param blockCddl block {@link Block}
   * @return
   */
  AggregatedSlotLeader getSlotLeaderHashAndPrefix(Block blockCddl);

  /**
   * find slot leader in Byron era pubKey
   *
   * @param blockCddl
   * @return
   */
  AggregatedSlotLeader getSlotLeaderHashAndPrefix(ByronMainBlock blockCddl);

  /**
   * Get slot leader entity by its raw hash and prefix
   *
   * @param hashRaw raw hash
   * @param prefix  prefix string
   * @return slot leader entity
   */
  SlotLeader getSlotLeader(String hashRaw, String prefix);
}
