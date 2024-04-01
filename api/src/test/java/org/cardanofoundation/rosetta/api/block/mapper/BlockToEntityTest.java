package org.cardanofoundation.rosetta.api.block.mapper;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.BaseMapperTest;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.TxOuput;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;

import static org.assertj.core.api.Assertions.assertThat;

class BlockToEntityTest extends BaseMapperTest {

  @Test
  void fromEntity_Test() {

    //given
    TranToEntity tranToEntity = new TranToEntity(modelMapper);
    BlockToEntity my = new BlockToEntity(modelMapper, tranToEntity);
    my.modelMapper.validate();
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
    assertThat(into.getCreatedBy()).isEqualTo(from.getIssuerVkey());
    assertThat(into.getEpochNo()).isEqualTo(from.getEpochNumber());
    assertThat(into.getSlotNo()).isEqualTo(from.getSlot());

    //TODO saa: refactor to use Tran.fromTx
//    assertThat(into.getTransactions().size()).isEqualTo(from.getTransactions().size());

  }

  private BlockEntity newBlockEntity() {
    return BlockEntity.builder()
        .number(1L)
        .hash("hash1")
        .blockTimeInSeconds(1L)
        .prev(null)
        .noOfTxs(1L)
        .blockBodySize(1L)
        .issuerVkey("issuerVkey1")
        .epochNumber(1)
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
        .slot(11L)
        .updateDateTime(LocalDateTime.MIN)
        .auxiliaryDataHash("auxiliaryDataHash1")
        .collateralInputs(List.of())
        .collateralReturnJson(new TxOuput())
        .invalid(false)
        .inputKeys(List.of())
        .outputKeys(List.of())
        .build());
  }
}