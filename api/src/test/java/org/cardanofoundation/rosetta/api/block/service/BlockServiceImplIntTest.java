package org.cardanofoundation.rosetta.api.block.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.Tran;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlockServiceImplIntTest extends IntegrationTest {

  @Autowired
  @SuppressWarnings("unused")
  private BlockService blockService;

  @Test
  void getBlockWithTransaction_Test() {
    //given
    //when
    Block block = blockService.findBlock(generatedTestData.getTopUpBlockNumber(),
        generatedTestData.getTopUpBlockHash());

    //then
    assertEquals(generatedTestData.getTopUpBlockHash(), block.getHash());
    assertEquals(generatedTestData.getTopUpBlockNumber(), block.getSlotNo());
    assertEquals(1, block.getTransactions().size());

    Utxo receiverUtxoDto = block.getTransactions().getFirst().getOutputs().getFirst();
    assertEquals(TestConstants.TEST_ACCOUNT_ADDRESS, receiverUtxoDto.getOwnerAddr());
    assertEquals(generatedTestData.getTopUpTxHash(), receiverUtxoDto.getTxHash());
    assertEquals(TestConstants.ACCOUNT_BALANCE_ADA_AMOUNT,
        receiverUtxoDto.getLovelaceAmount().toString());

  }

  @Test
  void getBlockTransaction_Test() {

    //given
    //when
    Tran tx = blockService.getBlockTransaction(
        generatedTestData.getTopUpBlockNumber(),
        generatedTestData.getTopUpBlockHash(),
        generatedTestData.getTopUpTxHash());

    //then
    assertEquals(generatedTestData.getTopUpTxHash(), tx.getHash());
    assertEquals(generatedTestData.getTopUpBlockNumber(), tx.getBlockNo());
    assertEquals("172321", tx.getFee());
    assertEquals(0, tx.getSize());
    assertEquals(false, tx.getValidContract());
    assertEquals(0, tx.getScriptSize());
    assertEquals(1, tx.getInputs().size());
    assertEquals(2, tx.getOutputs().size());
    assertEquals(0, tx.getStakeRegistrations().size());
    assertEquals(0, tx.getPoolRegistrations().size());
    assertEquals(0, tx.getPoolRetirements().size());
    assertEquals(0, tx.getDelegations().size());

    Utxo first = tx.getInputs().getFirst();

    //TODO saa: how to check?
//    assertEquals(TestConstants.TEST_ACCOUNT_ADDRESS, first.getOwnerAddr());
//    assertEquals(generatedTestData.getTopUpTxHash(), first.getTxHash());

    Utxo receiverUtxoDto = tx.getOutputs().getFirst();
    assertEquals(TestConstants.TEST_ACCOUNT_ADDRESS, receiverUtxoDto.getOwnerAddr());
    assertEquals(generatedTestData.getTopUpTxHash(), receiverUtxoDto.getTxHash());
    assertEquals(TestConstants.ACCOUNT_BALANCE_ADA_AMOUNT,
        receiverUtxoDto.getLovelaceAmount().toString());

  }

}
