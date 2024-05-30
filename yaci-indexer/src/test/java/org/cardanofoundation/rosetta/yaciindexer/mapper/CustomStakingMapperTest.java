package org.cardanofoundation.rosetta.yaciindexer.mapper;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.yaciindexer.BaseMapperSetup;
import org.cardanofoundation.rosetta.yaciindexer.TestDataGenerator;

import static org.assertj.core.api.Assertions.assertThat;

 class CustomStakingMapperTest extends BaseMapperSetup {

  @Autowired
  private CustomStakingMapper mapper;

  @Test
  void toStakeRegistrationEntityTest() {
    // given
    var from = TestDataGenerator.newStakeRegistrationDetail();
    // when
    var into = mapper.toStakeResistrationEntity(from);
    // then
    assertThat(into.getTxHash()).isEqualTo(from.getTxHash());
    assertThat(into.getCertIndex()).isEqualTo(from.getCertIndex());
    assertThat(into.getType()).isEqualTo(from.getType());
    assertThat(into.getAddress()).isEqualTo(from.getAddress());
    assertThat(into.getCredential()).isEqualTo(from.getCredential());

    assertThat(into.getEpoch()).isNull();
    assertThat(into.getSlot()).isNull();
    assertThat(into.getBlockHash()).isNull();

    assertThat(into.getUpdateDateTime()).isNull();
    assertThat(into.getBlockNumber()).isNull();
    assertThat(into.getBlockTime()).isNull();
  }

  @Test
  void toDelegationEntityTest() {
    // given
    var from = TestDataGenerator.newDelegation();
    // when
    var into = mapper.toDelegationEntity(from);
    // then

    assertThat(into.getTxHash()).isEqualTo(from.getTxHash());
    assertThat(into.getCertIndex()).isEqualTo(from.getCertIndex());
    assertThat(into.getCredential()).isEqualTo(from.getCredential());
    assertThat(into.getPoolId()).isEqualTo(from.getPoolId());
    assertThat(into.getAddress()).isEqualTo(from.getAddress());

    assertThat(into.getEpoch()).isNull();
    assertThat(into.getSlot()).isNull();
    assertThat(into.getBlockHash()).isNull();

    assertThat(into.getUpdateDateTime()).isNull();
    assertThat(into.getBlockNumber()).isNull();
    assertThat(into.getBlockTime()).isNull();
  }

}
