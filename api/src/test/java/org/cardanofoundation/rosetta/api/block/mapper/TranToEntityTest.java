package org.cardanofoundation.rosetta.api.block.mapper;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.BaseMapperTest;
import org.cardanofoundation.rosetta.api.block.model.domain.Tran;
import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.TxOuput;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;

import static org.assertj.core.api.Assertions.assertThat;

class TranToEntityTest extends BaseMapperTest {

  private UtxoKey inUtxKey = new UtxoKey("in_UtxoKey_txHash1", 55);
  private UtxoKey outUtxKey = new UtxoKey("out_UtxoKey_txHash1", 55);
  @Test
  void fromEntity_Test() {
    //given
    TranToEntity my = new TranToEntity(modelMapper);
    my.modelMapper.validate();
    TxnEntity from = newTxnEntity();



    //when
    Tran into = my.fromEntity(from);

    //then
    assertThat(into.getFee()).isEqualTo(from.getFee().toString());
    assertThat(into.getHash()).isEqualTo(from.getTxHash());
    assertThat(into.getBlockHash()).isEqualTo(from.getBlock().getHash());
    assertThat(into.getBlockNo()).isEqualTo(from.getBlock().getNumber());
    assertThat(into.getSize()).isEqualTo(0L);
    assertThat(into.getValidContract()).isEqualTo(from.getInvalid());
    assertThat(into.getScriptSize()).isEqualTo(0L);

    //TODO saa: refactor to use Utxo::fromUtxoKey
//    assertThat(into.getInputs().size()).isEqualTo(from.getInputKeys().size());
//    assertThat(into.getOutputs().size()).isEqualTo(from.getOutputKeys().size());



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