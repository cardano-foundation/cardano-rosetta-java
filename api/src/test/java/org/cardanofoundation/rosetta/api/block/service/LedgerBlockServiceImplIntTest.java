package org.cardanofoundation.rosetta.api.block.service;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.block.model.domain.*;
import org.cardanofoundation.rosetta.api.block.model.entity.*;
import org.cardanofoundation.rosetta.testgenerator.common.TransactionBlockDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.rosetta.testgenerator.common.TestConstants.STAKE_ADDRESS_WITH_EARNED_REWARDS;
import static org.cardanofoundation.rosetta.testgenerator.common.TestTransactionNames.*;

@Transactional
class LedgerBlockServiceImplIntTest extends IntegrationTest {

  @Autowired
  LedgerBlockService ledgerBlockService;

  @PersistenceContext
  EntityManager entityManager;

  @RepeatedTest(10)
  @Execution(ExecutionMode.CONCURRENT)
  void findBlock_Test_OK_tx_blk() {
    //given
    TransactionBlockDetails tx = generatedDataMap.get(SIMPLE_TRANSACTION.getName());
    //when
    Optional<Block> block = ledgerBlockService.findBlock(tx.blockNumber(), tx.blockHash());
    //then
    assertBlockAndTx(block, tx);
  }

  @RepeatedTest(10)
  @Execution(ExecutionMode.CONCURRENT)
  void findBlockIdentifier_Test_OK_tx_blk() {
    //given
    TransactionBlockDetails tx = generatedDataMap.get(SIMPLE_TRANSACTION.getName());
    //when
    Optional<BlockIdentifierExtended> block = ledgerBlockService.findBlockIdentifier(tx.blockNumber(), tx.blockHash());
    //then
    assertBlockIdentifierAndTx(block, tx);
  }

  @Test
  void findBlock_Test_OK_tx_null() {
    //given
    TransactionBlockDetails tx = generatedDataMap.get(SIMPLE_TRANSACTION.getName());
    //when
    Optional<Block> block = ledgerBlockService.findBlock(tx.blockNumber(), null);
    //then
    assertBlockAndTx(block, tx);
  }

  @Test
  void findBlockIdentifier_Test_OK_tx_null() {
    //given
    TransactionBlockDetails tx = generatedDataMap.get(SIMPLE_TRANSACTION.getName());
    //when
    Optional<BlockIdentifierExtended> block = ledgerBlockService.findBlockIdentifier(tx.blockNumber(), null);
    //then
    assertBlockIdentifierAndTx(block, tx);
  }

  @Test
  void findBlock_Test_OK_null_blk() {
    //given
    TransactionBlockDetails tx = generatedDataMap.get(SIMPLE_TRANSACTION.getName());
    //when
    Optional<Block> block = ledgerBlockService.findBlock(null, tx.blockHash());
    //then
    assertBlockAndTx(block, tx);
  }

  @Test
  void findBlockIdentifier_Test_OK_null_blk() {
    //given
    TransactionBlockDetails tx = generatedDataMap.get(SIMPLE_TRANSACTION.getName());
    //when
    Optional<BlockIdentifierExtended> block = ledgerBlockService.findBlockIdentifier(null, tx.blockHash());
    //then
    assertBlockIdentifierAndTx(block, tx);
  }

  @Test
  void findBlock_Test_OK_null() {
    //given
    //when
    Optional<Block> block = ledgerBlockService.findBlock(null, null);
    //then
    assertThat(block).isPresent();
  }

  @Test
  void findBlock_Test_OK_empty() {
    //given
    //when
    Optional<Block> block = ledgerBlockService.findBlock(-234L, "#####2");
    //then
    assertThat(block).isEmpty();
  }

  @Test
  void findTransactionsByBlock_Test_empty_tx() {
    //given
    //when
    List<BlockTx> txs = ledgerBlockService.findTransactionsByBlock(-123L, "#####");
    //then
    assertThat(txs).isEmpty();
  }

  @Test
  void findTransactionsByBlock_Test_pool_delegation_tx() {
    //given
    TransactionBlockDetails tx = generatedDataMap.get(POOL_DELEGATION_TRANSACTION.getName());
    //when

    List<BlockTx> txs =
        ledgerBlockService.findTransactionsByBlock(tx.blockNumber(), tx.blockHash());

    //then
    assertThat(txs).isNotNull().hasSize(1);

    BlockTx blockTx = txs.getFirst();
    assertThat(blockTx.getHash()).isEqualTo(tx.txHash());
    assertThat(blockTx.getBlockNo()).isEqualTo(tx.blockNumber());
    assertThat(blockTx.getBlockHash()).isEqualTo(tx.blockHash());

    assertThat(blockTx.getStakePoolDelegations()).hasSize(1);
    assertThat(blockTx.getStakePoolDelegations().getFirst().getAddress())
        .isEqualTo(STAKE_ADDRESS_WITH_EARNED_REWARDS);

    List<PoolDelegationEntity> poolDelegations = entityManager
        .createQuery("FROM PoolDelegationEntity b where b.txHash=:hash", PoolDelegationEntity.class)
        .setParameter("hash", tx.txHash())
        .getResultList();
    assertThat(poolDelegations).isNotNull().hasSize(1);
    PoolDelegationEntity expected = poolDelegations.getFirst();
    assertThat(expected.getAddress()).isEqualTo(STAKE_ADDRESS_WITH_EARNED_REWARDS);

    StakePoolDelegation actual = blockTx.getStakePoolDelegations().getFirst();
    assertThat(actual.getTxHash()).isEqualTo(expected.getTxHash());
    assertThat(actual.getAddress()).isEqualTo(expected.getAddress());
    assertThat(actual.getPoolId()).isEqualTo(expected.getPoolId());
    assertThat(actual.getCertIndex()).isEqualTo(expected.getCertIndex());
  }

