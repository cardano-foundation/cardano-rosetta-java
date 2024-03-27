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
  @SuppressWarnings("unused")
  private BlockService blockService;

  @Test
  public void getBlockWithTransaction_Test() {
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

}
