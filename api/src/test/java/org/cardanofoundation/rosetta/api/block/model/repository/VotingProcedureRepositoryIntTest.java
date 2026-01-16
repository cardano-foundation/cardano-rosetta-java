package org.cardanofoundation.rosetta.api.block.model.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.block.model.entity.VotingProcedureEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.VoterType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for VotingProcedureRepository.
 * Tests repository methods for querying SPO voting procedures from the database.
 *
 * These tests will work once test data is generated with SPO voting transactions.
 */
@Transactional
class VotingProcedureRepositoryIntTest extends IntegrationTest {

  @Autowired
  private VotingProcedureRepository votingProcedureRepository;

  @PersistenceContext
  private EntityManager entityManager;

  @Nested
  class FindByTxHashInAndVoterTypeTests {

    @Test
    void shouldReturnOnlySpoVotesWhenFilteringByStakingPoolKeyHash() {
      // given
      String txHash1 = "spo_vote_tx_" + UUID.randomUUID();
      String txHash2 = "spo_vote_tx_" + UUID.randomUUID();
      String drepVoteTxHash = "drep_vote_tx_" + UUID.randomUUID();

      // Create SPO votes
      createAndPersistVotingProcedure(txHash1, "pool_hash_1", VoterType.STAKING_POOL_KEY_HASH);
      createAndPersistVotingProcedure(txHash2, "pool_hash_2", VoterType.STAKING_POOL_KEY_HASH);

      // Create a DRep vote (should be filtered out)
      createAndPersistVotingProcedure(drepVoteTxHash, "drep_hash_1", VoterType.DREP_KEY_HASH);

      entityManager.flush();
      entityManager.clear();

      // when
      List<VotingProcedureEntity> results = votingProcedureRepository.findByTxHashInAndVoterType(
          List.of(txHash1, txHash2, drepVoteTxHash),
          VoterType.STAKING_POOL_KEY_HASH
      );

      // then
      assertThat(results).hasSize(2);
      assertThat(results)
          .extracting(VotingProcedureEntity::getTxHash)
          .containsExactlyInAnyOrder(txHash1, txHash2);
      assertThat(results)
          .extracting(VotingProcedureEntity::getVoterType)
          .containsOnly(VoterType.STAKING_POOL_KEY_HASH);
    }

    @Test
    void shouldReturnEmptyListWhenNoSpoVotesExist() {
      // given
      String txHash = "no_spo_votes_tx_" + UUID.randomUUID();
      createAndPersistVotingProcedure(txHash, "drep_hash", VoterType.DREP_KEY_HASH);
      entityManager.flush();
      entityManager.clear();

      // when
      List<VotingProcedureEntity> results = votingProcedureRepository.findByTxHashInAndVoterType(
          List.of(txHash),
          VoterType.STAKING_POOL_KEY_HASH
      );

      // then
      assertThat(results).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenTxHashesNotFound() {
      // when
      List<VotingProcedureEntity> results = votingProcedureRepository.findByTxHashInAndVoterType(
          List.of("non_existent_tx_1", "non_existent_tx_2"),
          VoterType.STAKING_POOL_KEY_HASH
      );

      // then
      assertThat(results).isEmpty();
    }

    @Test
    void shouldReturnMultipleSpoVotesFromSameTransaction() {
      // given - A transaction with multiple SPO votes (multiple pools voting)
      String txHash = "multi_spo_tx_" + UUID.randomUUID();

      createAndPersistVotingProcedure(txHash, "pool_hash_1", VoterType.STAKING_POOL_KEY_HASH, "gov_action_1", 0);
      createAndPersistVotingProcedure(txHash, "pool_hash_2", VoterType.STAKING_POOL_KEY_HASH, "gov_action_1", 0);
      createAndPersistVotingProcedure(txHash, "pool_hash_3", VoterType.STAKING_POOL_KEY_HASH, "gov_action_2", 1);

      entityManager.flush();
      entityManager.clear();

      // when
      List<VotingProcedureEntity> results = votingProcedureRepository.findByTxHashInAndVoterType(
          List.of(txHash),
          VoterType.STAKING_POOL_KEY_HASH
      );

      // then
      assertThat(results).hasSize(3);
      assertThat(results)
          .extracting(VotingProcedureEntity::getTxHash)
          .containsOnly(txHash);
      assertThat(results)
          .extracting(VotingProcedureEntity::getVoterHash)
          .containsExactlyInAnyOrder("pool_hash_1", "pool_hash_2", "pool_hash_3");
    }

    @Test
    void shouldHandleEmptyTxHashList() {
      // when
      List<VotingProcedureEntity> results = votingProcedureRepository.findByTxHashInAndVoterType(
          List.of(),
          VoterType.STAKING_POOL_KEY_HASH
      );

      // then
      assertThat(results).isEmpty();
    }
  }

