package org.cardanofoundation.rosetta.common.mapper;

import java.math.BigInteger;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.BaseMapperSetup;
import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.api.block.model.entity.EpochParamEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.ProtocolParamsEntity;

import static org.assertj.core.api.Assertions.assertThat;

class ProtocolParamsToEntityTest extends BaseMapperSetup {

  @Autowired
  private ProtocolParamsToEntity my;

  @Test
  void fromEntity_Test_ok() {
    EpochParamEntity from = newEpochParamEntity();

    ProtocolParams into = my.fromEntity(from.getParams());
    var param = from.getParams();
    assertProtocolParameters(into, param);

    assertThat(into.getCostModels()).hasSize(1);
    assertThat(into.getCostModels().keySet()).containsAll(param.getCostModels().keySet());
    assertThat(into.getCostModels().values()).containsAll(param.getCostModels().values());
  }

//  @Test
//  void merge_Test_ok() {
//    ProtocolParams from = ProtocolParams.builder()
//        .costModels(Map.of("key3", new long[]{4}))
//        .minPoolCost(BigInteger.valueOf(5))
//        .build();
//    ProtocolParams to = ProtocolParams.builder()
//        .minPoolCost(BigInteger.valueOf(7))
//        .build();
//    ProtocolParams merged = my.merge(from, to);
//
//    assertThat(merged.getCostModels()).hasSize(1);
//    assertThat(merged.getCostModels().keySet()).containsAll(from.getCostModels().keySet());
//    assertThat(merged.getCostModels().values()).containsAll(from.getCostModels().values());
//    assertThat(merged.getMinPoolCost()).isEqualTo(from.getMinPoolCost());
//  }

  private EpochParamEntity newEpochParamEntity() {
    return new EpochParamEntity(1, newEpochParams());
  }

  private ProtocolParamsEntity newEpochParams() {
    return ProtocolParamsEntity
        .builder()
        .adaPerUtxoByte(BigInteger.valueOf(2))
        .costModels(Map.of("key1", new long[]{0}))
        .keyDeposit(BigInteger.valueOf(10))
        .maxTxSize(19)
        .minFeeA(20)
        .minFeeB(21)
        .minPoolCost(BigInteger.valueOf(22))
        .poolDeposit(BigInteger.valueOf(25))
        .protocolMajorVer(29)
        .protocolMinorVer(30)
        .maxValSize(32L)
        .maxCollateralInputs(33)
        .build();
  }

  private void assertProtocolParameters(ProtocolParams into, ProtocolParamsEntity param) {
    assertThat(into.getAdaPerUtxoByte()).isEqualTo(param.getAdaPerUtxoByte());
    assertThat(into.getKeyDeposit()).isEqualTo(param.getKeyDeposit());
    assertThat(into.getMaxTxSize()).isEqualTo(param.getMaxTxSize());
    assertThat(into.getMinFeeA()).isEqualTo(param.getMinFeeA());
    assertThat(into.getMinFeeB()).isEqualTo(param.getMinFeeB());
    assertThat(into.getMinPoolCost()).isEqualTo(param.getMinPoolCost());
    assertThat(into.getPoolDeposit()).isEqualTo(param.getPoolDeposit());
    assertThat(into.getProtocolVersion().getMajor()).isEqualTo(param.getProtocolMajorVer());
    assertThat(into.getProtocolVersion().getMinor()).isEqualTo(param.getProtocolMinorVer());
    assertThat(into.getMaxValSize()).isEqualTo(param.getMaxValSize());
    assertThat(into.getMaxCollateralInputs()).isEqualTo(param.getMaxCollateralInputs());
  }
}
