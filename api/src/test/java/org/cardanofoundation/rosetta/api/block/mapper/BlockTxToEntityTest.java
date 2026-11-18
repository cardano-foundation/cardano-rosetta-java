package org.cardanofoundation.rosetta.api.block.mapper;

import java.math.BigInteger;
import java.util.List;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.BaseMapperSetup;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.TransactionSizeEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;

import static org.assertj.core.api.Assertions.assertThat;

class BlockTxToEntityTest extends BaseMapperSetup {

  @Autowired
  private BlockMapper my;

  private final UtxoKey inUtxKey = new UtxoKey("in_UtxoKey_txHash1", 55);
  private final UtxoKey outUtxKey = new UtxoKey("out_UtxoKey_txHash1", 55);
  @Test
  void mapToBlockTx_Test() {
    //given
    TxnEntity from = newTxnEntity(new TransactionSizeEntity("txHash1", 22L, 703));
    //when
    BlockTx into = my.mapToBlockTx(from);
    //then
    assertThat(into.getFee()).isEqualTo(from.getFee().toString());
    assertThat(into.getHash()).isEqualTo(from.getTxHash());
    assertThat(into.getBlockHash()).isEqualTo(from.getBlock().getHash());
    assertThat(into.getBlockNo()).isEqualTo(from.getBlock().getNumber());
    assertThat(into.getSize()).isEqualTo(703L);

    assertThat(into.getInputs()).hasSameSizeAs(from.getInputKeys());
    assertThat(into.getInputs()).extracting("txHash")
        .isEqualTo(List.of(inUtxKey.getTxHash()));
    assertThat(into.getInputs()).extracting("outputIndex")
        .isEqualTo(List.of(inUtxKey.getOutputIndex()));

    assertThat(into.getOutputs()).hasSameSizeAs(from.getOutputKeys());
    assertThat(into.getOutputs()).extracting("txHash")
        .isEqualTo(List.of(outUtxKey.getTxHash()));
    assertThat(into.getOutputs()).extracting("outputIndex")
        .isEqualTo(List.of(outUtxKey.getOutputIndex()));
  }

  @Test
  void mapToBlockTx_noSizeEntity_shouldReturnZeroSize() {
    TxnEntity from = newTxnEntity(null);

    BlockTx into = my.mapToBlockTx(from);

    assertThat(into.getSize()).isZero();
  }

  private TxnEntity newTxnEntity(@Nullable TransactionSizeEntity sizeEntity) {

    return TxnEntity
        .builder()
        .txHash("txHash1")
        .block(BlockEntity.builder().hash("blockHash1").number(22L).build())
        .fee(BigInteger.TEN)
        .inputKeys(List.of(inUtxKey))
        .outputKeys(List.of(outUtxKey))
        .sizeEntity(sizeEntity)
        .build();
  }

}