  @Test
  void findTransactionsByBlock_Test_pool_reg_tx() {
    //given
    TransactionBlockDetails tx = generatedDataMap.get(POOL_REGISTRATION_TRANSACTION.getName());
    //when
    List<BlockTx> txs =
        ledgerBlockService.findTransactionsByBlock(tx.blockNumber(), tx.blockHash());
    //then
    assertThat(txs).isNotNull().hasSize(1);

    BlockTx blockTx = txs.getFirst();
    assertThat(blockTx.getHash()).isEqualTo(tx.txHash());
    assertThat(blockTx.getBlockNo()).isEqualTo(tx.blockNumber());
    assertThat(blockTx.getBlockHash()).isEqualTo(tx.blockHash());
    assertThat(blockTx.getPoolRegistrations()).hasSize(1);

    List<PoolRegistrationEntity> entity = entityManager
        .createQuery(
            "FROM PoolRegistrationEntity b where b.txHash=:hash", PoolRegistrationEntity.class)
        .setParameter("hash", tx.txHash())
        .getResultList();
    assertThat(entity).isNotNull().hasSize(1);
    PoolRegistrationEntity expected = entity.getFirst();

    PoolRegistration actual = blockTx.getPoolRegistrations().getFirst();
    assertThat(actual.getTxHash()).isEqualTo(expected.getTxHash());
    assertThat(actual.getPoolId()).isEqualTo(expected.getPoolId());
    assertThat(actual.getCertIndex()).isEqualTo(expected.getCertIndex());
    assertThat(actual.getVrfKeyHash()).isEqualTo(expected.getVrfKeyHash());
    assertThat(actual.getPledge()).isEqualTo(expected.getPledge().toString());
    assertThat(actual.getMargin()).isEqualTo(expected.getMargin().toString());
    assertThat(actual.getCost()).isEqualTo(expected.getCost().toString());
    assertThat(actual.getRewardAccount()).isEqualTo(expected.getRewardAccount());
    assertThat(actual.getOwners()).isNotEmpty();
    assertThat(actual.getOwners()).containsExactlyInAnyOrderElementsOf(expected.getPoolOwners());
    assertThat(actual.getRelays()).hasSize(1);
    assertThat(actual.getRelays().getFirst())
        .usingRecursiveComparison()
        .ignoringFields("type")
        // If you have a warning in IDEA for the isEqualTo than it is not fixed yet:
        // https://youtrack.jetbrains.com/issue/IDEA-347472/
        .isEqualTo(expected.getRelays().getFirst());
  }

  @Test
  void findTransactionsByBlock_Test_pool_ret_tx() {
    //given
    TransactionBlockDetails tx = generatedDataMap.get(POOL_RETIREMENT_TRANSACTION.getName());
    //when
    List<BlockTx> txs =
        ledgerBlockService.findTransactionsByBlock(tx.blockNumber(), tx.blockHash());
    //then
    assertThat(txs).isNotNull().hasSize(1);

    BlockTx blockTx = txs.getFirst();
    assertThat(blockTx.getHash()).isEqualTo(tx.txHash());
    assertThat(blockTx.getBlockNo()).isEqualTo(tx.blockNumber());
    assertThat(blockTx.getBlockHash()).isEqualTo(tx.blockHash());
    assertThat(blockTx.getPoolRetirements()).hasSize(1);

    List<PoolRetirementEntity> entity = entityManager
        .createQuery("FROM PoolRetirementEntity b where b.txHash=:hash", PoolRetirementEntity.class)
        .setParameter("hash", tx.txHash())
        .getResultList();
    assertThat(entity).isNotNull().hasSize(1);
    PoolRetirementEntity expected = entity.getFirst();

    PoolRetirement actual = blockTx.getPoolRetirements().getFirst();
    assertThat(actual.getTxHash()).isEqualTo(expected.getTxHash());
    assertThat(actual.getPoolId()).isEqualTo(expected.getPoolId());
    assertThat(actual.getCertIndex()).isEqualTo(expected.getCertIndex());
    assertThat(actual.getEpoch()).isEqualTo(expected.getEpoch());
  }

