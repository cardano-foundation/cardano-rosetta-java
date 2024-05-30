package org.cardanofoundation.rosetta.yaciindexer.mapper;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.yaciindexer.BaseMapperSetup;
import org.cardanofoundation.rosetta.yaciindexer.TestDataGenerator;

import static org.assertj.core.api.Assertions.assertThat;

 class CustomPoolMapperTest extends BaseMapperSetup {

  @Autowired
  private CustomPoolMapper mapper;

  @Test
  void toPoolRegistrationEntityTest() {
    // given
    var from = TestDataGenerator.newPoolRegistration();
    // when
    var into = mapper.toPoolRegistrationEntity(from);

    assertThat(into.getTxHash()).isEqualTo(from.getTxHash());
    assertThat(into.getCertIndex()).isEqualTo(from.getCertIndex());
    assertThat(into.getPoolId()).isEqualTo(from.getPoolId());
    assertThat(into.getVrfKeyHash()).isEqualTo(from.getVrfKeyHash());
    assertThat(into.getPledge()).isEqualTo(from.getPledge());
    assertThat(into.getCost()).isEqualTo(from.getCost());
    assertThat(into.getMargin()).isEqualTo(from.getMargin());
    assertThat(into.getRewardAccount()).isEqualTo(from.getRewardAccount());
    assertThat(into.getPoolOwners()).isEqualTo(from.getPoolOwners());
    assertThat(into.getRelays()).isEqualTo(from.getRelays());

    assertThat(into.getMetadataUrl()).isNull();
    assertThat(into.getMetadataHash()).isNull();
    assertThat(into.getEpoch()).isNull();
    assertThat(into.getSlot()).isNull();
    assertThat(into.getBlockHash()).isNull();

    assertThat(into.getUpdateDateTime()).isNull();
    assertThat(into.getBlockNumber()).isNull();
    assertThat(into.getBlockTime()).isNull();
  }

  @Test
  void toPoolRetirementEntityTest() {
    // given
    var from = TestDataGenerator.newPoolRetirement();
    // when
    var into = mapper.toPoolRetirementEntity(from);

    assertThat(into.getTxHash()).isEqualTo(from.getTxHash());
    assertThat(into.getCertIndex()).isEqualTo(from.getCertIndex());
    assertThat(into.getPoolId()).isEqualTo(from.getPoolId());
    assertThat(into.getEpoch()).isEqualTo(from.getEpoch());

    assertThat(into.getRetirementEpoch()).isZero();
    assertThat(into.getSlot()).isNull();
    assertThat(into.getBlockHash()).isNull();

    assertThat(into.getUpdateDateTime()).isNull();
    assertThat(into.getBlockNumber()).isNull();
    assertThat(into.getBlockTime()).isNull();
  }

}
