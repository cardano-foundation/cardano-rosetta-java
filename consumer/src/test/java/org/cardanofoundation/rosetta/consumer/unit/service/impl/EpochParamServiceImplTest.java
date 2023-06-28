package org.cardanofoundation.rosetta.consumer.unit.service.impl;

import java.util.List;
import java.util.Optional;
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
import org.cardanofoundation.rosetta.consumer.service.impl.EpochParamServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class EpochParamServiceImplTest {

  @BeforeEach
  void setUp() {
  }


  @Test
  void setHandleEpochParamsLastGreaterThanMaxSlot() {
    BlockRepository blockRepository = Mockito.mock(BlockRepository.class);
    ParamProposalRepository paramProposalRepository = Mockito.mock(ParamProposalRepository.class);
    EpochParamRepository epochParamRepository = Mockito.mock(EpochParamRepository.class);
    EpochRepository epochRepository = Mockito.mock(EpochRepository.class);
    CostModelService costModelService = Mockito.mock(CostModelService.class);
    EpochParamMapper epochParamMapper = Mockito.mock(EpochParamMapper.class);
    EpochParam defShelleyEpochParam = Mockito.mock(EpochParam.class);
    EpochParam defAlonzoEpochParam = Mockito.mock(EpochParam.class);
    Epoch epoch = Mockito.mock(Epoch.class);
    EpochParam mockEpochParam = Mockito.mock(EpochParam.class);
    Mockito.when(mockEpochParam.getEpochNo()).thenReturn(112);
    Mockito.when(epochParamRepository.findLastEpochParam()).thenReturn(Optional.of(mockEpochParam));
    List<Epoch> epoches = List.of(epoch);
    Mockito.when(epoch.getNo()).thenReturn(111);
    Mockito.when(epoch.getMaxSlot()).thenReturn(432000);
    Mockito.when(epochRepository.findAll()).thenReturn(epoches);

    EpochParamServiceImpl epochParamServiceImpl = new EpochParamServiceImpl(blockRepository,
        paramProposalRepository, epochParamRepository, epochRepository, costModelService,
        epochParamMapper);
    epochParamServiceImpl.setDefShelleyEpochParam(defShelleyEpochParam);
    epochParamServiceImpl.setDefAlonzoEpochParam(defAlonzoEpochParam);

    epochParamServiceImpl.handleEpochParams();
  }

  @Test
  void setDefShelleyEpochParamException() {
    BlockRepository blockRepository = Mockito.mock(BlockRepository.class);
    ParamProposalRepository paramProposalRepository = Mockito.mock(ParamProposalRepository.class);
    EpochParamRepository epochParamRepository = Mockito.mock(EpochParamRepository.class);
    EpochRepository epochRepository = Mockito.mock(EpochRepository.class);
    CostModelService costModelService = Mockito.mock(CostModelService.class);
    EpochParamMapper epochParamMapper = Mockito.mock(EpochParamMapper.class);
    EpochParam defShelleyEpochParam = Mockito.mock(EpochParam.class);
    EpochParam defAlonzoEpochParam = Mockito.mock(EpochParam.class);
    Epoch epoch = Mockito.mock(Epoch.class);
    EpochParam mockEpochParam = Mockito.mock(EpochParam.class);
    Mockito.when(mockEpochParam.getEpochNo()).thenReturn(12);
    Mockito.when(epochParamRepository.findLastEpochParam()).thenReturn(Optional.of(mockEpochParam));
    List<Epoch> epoches = List.of(epoch);
    Mockito.when(epoch.getNo()).thenReturn(34);
    Mockito.when(epoch.getMaxSlot()).thenReturn(ConsumerConstant.SHELLEY_SLOT);
    Mockito.when(epoch.getEra()).thenReturn(EraType.valueOf(EraType.SHELLEY.getValue()));
    Mockito.when(epochRepository.findAll()).thenReturn(epoches);

    Optional<Epoch> optionalEpoch = Optional.of(epoch);
    Mockito.when(epochRepository.findEpochByNo(34)).thenReturn(optionalEpoch);
    Mockito.when(epochRepository.findEpochByNo(33)).thenReturn(optionalEpoch);

    EpochParamServiceImpl epochParamServiceImpl = new EpochParamServiceImpl(blockRepository,
        paramProposalRepository, epochParamRepository, epochRepository,
        costModelService, epochParamMapper);
    epochParamServiceImpl.setDefShelleyEpochParam(defShelleyEpochParam);
    epochParamServiceImpl.setDefAlonzoEpochParam(defAlonzoEpochParam);
    Assertions.assertThrows(RuntimeException.class, epochParamServiceImpl::handleEpochParams);
    Mockito.verify(epochParamRepository, Mockito.times(0)).save(Mockito.any());
  }

  @Test
  void setDefShelleyEpochParamEraShelley() {
    BlockRepository blockRepository = Mockito.mock(BlockRepository.class);
    ParamProposalRepository paramProposalRepository = Mockito.mock(ParamProposalRepository.class);
    EpochParamRepository epochParamRepository = Mockito.mock(EpochParamRepository.class);
    EpochRepository epochRepository = Mockito.mock(EpochRepository.class);
    CostModelService costModelService = Mockito.mock(CostModelService.class);
    EpochParamMapper epochParamMapper = Mockito.mock(EpochParamMapper.class);
    EpochParam defShelleyEpochParam = Mockito.mock(EpochParam.class);
    EpochParam defAlonzoEpochParam = Mockito.mock(EpochParam.class);
    Epoch epoch = Mockito.mock(Epoch.class);
    Epoch epoch2 = Mockito.mock(Epoch.class);
    EpochParam mockEpochParam = Mockito.mock(EpochParam.class);
    Mockito.when(mockEpochParam.getEpochNo()).thenReturn(2);
    Mockito.when(epochParamRepository.findLastEpochParam()).thenReturn(Optional.of(mockEpochParam));
    Optional<EpochParam> prevEpochParam = Optional.of(defAlonzoEpochParam);
    Mockito.when(epochParamRepository.findEpochParamByEpochNo(2))
        .thenReturn(prevEpochParam);
    List<Epoch> epoches = List.of(epoch);
    Mockito.when(epoch.getNo()).thenReturn(3);
    Mockito.when(epoch.getMaxSlot()).thenReturn(ConsumerConstant.SHELLEY_SLOT);
    Mockito.when(epoch.getEra()).thenReturn(EraType.valueOf(EraType.SHELLEY.getValue()));
    Mockito.when(epoch2.getNo()).thenReturn(2);
    Mockito.when(epoch2.getMaxSlot()).thenReturn(ConsumerConstant.BYRON_SLOT);
    Mockito.when(epoch2.getEra()).thenReturn(EraType.valueOf(EraType.BYRON.getValue()));
    Mockito.when(epochRepository.findAll()).thenReturn(epoches);
    Optional<Epoch> optionalEpoch = Optional.of(epoch);
    Optional<Epoch> optionalEpoch2 = Optional.of(epoch2);

    Block cachedBlock = Mockito.mock(Block.class);
    Optional<Block> optionalBlock = Optional.of(cachedBlock);
    Mockito.when(epochRepository.findEpochByNo(3)).thenReturn(optionalEpoch);
    Mockito.when(epochRepository.findEpochByNo(2)).thenReturn(optionalEpoch2);
    Mockito.when(blockRepository.findFirstByEpochNo(3)).thenReturn(optionalBlock);

    EpochParamServiceImpl epochParamServiceImpl = new EpochParamServiceImpl(blockRepository,
        paramProposalRepository, epochParamRepository, epochRepository,
        costModelService, epochParamMapper);
    epochParamServiceImpl.setDefShelleyEpochParam(defShelleyEpochParam);
    epochParamServiceImpl.setDefAlonzoEpochParam(defAlonzoEpochParam);

    epochParamServiceImpl.handleEpochParams();
    Mockito.verify(epochParamRepository, Mockito.times(1)).save(Mockito.any());
    Mockito.verify(epochParamMapper, Mockito.times(2))
        .updateByEpochParam(Mockito.any(), Mockito.any());
  }

  @Test
  void setDefShelleyEpochParamEraAlonzo() {
    BlockRepository blockRepository = Mockito.mock(BlockRepository.class);
    ParamProposalRepository paramProposalRepository = Mockito.mock(ParamProposalRepository.class);
    EpochParamRepository epochParamRepository = Mockito.mock(EpochParamRepository.class);
    EpochRepository epochRepository = Mockito.mock(EpochRepository.class);
    CostModelService costModelService = Mockito.mock(CostModelService.class);
    EpochParamMapper epochParamMapper = Mockito.mock(EpochParamMapper.class);
    EpochParam defShelleyEpochParam = Mockito.mock(EpochParam.class);
    EpochParam defAlonzoEpochParam = Mockito.mock(EpochParam.class);
    Epoch epoch = Mockito.mock(Epoch.class);
    Epoch epoch2 = Mockito.mock(Epoch.class);
    EpochParam mockEpochParam = Mockito.mock(EpochParam.class);
    Mockito.when(mockEpochParam.getEpochNo()).thenReturn(2);
    Mockito.when(epochParamRepository.findLastEpochParam()).thenReturn(Optional.of(mockEpochParam));
    Optional<EpochParam> prevEpochParam = Optional.of(defAlonzoEpochParam);
    Mockito.when(epochParamRepository.findEpochParamByEpochNo(4))
        .thenReturn(prevEpochParam);
    List<Epoch> epoches = List.of(epoch);
    Mockito.when(epoch.getNo()).thenReturn(5);
    Mockito.when(epoch.getMaxSlot()).thenReturn(ConsumerConstant.SHELLEY_SLOT);
    Mockito.when(epoch.getEra()).thenReturn(EraType.valueOf(EraType.ALONZO.getValue()));
    Mockito.when(epoch2.getNo()).thenReturn(4);
    Mockito.when(epoch2.getMaxSlot()).thenReturn(ConsumerConstant.SHELLEY_SLOT);
    Mockito.when(epoch2.getEra()).thenReturn(EraType.valueOf(EraType.MARY.getValue()));
    Mockito.when(epochRepository.findAll()).thenReturn(epoches);
    Optional<Epoch> optionalEpoch = Optional.of(epoch);
    Optional<Epoch> optionalEpoch2 = Optional.of(epoch2);

    ParamProposal paramProposal = Mockito.mock(ParamProposal.class);
    List<ParamProposal> prevParamProposals = List.of(paramProposal);
    Mockito.when(paramProposalRepository.findParamProposalsByEpochNo(4))
        .thenReturn(prevParamProposals);
    Block cachedBlock = Mockito.mock(Block.class);
    Optional<Block> optionalBlock = Optional.of(cachedBlock);
    Mockito.when(epochRepository.findEpochByNo(5)).thenReturn(optionalEpoch);
    Mockito.when(epochRepository.findEpochByNo(4)).thenReturn(optionalEpoch2);
    Mockito.when(blockRepository.findFirstByEpochNo(5)).thenReturn(optionalBlock);

    EpochParamServiceImpl epochParamServiceImpl = new EpochParamServiceImpl(blockRepository,
        paramProposalRepository, epochParamRepository, epochRepository,
        costModelService, epochParamMapper);
    epochParamServiceImpl.setDefShelleyEpochParam(defShelleyEpochParam);
    epochParamServiceImpl.setDefAlonzoEpochParam(defAlonzoEpochParam);

    epochParamServiceImpl.handleEpochParams();
    Mockito.verify(epochParamRepository, Mockito.times(1)).save(Mockito.any());
    Mockito.verify(epochParamMapper, Mockito.times(2))
        .updateByEpochParam(Mockito.any(), Mockito.any());
    Mockito.verify(epochParamMapper, Mockito.times(1))
        .updateByParamProposal(Mockito.any(), Mockito.any());
    Mockito.verify(costModelService, Mockito.times(1)).getGenesisCostModel();
  }
}