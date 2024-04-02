package org.cardanofoundation.rosetta.api.block.mapper;

import java.math.BigInteger;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.BaseMapperTest;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.account.model.entity.Amt;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;

import static org.assertj.core.api.Assertions.assertThat;

class BlockTxToRosettaTransactionTest extends BaseMapperTest {

  @Test
  void toDto_Test() {

    //given
    TranToRosettaTransaction my = new TranToRosettaTransaction(modelMapper);
    my.modelMapper.validate();
    BlockTx from = newTran();

    //when
    org.openapitools.client.model.Transaction into = my.toDto(from, "5000");

    //then
    my.modelMapper.validate();
    assertThat(into.getMetadata().getSize()).isEqualTo(from.getSize());
    assertThat(into.getMetadata().getScriptSize()).isEqualTo(from.getScriptSize());
    assertThat(into.getTransactionIdentifier().getHash()).isEqualTo(from.getHash());

    //TODO finalize the test
//    assertThat(into.getOperations().size()).isEqualTo(2);
//    assertThat(into.getOperations().get(0).getStatus()).isEqualTo("success");
//    assertThat(into.getOperations().get(0).getOperationIdentifier().getIndex()).isEqualTo(0);
//    assertThat(into.getOperations().get(0).getOperationIdentifier().getNetworkIndex()).isEqualTo(0);
//    assertThat(into.getOperations().getFirst().getType()).isEqualTo(Constants.INPUT);



  }

  private BlockTx newTran() {
    return BlockTx
        .builder()
        .blockNo(11L)
        .blockHash("blockHash11")
        .size(1L)
        .fee("123")
        .hash("hash12")
        .scriptSize(0L)
        .inputs(List.of(newUtxoIn()))
        .outputs(List.of(newUtxoOut()))
        .validContract(true)
        .build();
  }

  private Utxo newUtxoIn() {
    return Utxo.builder()
        .blockHash("in_blockHash1")
        .epoch(11)
        .slot(22L)
        .txHash("txHash1")
        .outputIndex(44)
        .amounts(List.of(newAmt()))
        .dataHash("in_dataHash1")
        .inlineDatum("in_inlineDatum1")
        .isCollateralReturn(true)
        .lovelaceAmount(BigInteger.TEN)
        .ownerAddr("in_ownerAddr1")
        .ownerAddrFull("ownerAddrFull1")
        .ownerPaymentCredential("in_ownerPaymentCredential1")
        .ownerStakeAddr("in_ownerStakeAddr1")
        .scriptRef("in_scriptRef1")
        .referenceScriptHash("in_referenceScriptHash1")
        .build();
  }

  private Utxo newUtxoOut() {
    return Utxo.builder()
        .blockHash("in_blockHash1")
        .epoch(11)
        .slot(22L)
        .txHash("txHash1")
        .outputIndex(44)
        .amounts(List.of(newAmt()))
        .dataHash("out_dataHash1")
        .inlineDatum("out_inlineDatum1")
        .isCollateralReturn(true)
        .lovelaceAmount(BigInteger.TEN)
        .ownerAddr("out_ownerAddr1")
        .ownerAddrFull("ownerAddrFull1")
        .ownerPaymentCredential("out_ownerPaymentCredential1")
        .ownerStakeAddr("out_ownerStakeAddr1")
        .scriptRef("out_scriptRef1")
        .referenceScriptHash("out_referenceScriptHash1")
        .build();
  }

  private static Amt newAmt() {
    return Amt.builder()
        .assetName("assetName1")
        .policyId("policyId1")
        .quantity(BigInteger.ONE)
        .unit("unit1")
        .build();
  }


}