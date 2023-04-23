package org.cardanofoundation.rosetta.consumer.service.impl;

import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import com.sotatek.cardano.common.entity.Block;
import com.sotatek.cardano.common.entity.Tx;
import org.cardanofoundation.rosetta.consumer.service.BlockDataService;
import org.cardanofoundation.rosetta.consumer.service.TransactionService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {


  BlockDataService blockDataService;

  @Override
  public Map<String, Tx> prepareTxs(Block block, AggregatedBlock aggregatedBlock) {
    List<AggregatedTx> aggregatedTxList = aggregatedBlock.getTxList();

    if (CollectionUtils.isEmpty(aggregatedTxList)) {
      return Collections.emptyMap();
    }

    /*
     * For each aggregated tx, map it to a new tx entity.
     * Because script and datum need to be mapped to their first appeared tx, it is easier
     * to handle them here as the overall processing time for both of them is fast
     *
     * Also check if the currently processing aggregated tx's validity and push it to
     * either a queue of success txs or failed txs
     */
    var txList = aggregatedTxList.stream().map(aggregatedTx -> {
      Tx tx = new Tx();
      tx.setHash(aggregatedTx.getHash());
      tx.setBlock(block);
      tx.setBlockIndex(aggregatedTx.getBlockIndex());
      tx.setOutSum(aggregatedTx.getOutSum());
      tx.setFee(aggregatedTx.getFee());
      tx.setValidContract(aggregatedTx.isValidContract());
      tx.setDeposit(aggregatedTx.getDeposit());


      if (aggregatedTx.isValidContract()) {
        blockDataService.saveSuccessTx(aggregatedTx);
      } else {
        blockDataService.saveFailedTx(aggregatedTx);
      }
      return tx;
    }).collect(Collectors.toList());


    return txList.stream().collect(Collectors.toConcurrentMap(Tx::getHash, Function.identity()));
  }

  @Override
  public void handleTxs(Map<String, Tx> txMap) {
    Collection<AggregatedTx> successTxs = blockDataService.getSuccessTxs();
    Collection<AggregatedTx> failedTxs = blockDataService.getFailedTxs();
    // Handle extra key witnesses from required signers
    handleExtraKeyWitnesses(successTxs, failedTxs, txMap);

    // Handle Tx contents
    handleTxContents(successTxs, failedTxs, txMap);
  }

  private void handleTxContents(Collection<AggregatedTx> successTxs,
      Collection<AggregatedTx> failedTxs, Map<String, Tx> txMap) {
    if (CollectionUtils.isEmpty(successTxs) && CollectionUtils.isEmpty(failedTxs)) {
      return;
    }

    // MUST SET FIRST
    // multi asset mint
    long startTime = System.currentTimeMillis();
    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    log.trace("Multi asset mint handling time: {} ms, {} seconds", totalTime, totalTime / 1000f);

  }

  public void handleExtraKeyWitnesses(Collection<AggregatedTx> successTxs,
      Collection<AggregatedTx> failedTxs, Map<String, Tx> txMap) {

    Map<String, Tx> mWitnessTx = new ConcurrentHashMap<>();
    Set<String> hashCollection = new ConcurrentSkipListSet<>();

    /*
     * Map all extra key witnesses hashes to its respective tx and add them to a set
     * which will be used to find all existing hashes from database. The existing hashes
     * will be opted out
     *
     * This process will be done asynchronously
     */
    Stream.concat(successTxs.parallelStream(), failedTxs.parallelStream())
        .filter(aggregatedTx -> !CollectionUtils.isEmpty(aggregatedTx.getRequiredSigners()))
        .forEach(aggregatedTx -> {
          Tx tx = txMap.get(aggregatedTx.getHash());
          aggregatedTx.getRequiredSigners().parallelStream().forEach(hash -> {
            mWitnessTx.put(hash, tx);
            hashCollection.add(hash);
          });
        });

    if (CollectionUtils.isEmpty(hashCollection)) {
      return;
    }
  }
}
