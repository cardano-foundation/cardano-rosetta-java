package org.cardanofoundation.rosetta.consumer.service;

import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedAddressBalance;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import org.springframework.data.util.Pair;

public interface BlockDataService {

  /**
   * Get stake address with its first appeared tx hash map
   *
   * @return a map with key is stake address hex, value is first appeared tx hash
   */
  Map<String, String> getStakeAddressTxHashMap();

  /**
   * Save a stake address's first appeared tx hash
   *
   * @param stakeAddress    target stake address hex string
   * @param txHash          first appeared tx hash
   */
  void saveFirstAppearedTxHashForStakeAddress(String stakeAddress, String txHash);

  /**
   * Get aggregated address balance object from address string (Base58 or Bech32 form)
   * If there isn't any, create a new one, push it to aggregated address balance map
   * and return it
   *
   * @param address         address string (Base58 or Bech32 form)
   * @return                aggregated address balance object
   */
  AggregatedAddressBalance getAggregatedAddressBalanceFromAddress(String address);

  /**
   * Get aggregated address balance map
   *
   * @return a map with key is address string (Base58 or Bech32 form)
   *         and value is associated aggregated address balance object
   */
  Map<String, AggregatedAddressBalance> getAggregatedAddressBalanceMap();

  /**
   * Get an asset fingerprint's first appeared block no and tx idx
   *
   * @param fingerprint       target asset fingerprint
   * @return                  a pair of first appeared block no and that block's tx idx
   */
  Pair<Long, Long> getFingerprintFirstAppearedBlockNoAndTxIdx(String fingerprint);

  /**
   * Set an asset fingerprint's first appeared block no and tx idx
   *
   * @param fingerprint       target asset fingerprint
   * @param blockNo           asset's first appeared block no
   * @param txIdx             asset's first appeared tx idx within specified block no
   */
  void setFingerprintFirstAppearedBlockNoAndTxIdx(String fingerprint, Long blockNo, Long txIdx);

  /**
   * Get aggregated block object by its block hash
   *
   * @param blockHash         block hash
   * @return                  aggregated block object
   */
  AggregatedBlock getAggregatedBlock(String blockHash);

  /**
   * Save aggregated block object
   *
   * @param aggregatedBlock aggregated block object
   */
  void saveAggregatedBlock(AggregatedBlock aggregatedBlock);

  /**
   * Iterate between cached aggregated block objects
   *
   * @param consumer consumer that accepts aggregated block as input
   */
  void forEachAggregatedBlock(Consumer<AggregatedBlock> consumer);

  /**
   * Get all success aggregated txs
   *
   * @return success aggregated txs
   */
  Collection<AggregatedTx> getSuccessTxs();

  /**
   * Save success aggregated tx
   *
   * @param successTx success aggregated tx
   */
  void saveSuccessTx(AggregatedTx successTx);

  /**
   * Get all failed aggregated txs
   *
   * @return failed aggregated txs
   */
  Collection<AggregatedTx> getFailedTxs();

  /**
   * Save failed aggregated tx
   *
   * @param failedTx failed aggregated tx
   */
  void saveFailedTx(AggregatedTx failedTx);

  /**
   * Flush all cached entities to database
   */
  void saveAll();

  /**
   * Get first and last block in block map (for log only)
   */
  Pair<AggregatedBlock,AggregatedBlock> getFirstAndLastBlock();

  int getBlockSize();
}
