package org.cardanofoundation.rosetta.yaciindexer.mapper;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.yaciindexer.BaseMapperSetup;
import org.cardanofoundation.rosetta.yaciindexer.TestDataGenerator;

import static org.assertj.core.api.Assertions.assertThat;

 class CustomTxnMapperTest extends BaseMapperSetup {

  @Autowired
  private CustomTxnMapper mapper;

  @Test
  void toTxnEntityTest() {
    // given
    var from = TestDataGenerator.newTxn();
    // when
    var into = mapper.toTxnEntity(from);
    // then
    assertThat(into.getTxHash()).isEqualTo(from.getTxHash());
    assertThat(into.getBlockHash()).isEqualTo(from.getBlockHash());
    assertThat(into.getBlockNumber()).isEqualTo(from.getBlockNumber());
    assertThat(into.getInputs()).isEqualTo(from.getInputs());
    assertThat(into.getOutputs()).isEqualTo(from.getOutputs());
    assertThat(into.getFee()).isEqualTo(from.getFee());
    assertThat(into.getCollateralInputs()).isEqualTo(from.getCollateralInputs());

    assertThat(into.getSlot()).isNull();
    assertThat(into.getTtl()).isNull();
    assertThat(into.getAuxiliaryDataHash()).isNull();
    assertThat(into.getValidityIntervalStart()).isNull();
    assertThat(into.getScriptDataHash()).isNull();
    assertThat(into.getRequiredSigners()).isNull();
    assertThat(into.getCollateralReturn()).isNull();
    assertThat(into.getCollateralReturnJson()).isNull();
    assertThat(into.getTotalCollateral()).isNull();
    assertThat(into.getReferenceInputs()).isNull();
    assertThat(into.getInvalid()).isNull();
  }

  @Test
  void toTxnWitnessEntityTest() {
    // given
    var from = TestDataGenerator.newTxnWitness();
    // when
    var into = mapper.toTxnWitnessEntity(from);
    // then
    assertThat(into.getTxHash()).isEqualTo(from.getTxHash());
    assertThat(into.getIndex()).isEqualTo(from.getIndex());

    assertThat(into.getPubKey()).isNull();
    assertThat(into.getSignature()).isNull();
    assertThat(into.getPubKeyhash()).isNull();
    assertThat(into.getType()).isNull();
    assertThat(into.getAdditionalData()).isNull();
    assertThat(into.getSlot()).isNull();
  }

  @Test
  void toWithdrawalEntityTest() {
    // given
    var from = TestDataGenerator.newWithdrawal();
    // when
    var into = mapper.toWithdrawalEntity(from);
    // then
    assertThat(into.getAddress()).isEqualTo(from.getAddress());
    assertThat(into.getAmount()).isEqualTo(from.getAmount());
    assertThat(into.getTxHash()).isEqualTo(from.getTxHash());

    assertThat(into.getEpoch()).isNull();
    assertThat(into.getSlot()).isNull();

    assertThat(into.getUpdateDateTime()).isNull();
    assertThat(into.getBlockNumber()).isNull();
    assertThat(into.getBlockTime()).isNull();
  }

}
