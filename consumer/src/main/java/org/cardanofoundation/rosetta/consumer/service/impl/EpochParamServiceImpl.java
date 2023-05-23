package org.cardanofoundation.rosetta.consumer.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.common.entity.Block;
import org.cardanofoundation.rosetta.common.entity.Epoch;
import org.cardanofoundation.rosetta.common.entity.EpochParam;
import org.cardanofoundation.rosetta.common.entity.ParamProposal;
import org.cardanofoundation.rosetta.common.enumeration.EraType;
import org.cardanofoundation.rosetta.common.ledgersync.constant.Constant;
import org.cardanofoundation.rosetta.consumer.constant.ConsumerConstant;
import org.cardanofoundation.rosetta.consumer.mapper.EpochParamMapper;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedBlockRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedEpochParamRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedEpochRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedParamProposalRepository;
import org.cardanofoundation.rosetta.consumer.service.CostModelService;
import org.cardanofoundation.rosetta.consumer.service.EpochParamService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Service
public class EpochParamServiceImpl implements EpochParamService {

  final CachedBlockRepository cachedBlockRepository;
  final CachedParamProposalRepository cachedParamProposalRepository;
  final CachedEpochParamRepository cachedEpochParamRepository;
  final CachedEpochRepository cachedEpochRepository;
  final CostModelService costModelService;
  final EpochParamMapper epochParamMapper;
  final ObjectMapper mapper;

  @Value("${genesis.network}")
  Integer network;

  EpochParam defShelleyEpochParam;
  EpochParam defAlonzoEpochParam;

  @Override
  public void setDefShelleyEpochParam(EpochParam defShelleyEpochParam) {
    this.defShelleyEpochParam = defShelleyEpochParam;
  }

  @Override
  public void setDefAlonzoEpochParam(EpochParam defAlonzoEpochParam) {
    this.defAlonzoEpochParam = defAlonzoEpochParam;
  }

  @Override
  public void handleEpochParams() {
    Integer lastEpochParam = cachedEpochParamRepository.findLastEpochParam();

    cachedEpochRepository.findAll()
        .stream()
        .filter(epoch -> epoch.getMaxSlot() == ConsumerConstant.SHELLEY_SLOT
            && epoch.getNo() > lastEpochParam)
        .sorted(Comparator.comparingInt(Epoch::getNo))
        .forEach(epoch -> handleEpochParam(epoch.getNo()));
  }

  /**
   * Handle epoch param for epochNo. Handle era here
   *
   * @param epochNo
   */

  void handleEpochParam(int epochNo) {
    EraType curEra = getEra(epochNo);
    EraType prevEra = getEra(epochNo - 1);

    if (curEra == EraType.BYRON || prevEra == null) {
      return;
    }
    log.info("Handling epoch param for epoch: {}", epochNo);

    Optional<EpochParam> prevEpochParam = cachedEpochParamRepository.findPrevEpochParamByEpochNo(
        epochNo);

    EpochParam curEpochParam = new EpochParam();

    prevEpochParam.ifPresent(
        epochParam -> epochParamMapper.updateByEpochParam(curEpochParam, epochParam));

    if (curEra == EraType.SHELLEY && prevEra == EraType.BYRON) {
      epochParamMapper.updateByEpochParam(curEpochParam, defShelleyEpochParam);
    }
    if (curEra == EraType.ALONZO && prevEra == EraType.MARY) {
      epochParamMapper.updateByEpochParam(curEpochParam, defAlonzoEpochParam);
      curEpochParam.setCostModel(costModelService.getGenesisCostModel());
    }

    List<ParamProposal> prevParamProposals = cachedParamProposalRepository.findParamProposalEpochNo(
        epochNo - 1);
    prevParamProposals.forEach(
        paramProposal -> epochParamMapper.updateByParamProposal(curEpochParam, paramProposal));

    Block block = cachedBlockRepository.findFirstByEpochNo(epochNo)
        .orElseThrow(
            () -> new RuntimeException("Block not found for epoch: " + epochNo));
    curEpochParam.setEpochNo(epochNo);
    curEpochParam.setBlock(block);
    cachedEpochParamRepository.save(curEpochParam);
  }

  /**
   * Get era by epoch
   *
   * @param epochNo epoch
   * @return eraType
   */
  private EraType getEra(int epochNo) {
    return cachedEpochRepository.findEpochByNo(epochNo)
        .map(Epoch::getEra)
        .orElse(null);
  }

  @PostConstruct
  void setup() {
    switch (network) {
      case Constant.MAINNET:
        defShelleyEpochParam = EpochParam.builder()
            .influence(0.3)
            .decentralisation(1.0)
            .maxEpoch(18)
            .keyDeposit(BigInteger.valueOf(2000000))
            .maxBlockSize(65536)
            .maxBhSize(1100)
            .maxTxSize(16384)
            .minFeeA(44)
            .minFeeB(155381)
            .minPoolCost(BigInteger.valueOf(340000000))
            .minUtxoValue(BigInteger.valueOf(1000000))
            .optimalPoolCount(150)
            .poolDeposit(BigInteger.valueOf(500000000))
            .protocolMajor(2)
            .protocolMinor(0)
            .monetaryExpandRate(0.003)
            .treasuryGrowthRate(0.2)
            .build();
        defAlonzoEpochParam = EpochParam.builder()
            .priceMem(0.0577)
            .priceStep(0.0000721)
            .maxTxExMem(BigInteger.valueOf(10000000L))
            .maxTxExSteps(BigInteger.valueOf(10000000000L))
            .maxBlockExMem(BigInteger.valueOf(50000000))
            .maxBlockExSteps(BigInteger.valueOf(40000000000L))
            .maxValSize(BigInteger.valueOf(5000))
            .collateralPercent(150)
            .maxCollateralInputs(3)
            .build();
        break;
      case Constant.PREPROD_TESTNET:
        defShelleyEpochParam = EpochParam.builder()
            .influence(0.1)
            .decentralisation(1.0)
            .maxEpoch(18)
            .keyDeposit(BigInteger.valueOf(400000))
            .maxBlockSize(65536)
            .maxBhSize(1100)
            .maxTxSize(16384)
            .minFeeA(44)
            .minFeeB(155381)
            .minPoolCost(BigInteger.valueOf(0))
            .minUtxoValue(BigInteger.valueOf(0))
            .optimalPoolCount(50)
            .poolDeposit(BigInteger.valueOf(500000000))
            .protocolMajor(2)
            .protocolMinor(0)
            .monetaryExpandRate(0.00178650067)
            .treasuryGrowthRate(0.1)
            .build();
        defAlonzoEpochParam = EpochParam.builder()
            .priceMem(0.0577)
            .priceStep(0.0000721)
            .maxTxExMem(BigInteger.valueOf(10000000))
            .maxTxExSteps(BigInteger.valueOf(10000000000L))
            .maxBlockExMem(BigInteger.valueOf(50000000))
            .maxBlockExSteps(BigInteger.valueOf(40000000000L))
            .maxValSize(BigInteger.valueOf(5000))
            .collateralPercent(150)
            .maxCollateralInputs(3)
            .build();
        break;
      default:
        throw new RuntimeException("not support yet");
    }
  }

}
