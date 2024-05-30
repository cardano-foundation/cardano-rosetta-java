package org.cardanofoundation.rosetta.yaciindexer.mapper;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.yaciindexer.BaseMapperSetup;
import org.cardanofoundation.rosetta.yaciindexer.TestDataGenerator;

import static org.assertj.core.api.Assertions.assertThat;

class CustomBlockMapperTest extends BaseMapperSetup {

  @Autowired
  private CustomBlockMapper mapper;

  @Test
  void toBlockEntityTest() {
    // given
    var from = TestDataGenerator.newBlock();
    // when
    var into = mapper.toBlockEntity(from);
    // then
    assertThat(into.getHash()).isEqualTo(from.getHash());
    assertThat(into.getNumber()).isEqualTo(from.getNumber());
    assertThat(into.getSlot()).isEqualTo(from.getSlot());
    assertThat(into.getEpochNumber()).isEqualTo(from.getEpochNumber());
    assertThat(into.getBlockTime()).isEqualTo(from.getBlockTime());
    assertThat(into.getPrevHash()).isEqualTo(from.getPrevHash());
    assertThat(into.getBlockBodySize()).isEqualTo(from.getBlockBodySize());
    assertThat(into.getNoOfTxs()).isEqualTo(from.getNoOfTxs());
    assertThat(into.getSlotLeader()).isEqualTo(from.getSlotLeader());

    assertThat(into.getEpochSlot()).isNull();
    assertThat(into.getTotalOutput()).isNull();
    assertThat(into.getTotalFees()).isNull();
    assertThat(into.getEra()).isNull();
    assertThat(into.getIssuerVkey()).isNull();
    assertThat(into.getVrfVkey()).isNull();
    assertThat(into.getNonceVrf()).isNull();
    assertThat(into.getLeaderVrf()).isNull();
    assertThat(into.getVrfResult()).isNull();
    assertThat(into.getOpCertHotVKey()).isNull();
    assertThat(into.getOpCertSeqNumber()).isNull();
    assertThat(into.getOpcertKesPeriod()).isNull();
    assertThat(into.getOpCertSigma()).isNull();
    assertThat(into.getBlockBodyHash()).isNull();
    assertThat(into.getProtocolVersion()).isNull();
  }

}
