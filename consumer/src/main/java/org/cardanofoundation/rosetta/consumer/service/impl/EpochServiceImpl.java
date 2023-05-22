package org.cardanofoundation.rosetta.consumer.service.impl;

import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.common.entity.Block;
import org.cardanofoundation.rosetta.common.entity.Epoch;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.enumeration.EraType;
import org.cardanofoundation.rosetta.consumer.constant.ConsumerConstant;
import org.cardanofoundation.rosetta.common.ledgersync.Era;
import org.cardanofoundation.rosetta.consumer.repository.EpochRepository;
import org.cardanofoundation.rosetta.consumer.repository.TxRepository;
import org.cardanofoundation.rosetta.consumer.service.EpochService;
import org.cardanofoundation.rosetta.consumer.service.MultiAssetService;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class EpochServiceImpl implements EpochService {

  //private final CachedEpochRepository cachedEpochRepository;

  // Repositories/services for rollback usage
  private final EpochRepository epochRepository;
  private final TxRepository txRepository;
  private final MultiAssetService multiAssetService;

  @Override
  public void handleEpoch(AggregatedBlock aggregatedBlock) {
    final var epoch = epochRepository.findEpochByNo(aggregatedBlock.getEpochNo());
    final var txSize = getTotalTxSize(aggregatedBlock.getTxList());
    final var fee = getTotalTxFee(aggregatedBlock.getTxList());
    final var outSum = getTxOutSum(aggregatedBlock.getTxList());
    final var blockTime = aggregatedBlock.getBlockTime();
    final var eraType = EraType.valueOf(aggregatedBlock.getEra().getValue());

    epoch.ifPresentOrElse(epochEntity -> updateEpoch(fee, outSum, txSize, blockTime, epochEntity)
        , () -> {
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
          epochRepository.save(entityEpoch);
          //cachedEpochRepository.save(entityEpoch);
        });
  }

  @Override
  public synchronized void addFee(AggregatedBlock aggregatedBlock, BigInteger fee) {
    final var epoch = epochRepository.findEpochByNo(aggregatedBlock.getEpochNo());
    if (epoch.isPresent()) {
      Epoch epochEntity = epoch.get();
      epochEntity.setFees(epochEntity.getFees().add(fee));
    }
  }

  @Override
  @Transactional
  public void rollbackEpochStats(List<Block> rollbackBlocks) {
    var epochNoBlocksMap = rollbackBlocks.stream()
        .collect(Collectors.groupingBy(
            org.cardanofoundation.rosetta.common.entity.Block::getEpochNo, Collectors.toSet()));
    var epochBlocksMap = epochRepository.findAllByNoIn(epochNoBlocksMap.keySet())
        .stream()
        .collect(Collectors.toMap(
            Function.identity(), epoch -> epochNoBlocksMap.get(epoch.getNo())));

    List<Tx> allRollbackTxs = new ArrayList<>();
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

      if(epoch.getBlkCount() == 0){
        epochRepository.delete(epoch);
      }else{
        Block minBlock = rollbackBlocks.stream()
            .filter(block -> block.getEpochNo().equals(epoch.getNo()))
            .min(Comparator.comparing(Block::getId)).get();

        epoch.setEndTime(minBlock.getTime());
        epochRepository.save(epoch);
      }



      allRollbackTxs.addAll(rollbackTxs);

      // Instantly delete epoch record from database when there is no blocks of it

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

    //cachedEpochRepository.save(entityEpoch);
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
