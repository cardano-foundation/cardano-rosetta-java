package org.cardanofoundation.rosetta.api.block.service;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.domain.GenesisBlock;
import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;
import org.cardanofoundation.rosetta.testgenerator.common.TransactionBlockDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.rosetta.testgenerator.common.TestConstants.STAKE_ADDRESS_WITH_EARNED_REWARDS;
import static org.cardanofoundation.rosetta.testgenerator.common.TestTransactionNames.POOL_DELEGATION_TRANSACTION;
import static org.cardanofoundation.rosetta.testgenerator.common.TestTransactionNames.SIMPLE_TRANSACTION;

class LedgerBlockServiceImplIntTest extends IntegrationTest {

  @Autowired
  LedgerBlockService ledgerBlockService;

  @PersistenceContext
  EntityManager entityManager;


  @Test
  void findBlock_Test_OK_tx_blk() {
    //given
    TransactionBlockDetails tx = generatedDataMap.get(SIMPLE_TRANSACTION.getName());
    //when
    Block block = ledgerBlockService.findBlock(tx.blockNumber(), tx.blockHash());
    //then
    assertBlockAndTx(block, tx);
  }

  @Test
  void findBlock_Test_OK_tx_null() {
    //given
    TransactionBlockDetails tx = generatedDataMap.get(SIMPLE_TRANSACTION.getName());
    //when
    Block block = ledgerBlockService.findBlock(tx.blockNumber(), null);
    //then
    assertBlockAndTx(block, tx);
  }

  @Test
  void findBlock_Test_OK_null_blk() {
    //given
    TransactionBlockDetails tx = generatedDataMap.get(SIMPLE_TRANSACTION.getName());
    //when
    Block block = ledgerBlockService.findBlock(null, tx.blockHash());
    //then
    assertBlockAndTx(block, tx);
  }




  @Test
  void findTransactionsByBlock_Test_pool_tx() {
    //given
    TransactionBlockDetails tx = generatedDataMap.get(POOL_DELEGATION_TRANSACTION.getName());
    //when
    List<BlockTx> txs =
        ledgerBlockService.findTransactionsByBlock(tx.blockNumber(), tx.blockHash());

    assertThat(txs).isNotNull();
    assertThat(txs.size()).isEqualTo(1);

    BlockTx blockTx = txs.getFirst();
    assertThat(blockTx.getHash()).isEqualTo(tx.txHash());
    assertThat(blockTx.getBlockNo()).isEqualTo(tx.blockNumber());
    assertThat(blockTx.getBlockHash()).isEqualTo(tx.blockHash());
    assertThat(blockTx.getDelegations().size()).isEqualTo(1);
    assertThat(blockTx.getDelegations().getFirst().getAddress())
        .isEqualTo(STAKE_ADDRESS_WITH_EARNED_REWARDS);
  }

  @Test
  void findLatestBlock() {
    //given
    BlockEntity fromBlockB = entityManager
        .createQuery("FROM BlockEntity b ORDER BY b.number DESC", BlockEntity.class)
        .setMaxResults(1)
        .getSingleResult();
    //when
    Block latestBlock = ledgerBlockService.findLatestBlock();
    //then
    assertThat(fromBlockB).isNotNull();
    assertBlocks(latestBlock, fromBlockB);
  }

  @Test
  void findGenesisBlock() {
    //given
    BlockEntity fromBlockB = entityManager
        .createQuery("FROM BlockEntity b ORDER BY b.number ASC", BlockEntity.class)
        .setMaxResults(1)
        .getSingleResult();
    //when
    GenesisBlock genesisBlock = ledgerBlockService.findGenesisBlock();
    //then
    assertThat(fromBlockB).isNotNull();
    assertThat(genesisBlock).isNotNull();
    assertThat(genesisBlock.getHash()).isEqualTo(fromBlockB.getHash());
    assertThat(genesisBlock.getNumber()).isEqualTo(-1);
    assertThat(genesisBlock.getNumber()).isEqualTo(fromBlockB.getNumber());
    assertThat(genesisBlock.getHash()).isEqualTo("Genesis");

  }

  private static void assertBlocks(Block latestBlock, BlockEntity fromBlockB) {
    assertThat(latestBlock).isNotNull();
    assertThat(latestBlock.getHash()).isEqualTo(fromBlockB.getHash());
    assertThat(latestBlock.getSlotNo()).isEqualTo(fromBlockB.getNumber());
    assertThat(latestBlock.getTransactions().size()).isEqualTo(fromBlockB.getTransactions().size());
    assertThat(latestBlock.getEpochNo()).isEqualTo(fromBlockB.getEpochNumber());
  }

  private static void assertBlockAndTx(Block block, TransactionBlockDetails tx) {
    assertThat(block).isNotNull();
    assertThat(block.getHash()).isEqualTo(tx.blockHash());
    assertThat(block.getSlotNo()).isEqualTo(tx.blockNumber());
    assertThat(block.getTransactions().size()).isEqualTo(1);
    assertThat(block.getTransactions().getFirst().getHash()).isEqualTo(tx.txHash());
  }
}