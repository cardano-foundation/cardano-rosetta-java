package org.cardanofoundation.rosetta.consumer.service;

import org.cardanofoundation.rosetta.common.entity.StakeAddress;
import org.cardanofoundation.rosetta.common.entity.Tx;
import java.util.Map;

public interface StakeAddressService {

  /**
   * Get stake address entity from stake address hex
   *
   * @param stakeAddressHex         stake address hex string
   * @return
   */
  StakeAddress getStakeAddress(String stakeAddressHex);

  /**
   * Handle stake addresses along with its first appeared tx
   *
   * @param stakeAddressTxHashMap  a map with key is the stake address and value is
   *                               its first appeared tx hash
   * @param txMap                  a map with key is tx hash and value is the
   *                               respective tx entity
   */
  void handleStakeAddressesFromTxs(
      Map<String, byte[]> stakeAddressTxHashMap, Map<byte[], Tx> txMap);
}
