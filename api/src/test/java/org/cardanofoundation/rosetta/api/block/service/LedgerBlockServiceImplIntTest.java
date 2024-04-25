package org.cardanofoundation.rosetta.api.block.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.testgenerator.common.TransactionBlockDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.rosetta.testgenerator.common.TestTransactionNames.SIMPLE_TRANSACTION;

class LedgerBlockServiceImplIntTest extends IntegrationTest {

  @Autowired
  LedgerBlockService ledgerBlockService;


  @Test
  void findBlock_Test_OK() {
    //given
    TransactionBlockDetails tx = generatedDataMap.get(SIMPLE_TRANSACTION.getName());
    //when
    Block block = ledgerBlockService.findBlock(tx.blockNumber(), tx.blockHash());
    //then
    assertThat(block).isNotNull();
    assertThat(block.getHash()).isEqualTo(tx.blockHash());
    assertThat(block.getSlotNo()).isEqualTo(tx.blockNumber());
    assertThat(block.getTransactions().size()).isEqualTo(1);
    assertThat(block.getTransactions().getFirst().getHash()).isEqualTo(tx.txHash());
  }

  @Test
  void findTransactionsByBlock() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Test
  void findLatestBlock() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Test
  void findGenesisBlock() {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}