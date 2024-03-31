package org.cardanofoundation.rosetta.api.block.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlockServiceImplIntTest extends IntegrationTest {

  @Autowired
  @SuppressWarnings("unused")
  private BlockService blockService;

  @Test
  void getBlockWithTransaction_Test() {
    Block block = blockService.findBlock(generatedTestData.getTopUpBlockNumber(),
        generatedTestData.getTopUpBlockHash());

    assertEquals(generatedTestData.getTopUpBlockHash(), block.getHash());
    assertEquals(generatedTestData.getTopUpBlockNumber(), block.getSlotNo());
    assertEquals(1, block.getTransactions().size());

    Utxo receiverUtxoDto = block.getTransactions().getFirst().getOutputs().getFirst();
    assertEquals(TestConstants.TEST_ACCOUNT_ADDRESS, receiverUtxoDto.getOwnerAddr());
    assertEquals(generatedTestData.getTopUpTxHash(), receiverUtxoDto.getTxHash());
    assertEquals(TestConstants.ACCOUNT_BALANCE_ADA_AMOUNT, receiverUtxoDto.getLovelaceAmount().toString());

  }

  //  @Test
//  void getBlockTransaction_Test() {
//
//    Tran tx = blockService.getBlockTransaction(generatedTestData.getTopUpBlockNumber(),
//        generatedTestData.getTopUpBlockHash(), generatedTestData.getTopUpTxHash());
//
//    assertEquals(generatedTestData.getTopUpTxHash(), tx.getHash());
//    assertEquals(generatedTestData.getTopUpBlockNumber(), tx.getBlockNo());
//
//    Utxo receiverUtxoDto = tx.getOutputs().getFirst();
//    assertEquals(TestConstants.TEST_ACCOUNT_ADDRESS, receiverUtxoDto.getOwnerAddr());
//    assertEquals(generatedTestData.getTopUpTxHash(), receiverUtxoDto.getTxHash());
//    assertEquals(TestConstants.ACCOUNT_BALANCE_ADA_AMOUNT, receiverUtxoDto.getLovelaceAmount().toString());
//
//  }

}
