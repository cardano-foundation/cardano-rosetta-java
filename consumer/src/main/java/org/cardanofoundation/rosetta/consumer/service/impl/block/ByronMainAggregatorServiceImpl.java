package org.cardanofoundation.rosetta.consumer.service.impl.block;

import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx.AggregatedTxBuilder;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTxIn;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTxOut;
import org.cardanofoundation.rosetta.common.ledgersync.byron.ByronMainBlock;
import org.cardanofoundation.rosetta.common.ledgersync.byron.ByronTx;
import org.cardanofoundation.rosetta.common.ledgersync.byron.ByronTxIn;
import org.cardanofoundation.rosetta.common.ledgersync.byron.ByronTxOut;
import org.cardanofoundation.rosetta.common.ledgersync.byron.payload.ByronTxPayload;
import org.cardanofoundation.rosetta.consumer.service.BlockAggregatorService;
import org.cardanofoundation.rosetta.consumer.service.BlockDataService;
import org.cardanofoundation.rosetta.consumer.service.SlotLeaderService;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ByronMainAggregatorServiceImpl extends BlockAggregatorService<ByronMainBlock> {

  public ByronMainAggregatorServiceImpl(
      SlotLeaderService slotLeaderService,
      BlockDataService blockDataService) {
    super(slotLeaderService, blockDataService);
  }

  @Override
  public AggregatedBlock aggregateBlock(ByronMainBlock block) {
    return mapBlockCddlToAggregatedBlock(block);
  }

  private AggregatedBlock mapBlockCddlToAggregatedBlock(ByronMainBlock blockCddl) {
    var blockHash = blockCddl.getBlockHash();
    var slotId = blockCddl.getHeader().getConsensusData().getSlotId();
    var epochNo = (int) slotId.getValue();
    var slotNo = slotId.getSlotId();
    var epochSlotNo = (int) slotId.getSlotOfEpoch();
    var blockNo = blockCddl.getBlockNumber();
    var prevHash = blockCddl.getHeader().getPrevBlock();
    var slotLeader = slotLeaderService.getSlotLeaderHashAndPrefix(blockCddl);
    var blockSize = blockCddl.getCborSize();
    var blockTime = Timestamp.valueOf(LocalDateTime.ofEpochSecond(
        blockCddl.getBlockTime(), 0, ZoneOffset.ofHours(0)));
    var txCount = (long) blockCddl.getBody().getTxPayload().size();

    var blockVersion = blockCddl.getHeader().getExtraData().getBlockVersion();
    var protoMajor = (int) blockVersion.getMajor();
    var protoMinor = (int) blockVersion.getMinor();

    List<AggregatedTx> txList = mapCddlTxToAggregatedTx(blockCddl);
    return AggregatedBlock.builder()
        .era(blockCddl.getEraType())
        .network(blockCddl.getNetwork())
        .hash(blockHash)
        .epochNo(epochNo)
        .epochSlotNo(epochSlotNo)
        .slotNo(slotNo)
        .blockNo(blockNo)
        .prevBlockHash(prevHash)
        .slotLeader(slotLeader)
        .blockSize(blockSize)
        .blockTime(blockTime)
        .txCount(txCount)
        .protoMajor(protoMajor)
        .protoMinor(protoMinor)
        .txList(txList)
        .auxiliaryDataMap(Collections.emptyMap())
        .build();
  }

  /**
   * This method transforms CDDL tx data to aggregated tx objects, which
   * will be used later by block processing and transactions handling
   * services
   *
   * @param blockCddl   transformed block data from CDDL, containing tx data
   * @return            list of aggregated tx objects
   */
  private List<AggregatedTx> mapCddlTxToAggregatedTx(ByronMainBlock blockCddl) {
    List<ByronTxPayload> txPayloads = blockCddl.getBody().getTxPayload();
    return IntStream.range(0, txPayloads.size()).mapToObj(txIdx -> {
      ByronTxPayload byronTxPayload = txPayloads.get(txIdx);
      return txToAggregatedTx(blockCddl.getBlockHash(), txIdx, byronTxPayload);
    }).collect(Collectors.toList());
  }

  /**
   * This method transforms a single CDDL tx data to aggregated tx object
   *
   * @param blockHash         block hash where the currently processing tx lies in
   * @param idx               currently processing tx's index within a block
   * @param txPayload         transformed CDDL tx data
   * @return                  aggregated tx object
   */
  private AggregatedTx txToAggregatedTx(byte[] blockHash, int idx, ByronTxPayload txPayload) {
    ByronTx byronTx = txPayload.getTransaction();
    AggregatedTxBuilder aggregatedTxBuilder = AggregatedTx.builder();

    // Handle basic tx data
    var txHash = byronTx.getTxHash();
    aggregatedTxBuilder.hash(txHash);
    aggregatedTxBuilder.blockHash(blockHash);
    aggregatedTxBuilder.blockIndex(idx);
    aggregatedTxBuilder.validContract(true);
    aggregatedTxBuilder.deposit(0);

    // Converts CDDL tx ins data to aggregated tx ins
    var inputs = byronTx.getInputs();
    aggregatedTxBuilder.txInputs(txInsToAggregatedTxIns(inputs));

    // Converts CDDL tx outs/collateral return data to aggregated tx outs
    var outputs = byronTx.getOutputs();
    var aggregatedTxOuts = txOutsToAggregatedTxOuts(outputs);
    aggregatedTxBuilder.txOutputs(aggregatedTxOuts);

    /*
     * Handle address balance from tx outputs or collateral return
     * This is initial step of calculating balance. The same process will be
     * done when tx ins are taken into account
     */
    mapAggregatedTxOutsToAddressBalanceMap(aggregatedTxOuts, txHash);

    var outSum = calculateByronOutSum(outputs);
    aggregatedTxBuilder.outSum(outSum);
    aggregatedTxBuilder.fee(BigInteger.ZERO);

    return aggregatedTxBuilder.build();
  }

  private Set<AggregatedTxIn> txInsToAggregatedTxIns(List<ByronTxIn> txInputs) {
    if (CollectionUtils.isEmpty(txInputs)) {
      return Collections.emptySet();
    }

    return txInputs.stream().map(AggregatedTxIn::of).collect(Collectors.toSet());
  }

  private List<AggregatedTxOut> txOutsToAggregatedTxOuts(List<ByronTxOut> txOutputs) {
    if (CollectionUtils.isEmpty(txOutputs)) {
      return Collections.emptyList();
    }

    return IntStream.range(0, txOutputs.size())
        .mapToObj(txOutIdx -> AggregatedTxOut.from(txOutputs.get(txOutIdx), txOutIdx))
        .collect(Collectors.toList());
  }

  private static BigInteger calculateByronOutSum(List<ByronTxOut> txOuts) {
    return txOuts.stream()
        .map(ByronTxOut::getAmount)
        .reduce(BigInteger.ZERO, BigInteger::add);
  }
}
