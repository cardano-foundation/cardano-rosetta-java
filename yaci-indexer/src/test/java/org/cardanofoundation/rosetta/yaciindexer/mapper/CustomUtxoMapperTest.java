package org.cardanofoundation.rosetta.yaciindexer.mapper;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.yaciindexer.BaseMapperSetup;
import org.cardanofoundation.rosetta.yaciindexer.TestDataGenerator;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUtxoMapperTest extends BaseMapperSetup {

  @Autowired
  private CustomUtxoMapper mapper;

  @Test
  void toAddressBalanceEntityTest() {
    // given
    var from = TestDataGenerator.newAddressUtxo();
    // when
    var into = mapper.toAddressUtxoEntity(from);
    // then
    assertThat(into.getTxHash()).isEqualTo(from.getTxHash());
    assertThat(into.getOutputIndex()).isEqualTo(from.getOutputIndex());
    assertThat(into.getOwnerAddr()).isEqualTo(from.getOwnerAddr());
    assertThat(into.getOwnerStakeAddr()).isEqualTo(from.getOwnerStakeAddr());
    assertThat(into.getAmounts()).isEqualTo(from.getAmounts());
    assertThat(into.getBlockNumber()).isEqualTo(from.getBlockNumber());

    assertThat(into.getSlot()).isNull();
    assertThat(into.getBlockHash()).isNull();
    assertThat(into.getEpoch()).isNull();
    assertThat(into.getOwnerAddrFull()).isNull();
    assertThat(into.getOwnerPaymentCredential()).isNull();
    assertThat(into.getOwnerStakeCredential()).isNull();
    assertThat(into.getLovelaceAmount()).isNull();
    assertThat(into.getDataHash()).isNull();
    assertThat(into.getInlineDatum()).isNull();
    assertThat(into.getScriptRef()).isNull();
    assertThat(into.getReferenceScriptHash()).isNull();
    assertThat(into.getIsCollateralReturn()).isNull();

    assertThat(into.getBlockTime()).isNull();
    assertThat(into.getUpdateDateTime()).isNull();
  }

}
