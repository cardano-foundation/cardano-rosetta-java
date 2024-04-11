package org.cardanofoundation.rosetta.common.mapper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.BaseMapperTest;
import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.api.block.model.entity.EpochParamEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.ProtocolParamsEntity;

import static org.assertj.core.api.Assertions.assertThat;

class ProtocolParamsToEntityTest extends BaseMapperTest {

  @Autowired
  private ProtocolParamsToEntity my;

  @Test
  void fromEntity_Test_ok() {

    my.modelMapper.validate();
    EpochParamEntity from = newEpochParamEntity();

    ProtocolParams into = my.fromEntity(from.getParams());
    var param = from.getParams();
    assertThat(into.getDecentralisationParam()).isEqualTo(param.getDecentralisationParam());
    assertThat(into.getAdaPerUtxoByte()).isEqualTo(param.getAdaPerUtxoByte());
    assertThat(into.getCommitteeMaxTermLength()).isEqualTo(param.getCommitteeMaxTermLength());
    assertThat(into.getCommitteeMinSize()).isEqualTo(param.getCommitteeMinSize());
    assertThat(into.getDrepDeposit()).isEqualTo(param.getDrepDeposit());
    assertThat(into.getCollateralPercent()).isEqualTo(param.getCollateralPercent());
    assertThat(into.getCostModelsHash()).isEqualTo(param.getCostModelsHash());
    assertThat(into.getExtraEntropy().getTag()).isEqualTo(param.getExtraEntropy());
    assertThat(into.getExpansionRate()).isEqualTo(param.getExpansionRate());
    assertThat(into.getKeyDeposit()).isEqualTo(param.getKeyDeposit());
    assertThat(into.getMaxBlockExMem()).isEqualTo(param.getMaxBlockExMem());
    assertThat(into.getMaxBlockHeaderSize()).isEqualTo(param.getMaxBlockHeaderSize());
    assertThat(into.getMaxBlockBodySize()).isEqualTo(param.getMaxBlockSize());
    assertThat(into.getMaxBlockExSteps()).isEqualTo(param.getMaxBlockExSteps());
    assertThat(into.getMaxBlockExMem()).isEqualTo(param.getMaxBlockExMem());
    assertThat(into.getMaxEpoch()).isEqualTo(param.getMaxEpoch());
    assertThat(into.getMaxTxExMem()).isEqualTo(param.getMaxTxExMem());
    assertThat(into.getMaxTxExSteps()).isEqualTo(param.getMaxTxExSteps());
    assertThat(into.getMaxTxSize()).isEqualTo(param.getMaxTxSize());
    assertThat(into.getMinFeeA()).isEqualTo(param.getMinFeeA());
    assertThat(into.getMinFeeB()).isEqualTo(param.getMinFeeB());
    assertThat(into.getMinPoolCost()).isEqualTo(param.getMinPoolCost());
    assertThat(into.getMinUtxo()).isEqualTo(param.getMinUtxo());
    assertThat(into.getNOpt()).isEqualTo(param.getNOpt());
    assertThat(into.getPoolDeposit()).isEqualTo(param.getPoolDeposit());
    assertThat(into.getPoolPledgeInfluence()).isEqualTo(param.getPoolPledgeInfluence());
    assertThat(into.getPriceMem()).isEqualTo(param.getPriceMem());
    assertThat(into.getPriceStep()).isEqualTo(param.getPriceStep());
    assertThat(into.getProtocolVersion().getMajor()).isEqualTo(param.getProtocolMajorVer());
    assertThat(into.getProtocolVersion().getMinor()).isEqualTo(param.getProtocolMinorVer());
    assertThat(into.getTreasuryGrowthRate()).isEqualTo(param.getTreasuryGrowthRate());
    assertThat(into.getMaxValSize()).isEqualTo(param.getMaxValSize());
    assertThat(into.getMaxCollateralInputs()).isEqualTo(param.getMaxCollateralInputs());
    assertThat(into.getGovActionDeposit()).isEqualTo(param.getGovActionDeposit());
    assertThat(into.getGovActionLifetime()).isEqualTo(param.getGovActionLifetime());
    assertThat(into.getDrepActivity()).isEqualTo(param.getDrepActivity());

    assertThat(into.getCostModels().size()).isEqualTo(1);
    assertThat(into.getCostModels().keySet()).containsAll(param.getCostModels().keySet());
    assertThat(into.getCostModels().values()).containsAll(param.getCostModels().values());
  }

  @Test
  void merge_Test_ok() {
    ProtocolParams from = ProtocolParams.builder().costModels(Map.of("key3", new long[]{4})).build();
    ProtocolParams to = ProtocolParams.builder().costModelsHash("costHash6").build();
    ProtocolParams merged = my.merge(from, to);
    assertThat(merged.getCostModels().size()).isEqualTo(1);
    assertThat(merged.getCostModels().keySet()).containsAll(from.getCostModels().keySet());
    assertThat(merged.getCostModels().values()).containsAll(from.getCostModels().values());

    assertThat(merged.getCostModelsHash()).isEqualTo(to.getCostModelsHash());
  }

  private EpochParamEntity newEpochParamEntity() {
    return EpochParamEntity.builder()
        .params(newEpochParams())
        .epoch(1)
        .slot(2L)
        .blockNumber(3L)
        .blockTime(4L)
        .costModelHash("costModelHash5")
        .updateDateTime(LocalDateTime.MIN)
        .build();
  }

  private ProtocolParamsEntity newEpochParams() {
    return ProtocolParamsEntity
        .builder()
        .decentralisationParam(BigDecimal.valueOf(1))
        .adaPerUtxoByte(BigInteger.valueOf(2))
        .committeeMaxTermLength(3)
        .committeeMinSize(4)
        .drepDeposit(BigInteger.valueOf(5))
        .collateralPercent(6)
        .costModelsHash("costModelsHash7")
        .costModels(Map.of("key1", new long[]{0}))
        .extraEntropy("extraEntropy8")
        .expansionRate(BigDecimal.valueOf(9))
        .keyDeposit(BigInteger.valueOf(10))
        .maxBlockExMem(BigInteger.valueOf(11))
        .maxBlockHeaderSize(12)
        .maxBlockExSteps(BigInteger.valueOf(13))
        .maxBlockExMem(BigInteger.valueOf(14))
        .maxBlockSize(15)
        .maxEpoch(16)
        .maxTxExMem(BigInteger.valueOf(17))
        .maxTxExSteps(BigInteger.valueOf(18))
        .maxTxSize(19)
        .minFeeA(20)
        .minFeeB(21)
        .minPoolCost(BigInteger.valueOf(22))
        .minUtxo(BigInteger.valueOf(23))
        .nOpt(24)
        .poolDeposit(BigInteger.valueOf(25))
        .poolPledgeInfluence(BigDecimal.valueOf(26))
        .priceMem(BigDecimal.valueOf(27))
        .priceStep(BigDecimal.valueOf(28))
        .protocolMajorVer(29)
        .protocolMinorVer(30)
        .treasuryGrowthRate(BigDecimal.valueOf(31))
        .maxValSize(32L)
        .maxCollateralInputs(33)
        .govActionDeposit(BigInteger.valueOf(34))
        .govActionLifetime(35)
        .drepActivity(36)
        .build();
  }
}