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

    BlockToEntity my = new BlockToEntity(modelMapper);
    my.modelMapper.validate();

    BlockEntity blockEntity = newBlockEntity();

    Block model = my.fromEntity(blockEntity);

    assertThat(model.getNumber()).isEqualTo(blockEntity.getNumber());
    assertThat(model.getHash()).isEqualTo(blockEntity.getHash());
    assertThat(model.getCreatedAt()).isEqualTo(
        TimeUnit.SECONDS.toMillis(blockEntity.getBlockTimeInSeconds()));
    assertThat(model.getPreviousBlockHash()).isEqualTo(
        blockEntity.getPrev() != null ? blockEntity.getPrev().getHash() : blockEntity.getHash());
    assertThat(model.getPreviousBlockNumber()).isEqualTo(
        blockEntity.getPrev() != null ? blockEntity.getPrev().getNumber() : 0);
    assertThat(model.getTransactionsCount()).isEqualTo(blockEntity.getNoOfTxs());
    assertThat(model.getSize()).isEqualTo(Math.toIntExact(blockEntity.getBlockBodySize()));
    assertThat(model.getCreatedBy()).isEqualTo(blockEntity.getIssuerVkey());
    assertThat(model.getEpochNo()).isEqualTo(blockEntity.getEpochNumber());
    assertThat(model.getSlotNo()).isEqualTo(blockEntity.getSlot());

    //TODO saa: refactor to use Tran.fromTx
//    assertThat(model.getTransactions().size()).isEqualTo(blockEntity.getTransactions().size());

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
        .build())
        ;
  }
}