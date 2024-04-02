package org.cardanofoundation.rosetta.api.block.mapper;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.BaseMapperTest;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.TxOuput;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;

import static org.assertj.core.api.Assertions.assertThat;

class BlockTxToEntityTest extends BaseMapperTest {

  private final UtxoKey inUtxKey = new UtxoKey("in_UtxoKey_txHash1", 55);
  private final UtxoKey outUtxKey = new UtxoKey("out_UtxoKey_txHash1", 55);
  @Test
  void fromEntity_Test() {
    //given
    TranToEntity my = new TranToEntity(modelMapper);
    my.modelMapper.validate();
    TxnEntity from = newTxnEntity();
    //when
    BlockTx into = my.fromEntity(from);
    //then
    assertThat(into.getFee()).isEqualTo(from.getFee().toString());
    assertThat(into.getHash()).isEqualTo(from.getTxHash());
    assertThat(into.getBlockHash()).isEqualTo(from.getBlock().getHash());
    assertThat(into.getBlockNo()).isEqualTo(from.getBlock().getNumber());
    assertThat(into.getSize()).isEqualTo(0L);
    assertThat(into.getValidContract()).isEqualTo(from.getInvalid());
    assertThat(into.getScriptSize()).isEqualTo(0L);

    assertThat(into.getInputs().size()).isEqualTo(from.getInputKeys().size());
    assertThat(into.getInputs()).extracting("txHash")
        .isEqualTo(List.of(inUtxKey.getTxHash()));
    assertThat(into.getInputs()).extracting("outputIndex")
        .isEqualTo(List.of(inUtxKey.getOutputIndex()));

    assertThat(into.getOutputs().size()).isEqualTo(from.getOutputKeys().size());
    assertThat(into.getOutputs()).extracting("txHash")
        .isEqualTo(List.of(outUtxKey.getTxHash()));
    assertThat(into.getOutputs()).extracting("outputIndex")
        .isEqualTo(List.of(outUtxKey.getOutputIndex()));

  }

  private TxnEntity newTxnEntity() {

    return TxnEntity
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
        .inputKeys(List.of(inUtxKey))
        .outputKeys(List.of(outUtxKey))
        .build();
  }

}