package org.cardanofoundation.rosetta.consumer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.common.entity.Block;
import org.cardanofoundation.rosetta.common.entity.Epoch;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.enumeration.EraType;
import org.cardanofoundation.rosetta.common.ledgersync.Era;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.consumer.constant.ConsumerConstant;
import org.cardanofoundation.rosetta.consumer.repository.EpochRepository;
import org.cardanofoundation.rosetta.consumer.repository.TxRepository;
import org.cardanofoundation.rosetta.consumer.service.EpochService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EpochServiceImpl implements EpochService {

  private final EpochRepository epochRepository;
  private final TxRepository txRepository;

  @Override
  public void handleEpoch(Collection<AggregatedBlock> aggregatedBlocks) {
    Map<Integer, Epoch> epochMap = new HashMap<>();
    aggregatedBlocks.forEach(aggregatedBlock -> handleEpoch(aggregatedBlock, epochMap));
    epochRepository.saveAll(epochMap.values());
  }

  private void handleEpoch(AggregatedBlock aggregatedBlock, Map<Integer, Epoch> epochMap) {
    final var epoch = Optional.ofNullable(epochMap.get(aggregatedBlock.getEpochNo()))
        .or(() -> epochRepository.findEpochByNo(aggregatedBlock.getEpochNo()));
    final var txSize = getTotalTxSize(aggregatedBlock.getTxList());
    final var fee = getTotalTxFee(aggregatedBlock.getTxList());
    final var outSum = getTxOutSum(aggregatedBlock.getTxList());
    final var blockTime = aggregatedBlock.getBlockTime();
    final var eraType = EraType.valueOf(aggregatedBlock.getEra().getValue());

    epoch.ifPresentOrElse(epochEntity -> {
      updateEpoch(fee, outSum, txSize, blockTime, epochEntity);
      epochMap.putIfAbsent(epochEntity.getNo(), epochEntity);
    }, () -> {
      var entityEpoch = Epoch.builder()
          .blkCount(BigInteger.ONE.intValue())
          .startTime(blockTime)
          .fees(fee)
          .outSum(outSum)
          .txCount(txSize)
          .no(aggregatedBlock.getEpochNo())
          .maxSlot(aggregatedBlock.getEra() == Era.BYRON ?
                   ConsumerConstant.BYRON_SLOT : ConsumerConstant.SHELLEY_SLOT)
          .era(eraType)
          .build();
      epochMap.put(entityEpoch.getNo(), entityEpoch);
    });
  }

  @Override
  @Transactional
  public void rollbackEpochStats(List<Block> rollbackBlocks) {
    var epochNoBlocksMap = rollbackBlocks.stream()
        .collect(Collectors.groupingBy(
            org.cardanofoundation.rosetta.common.entity.Block::getEpochNo,
            Collectors.toSet()));
    var epochBlocksMap = epochRepository.findAllByNoIn(epochNoBlocksMap.keySet())
        .stream()
        .collect(Collectors.toMap(
            Function.identity(), epoch -> epochNoBlocksMap.get(epoch.getNo())));

    // Update epoch stats according to its blocks
    epochBlocksMap.forEach((epoch, epochBlocksForDeletion) -> {
      int totalBlkCount = epochBlocksForDeletion.size();

      // Get required stats for rollback
      List<Tx> rollbackTxs = txRepository.findAllByBlockIn(epochBlocksForDeletion);
      BigInteger totalFees = rollbackTxs.stream().map(Tx::getFee)
          .reduce(BigInteger.ZERO, BigInteger::add);
      BigInteger totalOutSum = rollbackTxs.stream().map(Tx::getOutSum)
          .reduce(BigInteger.ZERO, BigInteger::add);

      // Update epoch's stats (subtract all stats)
      epoch.setBlkCount(epoch.getBlkCount() - totalBlkCount);
      epoch.setFees(epoch.getFees().subtract(totalFees));
      epoch.setOutSum(epoch.getOutSum().subtract(totalOutSum));
      epoch.setTxCount(epoch.getTxCount() - rollbackTxs.size());
      if (epoch.getBlkCount() == BigInteger.ZERO.intValue()) {
        epochRepository.delete(epoch);
      } else {
        Block minBlock = rollbackBlocks.stream()
            .filter(block -> block.getEpochNo().equals(epoch.getNo()))
            .min(Comparator.comparing(Block::getId)).get();

        epoch.setEndTime(minBlock.getTime());
        epochRepository.save(epoch);
      }
    });

    log.info("Epoch stats rollback finished");
  }

  private void updateEpoch(BigInteger fee, BigInteger outSum,
                           Integer txSize, Timestamp blockTime, Epoch entityEpoch) {
    entityEpoch.setBlkCount(entityEpoch.getBlkCount() + BigInteger.ONE.intValue());
    entityEpoch.setFees(entityEpoch.getFees().add(fee));
    entityEpoch.setOutSum(entityEpoch.getOutSum().add(outSum));
    entityEpoch.setTxCount(entityEpoch.getTxCount() + txSize);

    if (Objects.isNull(entityEpoch.getStartTime())) {
      entityEpoch.setStartTime(blockTime);
    }
    entityEpoch.setEndTime(blockTime);
  }

  private Integer getTotalTxSize(List<AggregatedTx> aggregatedTxList) {
    return CollectionUtils.isEmpty(aggregatedTxList) ? 0 : aggregatedTxList.size();
  }

  private BigInteger getTotalTxFee(List<AggregatedTx> aggregatedTxList) {
    if (CollectionUtils.isEmpty(aggregatedTxList)) {
      return BigInteger.ZERO;
    }

    return aggregatedTxList
        .stream()
        .map(AggregatedTx::getFee)
        .reduce(BigInteger.ZERO, BigInteger::add);
  }

  private BigInteger getTxOutSum(List<AggregatedTx> aggregatedTxList) {
    if (CollectionUtils.isEmpty(aggregatedTxList)) {
      return BigInteger.ZERO;
    }

    return aggregatedTxList.stream()
        .map(AggregatedTx::getOutSum)
        .reduce(BigInteger.ZERO, BigInteger::add);
  }
}
