package org.cardanofoundation.rosetta.api.block.mapper;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.BaseMapperSetup;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;

import static org.assertj.core.api.Assertions.assertThat;

class BlockToEntityTest extends BaseMapperSetup {

  @Autowired
  private BlockToEntity my;

  @Test
  void fromEntity_Test() {
    //given
    BlockEntity from = newBlockEntity();
    //when
    Block into = my.fromEntity(from);
    //then
    assertThat(into.getNumber()).isEqualTo(from.getNumber());
    assertThat(into.getHash()).isEqualTo(from.getHash());

    assertThat(into.getCreatedAt()).isEqualTo(
        TimeUnit.SECONDS.toMillis(from.getBlockTimeInSeconds()));

    assertThat(into.getPreviousBlockHash()).isEqualTo(
        from.getPrev() != null ? from.getPrev().getHash() : from.getHash());

    assertThat(into.getPreviousBlockNumber()).isEqualTo(
        from.getPrev() != null ? from.getPrev().getNumber() : 0);

    assertThat(into.getTransactionsCount()).isEqualTo(from.getNoOfTxs());
    assertThat(into.getSize()).isEqualTo(Math.toIntExact(from.getBlockBodySize()));
    assertThat(into.getCreatedBy()).isEqualTo(from.getSlotLeader());
    assertThat(into.getSlotNo()).isEqualTo(from.getSlot());

    assertThat(into.getTransactions()).hasSameSizeAs(from.getTransactions());
    assertThat(into.getTransactions()).hasSize(1);

    assertThat(to(into).getHash()).isEqualTo(got(from).getTxHash());
    assertThat(to(into).getBlockHash()).isEqualTo(got(from).getBlock().getHash());
    assertThat(to(into).getBlockNo()).isEqualTo(got(from).getBlock().getNumber());
    assertThat(to(into).getSize()).isZero();
    assertThat(to(into).getScriptSize()).isZero();
    assertThat(to(into).getInputs()).hasSameSizeAs(got(from).getInputKeys());
    assertThat(to(into).getOutputs()).hasSameSizeAs(got(from).getOutputKeys());
    assertThat(to(into).getFee()).isEqualTo(got(from).getFee().toString());


  }

  private static TxnEntity got(BlockEntity from) {
    return from.getTransactions().getFirst();
  }

  private static BlockTx to(Block into) {
    return into.getTransactions().getFirst();
  }

  private BlockEntity newBlockEntity() {
    return BlockEntity.builder()
        .number(1L)
        .hash("hash1")
        .blockTimeInSeconds(1L)
        .prev(null)
        .noOfTxs(1L)
        .blockBodySize(1L)
        .slot(1L)
        .transactions(newTxList())
        .build();
  }

  private List<TxnEntity> newTxList() {
    return List.of(TxnEntity
        .builder()
        .txHash("txHash1")
        .block(BlockEntity.builder().hash("blockHash1").number(22L).build())
        .fee(BigInteger.TEN)
        .inputKeys(List.of())
        .outputKeys(List.of())
        .build());
  }
}
