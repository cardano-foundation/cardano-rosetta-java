package org.cardanofoundation.rosetta.yaciindexer.mapper;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import com.bloxbean.cardano.yaci.store.account.domain.StakeAddressBalance;
import com.bloxbean.cardano.yaci.store.account.storage.impl.model.StakeAddressBalanceEntity;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.yaciindexer.BaseMapperSetup;
import org.cardanofoundation.rosetta.yaciindexer.TestDataGenerator;

import static org.assertj.core.api.Assertions.assertThat;

class CustomAccountMapperTest extends BaseMapperSetup {

  @Autowired
  private CustomAccountMapper mapper;

  @Test
  void toAddressBalanceEntityTest() {
    // given
    var from = TestDataGenerator.newAddressBalance();
    // when
    var into = mapper.toAddressBalanceEntity(from);
    // then
    assertThat(into.getAddress()).isEqualTo(from.getAddress());
    assertThat(into.getUnit()).isEqualTo(from.getUnit());
    assertThat(into.getSlot()).isEqualTo(from.getSlot());
    assertThat(into.getQuantity()).isEqualTo(from.getQuantity());
    assertThat(into.getBlockNumber()).isEqualTo(from.getBlockNumber());

    assertThat(into.getEpoch()).isNull();
    assertThat(into.getAddrFull()).isNull();

    assertThat(into.getBlockTime()).isNull();
    assertThat(into.getUpdateDateTime()).isNull();
  }

  @Test
  void toStakeBalanceEntityTest() {
    // given
    StakeAddressBalance from = newStakeAddressBalance();
    // when
    StakeAddressBalanceEntity into = mapper.toStakeBalanceEntity(from);
    // then
    assertThat(into.getAddress()).isEqualTo(from.getAddress());
    assertThat(into.getSlot()).isEqualTo(from.getSlot());
    assertThat(into.getQuantity()).isEqualTo(from.getQuantity());

    assertThat(into.getEpoch()).isNull();

    assertThat(into.getBlockNumber()).isNull();
    assertThat(into.getBlockTime()).isNull();
    assertThat(into.getUpdateDateTime()).isNull();
  }

  private StakeAddressBalance newStakeAddressBalance() {
    return StakeAddressBalance.builder()
        .address("address")
        .slot(100L)
        .quantity(BigInteger.TEN)
        .epoch(200)
        .blockNumber(300L)
        .blockTime(400L)
        .build();
  }


}
