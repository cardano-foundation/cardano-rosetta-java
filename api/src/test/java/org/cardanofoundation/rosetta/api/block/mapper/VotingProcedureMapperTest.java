package org.cardanofoundation.rosetta.api.block.mapper;

import com.bloxbean.cardano.client.transaction.spec.governance.Vote;
import com.bloxbean.cardano.client.transaction.spec.governance.VoterType;
import com.bloxbean.cardano.client.transaction.spec.governance.actions.GovActionId;
import org.cardanofoundation.rosetta.api.block.model.domain.GovernancePoolVote;
import org.cardanofoundation.rosetta.api.block.model.entity.VotingProcedureEntity;
import org.cardanofoundation.rosetta.api.common.mapper.TokenRegistryMapperImpl;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for VotingProcedure mapping logic in TransactionMapper.
 * Tests the mapVotingProcedureEntityToGovernancePoolVote method which converts
 * database entities to domain models for SPO voting.
 */
@ExtendWith(MockitoExtension.class)
class VotingProcedureMapperTest {

  @Mock
  private ProtocolParamService protocolParamService;
  private TransactionMapper transactionMapper;

  @BeforeEach
  void setUp() {
    // Create real instances with dependencies
    DataMapper dataMapper = new DataMapper(new TokenRegistryMapperImpl());
    TransactionMapperUtils transactionMapperUtils = new TransactionMapperUtils(
        protocolParamService,
        dataMapper
    );

    transactionMapper = new TransactionMapperImpl(transactionMapperUtils);
  }

  @Nested
  class BasicMappingTests {

    @Test
    void shouldMapCompleteVotingProcedureEntityWithAllFields() {
      // given
      VotingProcedureEntity entity = createCompleteVotingProcedureEntity();

      // when
      GovernancePoolVote result = transactionMapper.mapVotingProcedureEntityToGovernancePoolVote(entity);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getPoolCredentialHex()).isEqualTo("abcd1234567890abcdef");
      assertThat(result.getVote()).isEqualTo(Vote.YES);
      assertThat(result.getGovActionId()).isNotNull();
      assertThat(result.getGovActionId().getTransactionId()).isEqualTo("gov_action_tx_hash_123");
      assertThat(result.getGovActionId().getGovActionIndex()).isEqualTo(5);
      assertThat(result.getVoter()).isNotNull();
      assertThat(result.getVoter().getType()).isEqualTo(VoterType.STAKING_POOL_KEY_HASH);
      assertThat(result.getVoteRationale()).isNotNull();
      assertThat(result.getVoteRationale().getAnchorUrl()).isEqualTo("https://pool.example.com/vote-rationale.json");
    }