  @Nested
  class FindByTxHashAndVoterTypeTests {

    @Test
    void shouldReturnSpoVoteForSpecificTransaction() {
      // given
      String txHash = "single_spo_tx_" + UUID.randomUUID();
      VotingProcedureEntity created = createAndPersistVotingProcedure(
          txHash, "pool_hash_123", VoterType.STAKING_POOL_KEY_HASH);
      entityManager.flush();
      entityManager.clear();

      // when
      List<VotingProcedureEntity> results = votingProcedureRepository.findByTxHashAndVoterType(
          txHash,
          VoterType.STAKING_POOL_KEY_HASH
      );

      // then
      assertThat(results).hasSize(1);
      assertThat(results.get(0).getTxHash()).isEqualTo(txHash);
      assertThat(results.get(0).getVoterHash()).isEqualTo("pool_hash_123");
      assertThat(results.get(0).getVoterType()).isEqualTo(VoterType.STAKING_POOL_KEY_HASH);
    }

    @Test
    void shouldReturnEmptyWhenTxHashNotFound() {
      // when
      List<VotingProcedureEntity> results = votingProcedureRepository.findByTxHashAndVoterType(
          "non_existent_tx_hash",
          VoterType.STAKING_POOL_KEY_HASH
      );

      // then
      assertThat(results).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenWrongVoterType() {
      // given
      String txHash = "drep_only_tx_" + UUID.randomUUID();
      createAndPersistVotingProcedure(txHash, "drep_hash", VoterType.DREP_KEY_HASH);
      entityManager.flush();
      entityManager.clear();

      // when
      List<VotingProcedureEntity> results = votingProcedureRepository.findByTxHashAndVoterType(
          txHash,
          VoterType.STAKING_POOL_KEY_HASH // Looking for SPO vote
      );

      // then
      assertThat(results).isEmpty();
    }
  }

  @Nested
  class VoterTypeFilteringTests {

    @Test
    void shouldFilterDifferentVoterTypes() {
      // given
      String txHash = "mixed_voters_tx_" + UUID.randomUUID();

      createAndPersistVotingProcedure(txHash, "pool_1", VoterType.STAKING_POOL_KEY_HASH);
      createAndPersistVotingProcedure(txHash, "drep_1", VoterType.DREP_KEY_HASH);
      createAndPersistVotingProcedure(txHash, "cc_1", VoterType.CONSTITUTIONAL_COMMITTEE_HOT_KEY_HASH);

      entityManager.flush();
      entityManager.clear();

      // when
      List<VotingProcedureEntity> spoResults = votingProcedureRepository.findByTxHashAndVoterType(
          txHash, VoterType.STAKING_POOL_KEY_HASH);
      List<VotingProcedureEntity> drepResults = votingProcedureRepository.findByTxHashAndVoterType(
          txHash, VoterType.DREP_KEY_HASH);
      List<VotingProcedureEntity> ccResults = votingProcedureRepository.findByTxHashAndVoterType(
          txHash, VoterType.CONSTITUTIONAL_COMMITTEE_HOT_KEY_HASH);

      // then
      assertThat(spoResults).hasSize(1);
      assertThat(spoResults.get(0).getVoterHash()).isEqualTo("pool_1");

      assertThat(drepResults).hasSize(1);
      assertThat(drepResults.get(0).getVoterHash()).isEqualTo("drep_1");

      assertThat(ccResults).hasSize(1);
      assertThat(ccResults.get(0).getVoterHash()).isEqualTo("cc_1");
    }
  }

