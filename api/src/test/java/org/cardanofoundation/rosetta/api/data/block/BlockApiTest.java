package org.cardanofoundation.rosetta.api.data.block;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.service.BlockService;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class BlockApiTest extends IntegrationTest {

  @Autowired
  private BlockService blockService;

  @Test
  public void getBlockWithTransaction_Test() {
    Block block = blockService.findBlock((long) generatedTestData.getTopUpBlockNumber(),
        generatedTestData.getTopUpBlockHash());

    assertEquals(generatedTestData.getTopUpBlockHash(), block.getHash());
    assertEquals(generatedTestData.getTopUpBlockNumber(), block.getSlotNo());
    assertEquals(1, block.getTransactions().size());

    Utxo receiverUtxoDto = block.getTransactions().get(0).getOutputs().get(0);
    assertEquals(TestConstants.TEST_ACCOUNT_ADDRESS, receiverUtxoDto.getOwnerAddr());
    assertEquals(generatedTestData.getTopUpTxHash(), receiverUtxoDto.getTxHash());
    assertEquals(TestConstants.ACCOUNT_BALANCE_ADA_AMOUNT, receiverUtxoDto.getLovelaceAmount().toString());

  }

}
