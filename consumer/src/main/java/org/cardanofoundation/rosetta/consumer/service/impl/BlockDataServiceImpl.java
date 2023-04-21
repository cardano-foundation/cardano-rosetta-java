package org.cardanofoundation.rosetta.consumer.service.impl;

import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBatchBlockData;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedBlockRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedTxRepository;
import org.cardanofoundation.rosetta.consumer.service.BlockDataService;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BlockDataServiceImpl implements BlockDataService {

  AggregatedBatchBlockData aggregatedBatchBlockData;

  CachedBlockRepository cachedBlockRepository;
  CachedTxRepository cachedTxRepository;

  @Override
  public Map<String, String> getStakeAddressTxHashMap() {
    return aggregatedBatchBlockData.getStakeAddressTxHashMap();
  }

  @Override
  public void saveFirstAppearedTxHashForStakeAddress(String stakeAddress, String txHash) {
    aggregatedBatchBlockData.getStakeAddressTxHashMap().putIfAbsent(stakeAddress, txHash);
  }

  @Override
  public void saveAggregatedBlock(AggregatedBlock aggregatedBlock) {
    aggregatedBatchBlockData.getAggregatedBlockMap()
        .put(aggregatedBlock.getHash(), aggregatedBlock);
  }

  @Override
  public void forEachAggregatedBlock(Consumer<AggregatedBlock> consumer) {
    aggregatedBatchBlockData.getAggregatedBlockMap().values().forEach(consumer);
  }

  @Override
  public Collection<AggregatedTx> getSuccessTxs() {
    return aggregatedBatchBlockData.getSuccessTxs();
  }

  @Override
  public void saveSuccessTx(AggregatedTx successTx) {
    aggregatedBatchBlockData.getSuccessTxs().add(successTx);
  }

  @Override
  public Collection<AggregatedTx> getFailedTxs() {
    return aggregatedBatchBlockData.getFailedTxs();
  }

  @Override
  public void saveFailedTx(AggregatedTx failedTx) {
    aggregatedBatchBlockData.getFailedTxs().add(failedTx);
  }

  /**
   * Use for log only because method will return new aggregated block if block map is empty *
   *
   * @return
   */
  public Pair<AggregatedBlock, AggregatedBlock> getFirstAndLastBlock() {
    AggregatedBlock first;
    AggregatedBlock last = new AggregatedBlock();
    var mBlock = aggregatedBatchBlockData.getAggregatedBlockMap();
    if (!mBlock.entrySet().iterator().hasNext()) {
      return Pair.of(new AggregatedBlock(), new AggregatedBlock());
    }
    first = mBlock.entrySet().iterator().next().getValue();
    for (Map.Entry<String, AggregatedBlock> entry : mBlock.entrySet()) {
      last = entry.getValue();
    }
    return Pair.of(first, last);
  }

  public int getBlockSize() {
    return aggregatedBatchBlockData.getAggregatedBlockMap().size();
  }

  @Transactional
  @Override
  public void saveAll() {
    long startTime = System.currentTimeMillis();
    aggregatedBatchBlockData.clear();
    cachedBlockRepository.flushToDb();
    cachedTxRepository.flushToDb();
    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    log.debug("Insertion time elapsed: {} ms, {} second(s)", totalTime, totalTime / 1000f);
  }
}