  @Nested
  class DataIntegrityTests {

    @Test
    void shouldPreserveAllFieldsWhenQuerying() {
      // given
      String txHash = "complete_vote_tx_" + UUID.randomUUID();
      VotingProcedureEntity original = createCompleteVotingProcedure(txHash);
      entityManager.persist(original);
      entityManager.flush();
      entityManager.clear();

      // when
      List<VotingProcedureEntity> results = votingProcedureRepository.findByTxHashAndVoterType(
          txHash,
          VoterType.STAKING_POOL_KEY_HASH
      );

      // then
      assertThat(results).hasSize(1);
      VotingProcedureEntity retrieved = results.get(0);

      assertThat(retrieved.getTxHash()).isEqualTo(original.getTxHash());
      assertThat(retrieved.getVoterHash()).isEqualTo(original.getVoterHash());
      assertThat(retrieved.getGovActionTxHash()).isEqualTo(original.getGovActionTxHash());
      assertThat(retrieved.getGovActionIndex()).isEqualTo(original.getGovActionIndex());
      assertThat(retrieved.getVote()).isEqualTo(original.getVote());
      assertThat(retrieved.getAnchorUrl()).isEqualTo(original.getAnchorUrl());
      assertThat(retrieved.getAnchorHash()).isEqualTo(original.getAnchorHash());
      assertThat(retrieved.getEpoch()).isEqualTo(original.getEpoch());
      assertThat(retrieved.getSlot()).isEqualTo(original.getSlot());
      assertThat(retrieved.getBlockNumber()).isEqualTo(original.getBlockNumber());
    }
  }

  // Helper methods

  private VotingProcedureEntity createAndPersistVotingProcedure(
      String txHash, String voterHash, VoterType voterType) {
    return createAndPersistVotingProcedure(txHash, voterHash, voterType, "default_gov_action", 0);
  }

  private VotingProcedureEntity createAndPersistVotingProcedure(
      String txHash, String voterHash, VoterType voterType, String govActionTxHash, int govActionIndex) {

    VotingProcedureEntity entity = new VotingProcedureEntity();
    entity.setId(UUID.randomUUID());
    entity.setTxHash(txHash);
    entity.setVoterHash(voterHash);
    entity.setVoterType(voterType);
    entity.setGovActionTxHash(govActionTxHash);
    entity.setGovActionIndex(govActionIndex);
    entity.setIdx(0);
    entity.setTxIndex(1);
    entity.setVote(org.cardanofoundation.rosetta.api.block.model.entity.Vote.YES);
    entity.setEpoch(1);
    entity.setSlot(1L);
    entity.setBlockNumber(1L);
    entity.setBlockTime(1L);
    entity.setUpdateDateTime(LocalDateTime.now());

    entityManager.persist(entity);
    return entity;
  }

  private VotingProcedureEntity createCompleteVotingProcedure(String txHash) {
    VotingProcedureEntity entity = new VotingProcedureEntity();
    entity.setId(UUID.randomUUID());
    entity.setTxHash(txHash);
    entity.setVoterHash("complete_pool_hash");
    entity.setVoterType(VoterType.STAKING_POOL_KEY_HASH);
    entity.setGovActionTxHash("complete_gov_action");
    entity.setGovActionIndex(5);
    entity.setIdx(0);
    entity.setTxIndex(10);
    entity.setVote(org.cardanofoundation.rosetta.api.block.model.entity.Vote.NO);
    entity.setAnchorUrl("https://complete.example.com/rationale.json");
    entity.setAnchorHash("abcd1234567890abcdef1234567890abcdef1234567890abcdef1234567890ab");
    entity.setEpoch(500);
    entity.setSlot(987654321L);
    entity.setBlockNumber(1234567L);
    entity.setBlockTime(1704067200L);
    entity.setUpdateDateTime(LocalDateTime.now());
    return entity;
  }

}