  @Test
  void findTransactionsByBlock_Test_stake_pool_tx() {
    //given
    TransactionBlockDetails tx = generatedDataMap.get(STAKE_KEY_REGISTRATION_TRANSACTION.getName());
    //when
    List<BlockTx> txs =
        ledgerBlockService.findTransactionsByBlock(tx.blockNumber(), tx.blockHash());
    //then
    assertThat(txs).isNotNull().hasSize(1);

    BlockTx blockTx = txs.getFirst();
    assertThat(blockTx.getHash()).isEqualTo(tx.txHash());
    assertThat(blockTx.getBlockNo()).isEqualTo(tx.blockNumber());
    assertThat(blockTx.getBlockHash()).isEqualTo(tx.blockHash());
    assertThat(blockTx.getStakeRegistrations()).hasSize(1);

    List<StakeRegistrationEntity> entity = entityManager
        .createQuery("FROM StakeRegistrationEntity b where b.txHash=:hash",
            StakeRegistrationEntity.class)
        .setParameter("hash", tx.txHash())
        .getResultList();
    assertThat(entity).isNotNull().hasSize(1);
    StakeRegistrationEntity expected = entity.getFirst();

    StakeRegistration actual = blockTx.getStakeRegistrations().getFirst();
    assertThat(actual.getTxHash()).isEqualTo(expected.getTxHash());
    assertThat(actual.getAddress()).isEqualTo(expected.getAddress());
    assertThat(actual.getCertIndex()).isEqualTo(expected.getCertIndex());
    assertThat(actual.getType()).isEqualTo(expected.getType());
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
  void findLatestBlockIdentifier() {
    //given
    BlockEntity fromBlockB = entityManager
        .createQuery("FROM BlockEntity b "
            + "ORDER BY b.number DESC LIMIT 1", BlockEntity.class)
        .setMaxResults(1)
        .getSingleResult();
    //when
    BlockIdentifierExtended latestBlock = ledgerBlockService.findLatestBlockIdentifier();
    //then
    assertThat(fromBlockB).isNotNull();
    assertBlockIdentifier(latestBlock, fromBlockB);
  }

  @Test
  void findGenesisBlockIdentifier() {
    //given
    BlockEntity fromBlockB = entityManager
        .createQuery("FROM BlockEntity b "
            + "WHERE b.number = 0 ORDER BY b.number ASC LIMIT 1", BlockEntity.class)
        .setMaxResults(1)
        .getSingleResult();
    //when

    BlockIdentifierExtended genesisBlock = ledgerBlockService.findGenesisBlockIdentifier();
    //then
    assertThat(fromBlockB).isNotNull();
    assertBlockIdentifier(genesisBlock, fromBlockB);
    assertThat(genesisBlock.getHash()).isEqualTo("Genesis");
  }

  private static void assertBlocks(Block latestBlock, BlockEntity fromBlockB) {
    assertThat(latestBlock).isNotNull();
    assertThat(latestBlock.getHash()).isEqualTo(fromBlockB.getHash());
    assertThat(latestBlock.getNumber()).isEqualTo(fromBlockB.getNumber());
    assertThat(latestBlock.getTransactions()).hasSize(fromBlockB.getTransactions().size());
    assertThat(latestBlock.getEpochNo()).isEqualTo(fromBlockB.getEpochNumber());
  }

  private static void assertBlockIdentifier(BlockIdentifierExtended blockIdentifier,
      BlockEntity fromBlockB) {
    assertThat(blockIdentifier).isNotNull();
    assertThat(blockIdentifier.getHash()).isEqualTo(fromBlockB.getHash());
    assertThat(blockIdentifier.getNumber()).isEqualTo(fromBlockB.getNumber());
    assertThat(blockIdentifier.getBlockTimeInSeconds())
        .isEqualTo(fromBlockB.getBlockTimeInSeconds());
  }

  private static void assertBlockAndTx(Optional<Block> blockOpt, TransactionBlockDetails tx) {
    if (blockOpt.isEmpty()) {
      throw new AssertionError("Not a block");
    }
    var block = blockOpt.get();
    assertThat(block).isNotNull();
    assertThat(block.getHash()).isEqualTo(tx.blockHash());
    assertThat(block.getNumber()).isEqualTo(tx.blockNumber());
    assertThat(block.getTransactions()).hasSize(1);
    assertThat(block.getTransactions().getFirst().getHash()).isEqualTo(tx.txHash());
  }

  private static void assertBlockIdentifierAndTx(Optional<BlockIdentifierExtended> blockOpt, TransactionBlockDetails tx) {
    if (blockOpt.isEmpty()) {
      throw new AssertionError("Not a block");
    }
    var block = blockOpt.get();
    assertThat(block).isNotNull();
    assertThat(block.getHash()).isEqualTo(tx.blockHash());
    assertThat(block.getNumber()).isEqualTo(tx.blockNumber());
  }
}
