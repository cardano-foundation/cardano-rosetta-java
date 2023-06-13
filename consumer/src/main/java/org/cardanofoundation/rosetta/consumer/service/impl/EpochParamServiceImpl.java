package org.cardanofoundation.rosetta.consumer.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.common.entity.Block;
import org.cardanofoundation.rosetta.common.entity.Epoch;
import org.cardanofoundation.rosetta.common.entity.EpochParam;
import org.cardanofoundation.rosetta.common.entity.ParamProposal;
import org.cardanofoundation.rosetta.common.enumeration.EraType;
import org.cardanofoundation.rosetta.consumer.constant.ConsumerConstant;
import org.cardanofoundation.rosetta.consumer.mapper.EpochParamMapper;
import org.cardanofoundation.rosetta.consumer.repository.BlockRepository;
import org.cardanofoundation.rosetta.consumer.repository.EpochParamRepository;
import org.cardanofoundation.rosetta.consumer.repository.EpochRepository;
import org.cardanofoundation.rosetta.consumer.repository.ParamProposalRepository;
import org.cardanofoundation.rosetta.consumer.service.CostModelService;
import org.cardanofoundation.rosetta.consumer.service.EpochParamService;
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

  final BlockRepository blockRepository;
  final ParamProposalRepository paramProposalRepository;
  final EpochParamRepository epochParamRepository;
  final EpochRepository epochRepository;
  final CostModelService costModelService;
  final EpochParamMapper epochParamMapper;

  EpochParam defShelleyEpochParam;

  @Override
  public void setDefShelleyEpochParam(
      EpochParam defShelleyEpochParam) {
    this.defShelleyEpochParam = defShelleyEpochParam;
  }

  @Override
  public void setDefAlonzoEpochParam(
      EpochParam defAlonzoEpochParam) {
    this.defAlonzoEpochParam = defAlonzoEpochParam;
  }

  EpochParam defAlonzoEpochParam;

  @Override
  public void handleEpochParams() {
    Integer lastEpochParam = epochParamRepository.findLastEpochParam()
        .map(EpochParam::getEpochNo)
        .orElse(0);

    epochRepository.findAll()
        .stream()
        .filter(epoch -> epoch.getMaxSlot() == ConsumerConstant.SHELLEY_SLOT
            && epoch.getNo() > lastEpochParam)
        .sorted(Comparator.comparingInt(Epoch::getNo))
        .forEach(epoch -> handleEpochParam(epoch.getNo()));
  }

  /**
   * Handle epoch param for epochNo.
   *
   * @param epochNo
   */
  void handleEpochParam(int epochNo) {
    EraType curEra = getEra(epochNo);
    EraType prevEra = getEra(epochNo - BigInteger.ONE.intValue());

    if (curEra == EraType.BYRON || prevEra == null) {
      return;
    }
    log.info("Handling epoch param for epoch: {}", epochNo);

    Optional<EpochParam> prevEpochParam = epochParamRepository.findEpochParamByEpochNo(epochNo - 1);

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

    List<ParamProposal> prevParamProposals = paramProposalRepository
        .findParamProposalsByEpochNo(epochNo - 1);
    prevParamProposals.forEach(
        paramProposal -> epochParamMapper.updateByParamProposal(curEpochParam, paramProposal));

    Block block = blockRepository.findFirstByEpochNo(epochNo)
        .orElseThrow(
            () -> new RuntimeException("Block not found for epoch: " + epochNo));
    curEpochParam.setEpochNo(epochNo);
    curEpochParam.setBlock(block);
    epochParamRepository.save(curEpochParam);
  }

  /**
   * Get era by epoch
   *
   * @param epochNo epoch
   * @return eraType
   */
  private EraType getEra(int epochNo) {
    return epochRepository.findEpochByNo(epochNo)
        .map(Epoch::getEra)
        .orElse(null);
  }

}