    @Test
    void shouldMapMinimalVotingProcedureEntityWithoutOptionalFields() {
      // given
      VotingProcedureEntity entity = createMinimalVotingProcedureEntity();

      // when
      GovernancePoolVote result = transactionMapper.mapVotingProcedureEntityToGovernancePoolVote(entity);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getPoolCredentialHex()).isEqualTo("abcdef1234567890abcdef1234567890abcdef1234567890abcdef12");
      assertThat(result.getVote()).isEqualTo(Vote.YES); // Vote is required
      assertThat(result.getGovActionId()).isNotNull();
      assertThat(result.getVoteRationale()).isNull(); // No anchor provided
    }

    @Test
    void shouldHandleNullEntity() {
      // when
      GovernancePoolVote result = transactionMapper.mapVotingProcedureEntityToGovernancePoolVote(null);

      // then
      assertThat(result).isNull();
    }

    @Test
    void shouldMapVoterHashToPoolCredentialHex() {
      // given
      VotingProcedureEntity entity = createMinimalVotingProcedureEntity();
      String expectedHash = "fedcba9876543210fedcba9876543210fedcba9876543210fedcba98";
      entity.setVoterHash(expectedHash);

      // when
      GovernancePoolVote result = transactionMapper.mapVotingProcedureEntityToGovernancePoolVote(entity);

      // then
      assertThat(result.getPoolCredentialHex()).isEqualTo(expectedHash);
    }
  }

  @Nested
  class VoteConversionTests {

    @Test
    void shouldConvertYesVote() {
      // given
      VotingProcedureEntity entity = createMinimalVotingProcedureEntity();
      entity.setVote(org.cardanofoundation.rosetta.api.block.model.entity.Vote.YES);

      // when
      GovernancePoolVote result = transactionMapper.mapVotingProcedureEntityToGovernancePoolVote(entity);

      // then
      assertThat(result.getVote()).isEqualTo(Vote.YES);
    }

    @Test
    void shouldConvertNoVote() {
      // given
      VotingProcedureEntity entity = createMinimalVotingProcedureEntity();
      entity.setVote(org.cardanofoundation.rosetta.api.block.model.entity.Vote.NO);

      // when
      GovernancePoolVote result = transactionMapper.mapVotingProcedureEntityToGovernancePoolVote(entity);

      // then
      assertThat(result.getVote()).isEqualTo(Vote.NO);
    }

    @Test
    void shouldConvertAbstainVote() {
      // given
      VotingProcedureEntity entity = createMinimalVotingProcedureEntity();
      entity.setVote(org.cardanofoundation.rosetta.api.block.model.entity.Vote.ABSTAIN);

      // when
      GovernancePoolVote result = transactionMapper.mapVotingProcedureEntityToGovernancePoolVote(entity);

      // then
      assertThat(result.getVote()).isEqualTo(Vote.ABSTAIN);
    }
  }

  @Nested
  class GovActionIdMappingTests {

    @Test
    void shouldMapGovActionTxHashAndIndex() {
      // given
      VotingProcedureEntity entity = createMinimalVotingProcedureEntity();
      entity.setGovActionTxHash("action_tx_hash_999");
      entity.setGovActionIndex(42);

      // when
      GovernancePoolVote result = transactionMapper.mapVotingProcedureEntityToGovernancePoolVote(entity);

      // then
      assertThat(result.getGovActionId()).isNotNull();
      assertThat(result.getGovActionId().getTransactionId()).isEqualTo("action_tx_hash_999");
      assertThat(result.getGovActionId().getGovActionIndex()).isEqualTo(42);
    }

    @Test
    void shouldHandleZeroGovActionIndex() {
      // given
      VotingProcedureEntity entity = createMinimalVotingProcedureEntity();
      entity.setGovActionIndex(0);

      // when
      GovernancePoolVote result = transactionMapper.mapVotingProcedureEntityToGovernancePoolVote(entity);

      // then
      assertThat(result.getGovActionId().getGovActionIndex()).isEqualTo(0);
    }

    @Test
    void shouldHandleLargeGovActionIndex() {
      // given
      VotingProcedureEntity entity = createMinimalVotingProcedureEntity();
      entity.setGovActionIndex(Integer.MAX_VALUE);

      // when
      GovernancePoolVote result = transactionMapper.mapVotingProcedureEntityToGovernancePoolVote(entity);

      // then
      assertThat(result.getGovActionId().getGovActionIndex()).isEqualTo(Integer.MAX_VALUE);
    }
  }

  @Nested
  class VoteRationaleMappingTests {

    @Test
    void shouldMapAnchorWhenBothUrlAndHashPresent() {
      // given
      VotingProcedureEntity entity = createMinimalVotingProcedureEntity();
      entity.setAnchorUrl("https://example.com/rationale.json");
      entity.setAnchorHash("1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef");

      // when
      GovernancePoolVote result = transactionMapper.mapVotingProcedureEntityToGovernancePoolVote(entity);

      // then
      assertThat(result.getVoteRationale()).isNotNull();
      assertThat(result.getVoteRationale().getAnchorUrl()).isEqualTo("https://example.com/rationale.json");
      assertThat(result.getVoteRationale().getAnchorDataHash()).hasSize(32); // Decoded from hex
    }

    @Test
    void shouldNotMapAnchorWhenOnlyUrlPresent() {
      // given
      VotingProcedureEntity entity = createMinimalVotingProcedureEntity();
      entity.setAnchorUrl("https://example.com/rationale.json");
      entity.setAnchorHash(null);

      // when
      GovernancePoolVote result = transactionMapper.mapVotingProcedureEntityToGovernancePoolVote(entity);

      // then
      assertThat(result.getVoteRationale()).isNull();
    }

    @Test
    void shouldNotMapAnchorWhenOnlyHashPresent() {
      // given
      VotingProcedureEntity entity = createMinimalVotingProcedureEntity();
      entity.setAnchorUrl(null);
      entity.setAnchorHash("1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef");

      // when
      GovernancePoolVote result = transactionMapper.mapVotingProcedureEntityToGovernancePoolVote(entity);

      // then
      assertThat(result.getVoteRationale()).isNull();
    }

    @Test
    void shouldNotMapAnchorWhenBothNull() {
      // given
      VotingProcedureEntity entity = createMinimalVotingProcedureEntity();
      entity.setAnchorUrl(null);
      entity.setAnchorHash(null);

      // when
      GovernancePoolVote result = transactionMapper.mapVotingProcedureEntityToGovernancePoolVote(entity);

      // then
      assertThat(result.getVoteRationale()).isNull();
    }
  }

  @Nested
  class VoterMappingTests {

    @Test
    void shouldCreateVoterWithStakingPoolKeyHashType() {
      // given
      VotingProcedureEntity entity = createMinimalVotingProcedureEntity();

      // when
      GovernancePoolVote result = transactionMapper.mapVotingProcedureEntityToGovernancePoolVote(entity);

      // then
      assertThat(result.getVoter()).isNotNull();
      assertThat(result.getVoter().getType()).isEqualTo(VoterType.STAKING_POOL_KEY_HASH);
    }

    @Test
    void shouldCreateCredentialFromVoterHash() {
      // given
      VotingProcedureEntity entity = createMinimalVotingProcedureEntity();
      String voterHash = "fedcba9876543210fedcba9876543210fedcba9876543210fedcba98";

      entity.setVoterHash(voterHash);

      // when
      GovernancePoolVote result = transactionMapper.mapVotingProcedureEntityToGovernancePoolVote(entity);

      // then
      assertThat(result.getVoter()).isNotNull();
      assertThat(result.getVoter().getCredential()).isNotNull();
      // Credential hash should be derived from voter hash
      assertThat(result.getVoter().getCredential().getBytes()).hasSize(28); // 224 bits = 28 bytes
    }
  }

  @Nested
  class EdgeCaseTests {

    @Test
    void shouldHandleEmptyStrings() {
      // given
      VotingProcedureEntity entity = createMinimalVotingProcedureEntity();
      entity.setAnchorUrl("");

      // when
      GovernancePoolVote result = transactionMapper.mapVotingProcedureEntityToGovernancePoolVote(entity);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getVote()).isNotNull(); // Vote is required
      assertThat(result.getVoteRationale()).isNull(); // Empty URL is treated as null
    }

    @Test
    void shouldHandleSpecialCharactersInUrls() {
      // given
      VotingProcedureEntity entity = createMinimalVotingProcedureEntity();
      String complexUrl = "https://example.com/path?param=value&other=test#anchor";
      entity.setAnchorUrl(complexUrl);
      entity.setAnchorHash("1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef");

      // when
      GovernancePoolVote result = transactionMapper.mapVotingProcedureEntityToGovernancePoolVote(entity);

      // then
      assertThat(result.getVoteRationale()).isNotNull();
      assertThat(result.getVoteRationale().getAnchorUrl()).isEqualTo(complexUrl);
    }

    @Test
    void shouldHandleVeryLongTxHashes() {
      // given
      VotingProcedureEntity entity = createMinimalVotingProcedureEntity();
      String longHash = "a".repeat(64); // Standard Cardano tx hash length
      entity.setTxHash(longHash);
      entity.setGovActionTxHash(longHash);

      // when
      GovernancePoolVote result = transactionMapper.mapVotingProcedureEntityToGovernancePoolVote(entity);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getGovActionId().getTransactionId()).isEqualTo(longHash);
    }
  }

  // Helper methods

  private VotingProcedureEntity createCompleteVotingProcedureEntity() {
    VotingProcedureEntity entity = new VotingProcedureEntity();
    entity.setId(UUID.randomUUID());
    entity.setTxHash("tx_hash_123");
    entity.setVoterHash("abcd1234567890abcdef");
    entity.setVoterType(org.cardanofoundation.rosetta.api.block.model.entity.VoterType.STAKING_POOL_KEY_HASH);
    entity.setGovActionTxHash("gov_action_tx_hash_123");
    entity.setGovActionIndex(5);
    entity.setIdx(0);
    entity.setTxIndex(10);
    entity.setVote(org.cardanofoundation.rosetta.api.block.model.entity.Vote.YES);
    entity.setAnchorUrl("https://pool.example.com/vote-rationale.json");
    entity.setAnchorHash("fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210");
    entity.setEpoch(450);
    entity.setSlot(123456789L);
    entity.setBlockNumber(9876543L);
    entity.setBlockTime(1704067200L);
    entity.setUpdateDateTime(LocalDateTime.now());
    return entity;
  }

  private VotingProcedureEntity createMinimalVotingProcedureEntity() {
    VotingProcedureEntity entity = new VotingProcedureEntity();
    entity.setId(UUID.randomUUID());
    entity.setTxHash("minimal_tx_hash");
    entity.setVoterHash("abcdef1234567890abcdef1234567890abcdef1234567890abcdef12"); // Valid 56-char hex
    entity.setVoterType(org.cardanofoundation.rosetta.api.block.model.entity.VoterType.STAKING_POOL_KEY_HASH);
    entity.setGovActionTxHash("minimal_gov_action");
    entity.setGovActionIndex(1);
    entity.setIdx(0);
    entity.setTxIndex(5);
    entity.setVote(org.cardanofoundation.rosetta.api.block.model.entity.Vote.YES); // Vote is required
    entity.setEpoch(1);
    entity.setSlot(1L);
    entity.setBlockNumber(1L);
    entity.setBlockTime(1L);
    entity.setUpdateDateTime(LocalDateTime.now());
    // Optional fields (anchorUrl, anchorHash) left as null
    return entity;
  }

}
