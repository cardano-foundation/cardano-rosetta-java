package org.cardanofoundation.rosetta.consumer.aggregate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

@Getter
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AggregatedBatchBlockData {

  // Key is address (Bech32 or Base58 format)
  Map<String, AggregatedAddressBalance> aggregatedAddressBalanceMap;

  // Key is stake address hex, value is first appeared tx hash
  Map<String,  byte[]> stakeAddressTxHashMap;

  // Key is asset fingerprint, value is its first minted block no and tx index within that block
  Map<String, Pair<Long, Long>> fingerprintFirstAppearedMap;

  Map<byte[], AggregatedBlock> aggregatedBlockMap;
  Queue<AggregatedTx> successTxs;
  Queue<AggregatedTx> failedTxs;

  public AggregatedBatchBlockData() {
    aggregatedAddressBalanceMap = new ConcurrentHashMap<>();
    stakeAddressTxHashMap = new ConcurrentHashMap<>();
    fingerprintFirstAppearedMap = new ConcurrentHashMap<>();

    aggregatedBlockMap = new LinkedHashMap<>();
    successTxs = new ConcurrentLinkedQueue<>();
    failedTxs = new ConcurrentLinkedQueue<>();
  }

  // This method must be called every batch saving
  public void clear() {
    aggregatedAddressBalanceMap.clear();
    stakeAddressTxHashMap.clear();
    fingerprintFirstAppearedMap.clear();

    aggregatedBlockMap.clear();
    successTxs.clear();
    failedTxs.clear();
  }
}
