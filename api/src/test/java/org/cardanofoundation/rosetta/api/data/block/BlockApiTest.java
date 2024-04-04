package org.cardanofoundation.rosetta.api.data.block;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.service.BlockService;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;
import org.cardanofoundation.rosetta.testgenerator.common.TransactionBlockDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlockApiTest extends IntegrationTest {

  @Autowired
  @SuppressWarnings("unused")
  private BlockService blockService;

  @Test
  void getBlockWithTransaction_Test() {
    TransactionBlockDetails generatedTestData = generatedDataMap.get(TestConstants.SIMPLE_TRANSACTION_NAME);
    Block block = blockService.findBlock(generatedTestData.blockNumber(),
        generatedTestData.blockHash());

    assertEquals(generatedTestData.blockHash(), block.getHash());
    assertEquals(generatedTestData.blockNumber(), block.getSlotNo());
    assertEquals(1, block.getTransactions().size());

    Utxo receiverUtxoDto = block.getTransactions().getFirst().getOutputs().getFirst();
    assertEquals(TestConstants.TEST_ACCOUNT_ADDRESS, receiverUtxoDto.getOwnerAddr());
    assertEquals(generatedTestData.txHash(), receiverUtxoDto.getTxHash());
    assertEquals(TestConstants.ACCOUNT_BALANCE_LOVELACE_AMOUNT, receiverUtxoDto.getLovelaceAmount().toString());

  }

}
