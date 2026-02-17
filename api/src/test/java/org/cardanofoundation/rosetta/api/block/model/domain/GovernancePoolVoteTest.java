package org.cardanofoundation.rosetta.api.block.model.domain;

import com.bloxbean.cardano.client.transaction.spec.governance.Anchor;
import com.bloxbean.cardano.client.transaction.spec.governance.Vote;
import com.bloxbean.cardano.client.transaction.spec.governance.Voter;
import com.bloxbean.cardano.client.transaction.spec.governance.actions.GovActionId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.GovVoteParams;
import org.openapitools.client.model.GovVoteRationaleParams;
import org.openapitools.client.model.PoolGovernanceVoteParams;
import org.openapitools.client.model.PublicKey;

import static com.bloxbean.cardano.client.transaction.spec.governance.VoterType.STAKING_POOL_KEY_HASH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openapitools.client.model.CurveType.EDWARDS25519;

/**
 * Unit tests for GovernancePoolVote conversion methods.
 * Tests all conversion logic between Rosetta API models and Cardano client library models.
 */
class GovernancePoolVoteTest {

  @Nested
  class AnchorConversionTests {

    @Test
    void convertFromRosetta_shouldReturnNull_whenAnchorIsNull() {
      // when
      GovVoteRationaleParams result = GovernancePoolVote.convertFromRosetta((Anchor) null);

      // then
      assertThat(result).isNull();
    }

    @Test
    void convertFromRosetta_shouldConvertAnchor_whenAnchorIsValid() {
      // given
      Anchor anchor = Anchor.builder()
          .anchorUrl("https://example.com/rationale.json")
          .anchorDataHash(new byte[] {
              0x12, 0x34, 0x56, 0x78, (byte) 0x90, (byte) 0xab, (byte) 0xcd, (byte) 0xef,
              0x12, 0x34, 0x56, 0x78, (byte) 0x90, (byte) 0xab, (byte) 0xcd, (byte) 0xef,
              0x12, 0x34, 0x56, 0x78, (byte) 0x90, (byte) 0xab, (byte) 0xcd, (byte) 0xef,
              0x12, 0x34, 0x56, 0x78, (byte) 0x90, (byte) 0xab, (byte) 0xcd, (byte) 0xef
          })
          .build();

      // when
      GovVoteRationaleParams result = GovernancePoolVote.convertFromRosetta(anchor);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getUrl()).isEqualTo("https://example.com/rationale.json");
      assertThat(result.getDataHash()).isEqualTo("1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef");
    }

    @Test
    void convertToRosetta_shouldReturnNull_whenGovVoteRationaleParamsIsNull() {
      // when
      Anchor result = GovernancePoolVote.convertToRosetta((GovVoteRationaleParams) null);

      // then
      assertThat(result).isNull();
    }

    @Test
    void convertToRosetta_shouldConvertGovVoteRationaleParams_whenParamsAreValid() {
      // given
      GovVoteRationaleParams params = GovVoteRationaleParams.builder()
          .url("https://example.com/rationale.json")
          .dataHash("1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
          .build();

      // when
      Anchor result = GovernancePoolVote.convertToRosetta(params);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getAnchorUrl()).isEqualTo("https://example.com/rationale.json");
      assertThat(result.getAnchorDataHash()).hasSize(32); // 256 bits = 32 bytes
    }

    @Test
    void shouldSupportRoundTripConversion_anchorToParamsToAnchor() {
      // given
      Anchor originalAnchor = Anchor.builder()
          .anchorUrl("https://pool.example.com/vote.json")
          .anchorDataHash(new byte[]{
              (byte) 0xfe, (byte) 0xdc, (byte) 0xba, (byte) 0x98, 0x76, 0x54, 0x32, 0x10,
              (byte) 0xfe, (byte) 0xdc, (byte) 0xba, (byte) 0x98, 0x76, 0x54, 0x32, 0x10,
              (byte) 0xfe, (byte) 0xdc, (byte) 0xba, (byte) 0x98, 0x76, 0x54, 0x32, 0x10,
              (byte) 0xfe, (byte) 0xdc, (byte) 0xba, (byte) 0x98, 0x76, 0x54, 0x32, 0x10
          })
          .build();

      // when - convert anchor to params and back
      GovVoteRationaleParams params = GovernancePoolVote.convertFromRosetta(originalAnchor);
      Anchor resultAnchor = GovernancePoolVote.convertToRosetta(params);

      // then
      assertThat(resultAnchor).isNotNull();
      assertThat(resultAnchor.getAnchorUrl()).isEqualTo(originalAnchor.getAnchorUrl());
      assertThat(resultAnchor.getAnchorDataHash()).isEqualTo(originalAnchor.getAnchorDataHash());
    }

    @Test
    void shouldHandleEmptyUrl() {
      // given
      Anchor anchor = Anchor.builder()
          .anchorUrl("")
          .anchorDataHash(new byte[32])
          .build();

      // when
      GovVoteRationaleParams result = GovernancePoolVote.convertFromRosetta(anchor);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getUrl()).isEmpty();
    }

    @Test
    void shouldHandleUrlWithSpecialCharacters() {
      // given
      String complexUrl = "https://example.com/path?param=value&other=test#anchor";
      Anchor anchor = Anchor.builder()
          .anchorUrl(complexUrl)
          .anchorDataHash(new byte[32])
          .build();

      // when
      GovVoteRationaleParams result = GovernancePoolVote.convertFromRosetta(anchor);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getUrl()).isEqualTo(complexUrl);
    }
  }

  @Nested
  class VoteConversionTests {

    @Test
    void convertFromRosetta_shouldConvertYesVote() {
      // when
      GovVoteParams result = GovernancePoolVote.convertFromRosetta(Vote.YES);

      // then
      assertThat(result).isEqualTo(GovVoteParams.YES);
    }

    @Test
    void convertFromRosetta_shouldConvertNoVote() {
      // when
      GovVoteParams result = GovernancePoolVote.convertFromRosetta(Vote.NO);

      // then
      assertThat(result).isEqualTo(GovVoteParams.NO);
    }

    @Test
    void convertFromRosetta_shouldConvertAbstainVote() {
      // when
      GovVoteParams result = GovernancePoolVote.convertFromRosetta(Vote.ABSTAIN);

      // then
      assertThat(result).isEqualTo(GovVoteParams.ABSTAIN);
    }

    @Test
    void convertToRosetta_shouldConvertYesVoteParams() {
      // when
      Vote result = GovernancePoolVote.convertToRosetta(GovVoteParams.YES);

      // then
      assertThat(result).isEqualTo(Vote.YES);
    }

    @Test
    void convertToRosetta_shouldConvertNoVoteParams() {
      // when
      Vote result = GovernancePoolVote.convertToRosetta(GovVoteParams.NO);

      // then
      assertThat(result).isEqualTo(Vote.NO);
    }

    @Test
    void convertToRosetta_shouldConvertAbstainVoteParams() {
      // when
      Vote result = GovernancePoolVote.convertToRosetta(GovVoteParams.ABSTAIN);

      // then
      assertThat(result).isEqualTo(Vote.ABSTAIN);
    }

    @Test
    void shouldSupportRoundTripConversion_voteToParamsToVote() {
      // given
      Vote originalVote = Vote.YES;

      // when
      GovVoteParams params = GovernancePoolVote.convertFromRosetta(originalVote);
      Vote resultVote = GovernancePoolVote.convertToRosetta(params);

      // then
      assertThat(resultVote).isEqualTo(originalVote);
    }
  }

  @Nested
  class GovActionIdConversionTests {

    @Test
    void convertGovActionIdToRosetta_shouldParseValidGovActionString() {
      // given - Format: 64-char hex txId + 2-char hex index (05 = index 5)
      String govActionString = "abcd1234567890abcdef1234567890abcdef1234567890abcdef1234567890ab05";

      // when
      GovActionId result = GovernancePoolVote.convertGovActionIdToRosetta(govActionString);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getTransactionId()).isEqualTo("abcd1234567890abcdef1234567890abcdef1234567890abcdef1234567890ab");
      assertThat(result.getGovActionIndex()).isEqualTo(5);
    }

    @Test
    void convertFromRosetta_shouldFormatGovActionId() {
      // given
      GovActionId govActionId = new GovActionId(
          "abcd1234567890abcdef1234567890abcdef1234567890abcdef1234567890ab",
          5
      );

      // when
      String result = GovernancePoolVote.convertFromRosetta(govActionId);

      // then - Format: 64-char hex txId + 2-char hex index (05 = index 5)
      assertThat(result).isEqualTo("abcd1234567890abcdef1234567890abcdef1234567890abcdef1234567890ab05");
    }

    @Test
    void shouldSupportRoundTripConversion_govActionIdToStringToGovActionId() {
      // given
      GovActionId originalGovActionId = new GovActionId(
          "fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210",
          42 // 42 in decimal = 2a in hex
      );

      // when
      String govActionString = GovernancePoolVote.convertFromRosetta(originalGovActionId);
      GovActionId result = GovernancePoolVote.convertGovActionIdToRosetta(govActionString);

      // then
      assertThat(result.getTransactionId()).isEqualTo(originalGovActionId.getTransactionId());
      assertThat(result.getGovActionIndex()).isEqualTo(originalGovActionId.getGovActionIndex());
    }

    @Test
    void convertGovActionIdToRosetta_shouldHandleZeroIndex() {
      // given - Format: 64-char hex txId + 2-char hex index (00 = index 0)
      String govActionString = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef00";

      // when
      GovActionId result = GovernancePoolVote.convertGovActionIdToRosetta(govActionString);

      // then
      assertThat(result.getGovActionIndex()).isEqualTo(0);
    }

    @Test
    void convertGovActionIdToRosetta_shouldHandleMaxIndex() {
      // given - Format: 64-char hex txId + 2-char hex index (63 = 99 decimal, max allowed)
      String govActionString = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef63";

      // when
      GovActionId result = GovernancePoolVote.convertGovActionIdToRosetta(govActionString);

      // then
      assertThat(result.getGovActionIndex()).isEqualTo(99);
    }

    @Test
    void convertFromRosetta_shouldFormatZeroIndex() {
      // given
      GovActionId govActionId = new GovActionId(
          "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",
          0
      );

      // when
      String result = GovernancePoolVote.convertFromRosetta(govActionId);

      // then - 0 should be formatted as "00"
      assertThat(result).isEqualTo("1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef00");
    }

    @Test
    void convertFromRosetta_shouldFormatMaxIndex() {
      // given
      GovActionId govActionId = new GovActionId(
          "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",
          99
      );

      // when
      String result = GovernancePoolVote.convertFromRosetta(govActionId);

      // then - 99 should be formatted as "63" in hex
      assertThat(result).isEqualTo("1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef63");
    }
  }

  @Nested
  class PoolCredentialConversionTests {

    @Test
    void convertFromRosetta_shouldCreateVoterFromPoolCredential() {
      // given - Ed25519 pool cold vKey hash (56 hex chars = 28 bytes)
      PublicKey poolCredential = new PublicKey(
          "abcdef1234567890abcdef1234567890abcdef1234567890abcdef12",
          EDWARDS25519
      );

      // when
      Voter result = GovernancePoolVote.convertFromRosetta(poolCredential);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getType()).isEqualTo(STAKING_POOL_KEY_HASH);
      assertThat(result.getCredential()).isNotNull();
      assertThat(result.getCredential().getBytes()).hasSize(28); // Blake2b-224 produces 28 bytes
    }

    @Test
    void convertToRosetta_shouldCreatePublicKeyFromPoolCredentialHex() {
      // given
      String poolCredentialHex = "abcdef1234567890abcdef1234567890abcdef1234567890abcdef12";

      // when
      PublicKey result = GovernancePoolVote.convertToRosetta(poolCredentialHex);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getHexBytes()).isEqualTo(poolCredentialHex);
      assertThat(result.getCurveType()).isEqualTo(EDWARDS25519);
    }

    @Test
    void shouldSupportRoundTripConversion_poolCredentialHexToPublicKeyToHex() {
      // given
      String originalHex = "fedcba9876543210fedcba9876543210fedcba9876543210fedcba98";

      // when
      PublicKey publicKey = GovernancePoolVote.convertToRosetta(originalHex);
      String resultHex = publicKey.getHexBytes();

      // then
      assertThat(resultHex).isEqualTo(originalHex);
    }
  }

  @Nested
  class FullConversionTests {

    @Test
    void convertToRosetta_shouldConvertCompletePoolGovernanceVoteParams() {
      // given - Format: 64-char hex txId + 2-char hex index (05 = index 5)
      PoolGovernanceVoteParams voteParams = PoolGovernanceVoteParams.builder()
          .governanceActionHash("abcd1234567890abcdef1234567890abcdef1234567890abcdef1234567890ab05")
          .poolCredential(new PublicKey(
              "fedcba9876543210fedcba9876543210fedcba9876543210fedcba98",
              EDWARDS25519
          ))
          .vote(GovVoteParams.YES)
          .voteRationale(GovVoteRationaleParams.builder()
              .url("https://pool.example.com/vote.json")
              .dataHash("1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
              .build())
          .build();

      // when
      GovernancePoolVote result = GovernancePoolVote.convertToRosetta(voteParams);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getGovActionId()).isNotNull();
      assertThat(result.getGovActionId().getTransactionId())
          .isEqualTo("abcd1234567890abcdef1234567890abcdef1234567890abcdef1234567890ab");
      assertThat(result.getGovActionId().getGovActionIndex()).isEqualTo(5);
      assertThat(result.getPoolCredentialHex()).isEqualTo("fedcba9876543210fedcba9876543210fedcba9876543210fedcba98");
      assertThat(result.getVote()).isEqualTo(Vote.YES);
      assertThat(result.getVoter()).isNotNull();
      assertThat(result.getVoter().getType()).isEqualTo(STAKING_POOL_KEY_HASH);
      assertThat(result.getVoteRationale()).isNotNull();
      assertThat(result.getVoteRationale().getAnchorUrl()).isEqualTo("https://pool.example.com/vote.json");
    }

    @Test
    void convertToRosetta_shouldHandleNullVoteRationale() {
      // given - Format: 64-char hex txId + 2-char hex index (05 = index 5)
      PoolGovernanceVoteParams voteParams = PoolGovernanceVoteParams.builder()
          .governanceActionHash("abcd1234567890abcdef1234567890abcdef1234567890abcdef1234567890ab05")
          .poolCredential(new PublicKey(
              "fedcba9876543210fedcba9876543210fedcba9876543210fedcba98",
              EDWARDS25519
          ))
          .vote(GovVoteParams.NO)
          .voteRationale(null) // No rationale provided
          .build();

      // when
      GovernancePoolVote result = GovernancePoolVote.convertToRosetta(voteParams);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getVoteRationale()).isNull();
      assertThat(result.getVote()).isEqualTo(Vote.NO);
    }

    @Test
    void convertToRosetta_shouldConvertMinimalPoolGovernanceVoteParams() {
      // given - Format: 64-char hex txId + 2-char hex index (00 = index 0)
      PoolGovernanceVoteParams voteParams = PoolGovernanceVoteParams.builder()
          .governanceActionHash("1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef00")
          .poolCredential(new PublicKey(
              "1234567890abcdef1234567890abcdef1234567890abcdef12345678",
              EDWARDS25519
          ))
          .vote(GovVoteParams.ABSTAIN)
          .build();

      // when
      GovernancePoolVote result = GovernancePoolVote.convertToRosetta(voteParams);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getGovActionId().getGovActionIndex()).isEqualTo(0);
      assertThat(result.getVote()).isEqualTo(Vote.ABSTAIN);
      assertThat(result.getVoteRationale()).isNull();
    }
  }

  @Nested
  class EdgeCaseTests {

    @Test
    void shouldHandleVeryLongUrls() {
      // given
      String longUrl = "https://example.com/" + "a".repeat(1000) + "/rationale.json";
      Anchor anchor = Anchor.builder()
          .anchorUrl(longUrl)
          .anchorDataHash(new byte[32])
          .build();

      // when
      GovVoteRationaleParams result = GovernancePoolVote.convertFromRosetta(anchor);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getUrl()).isEqualTo(longUrl);
    }

    @Test
    void shouldHandleMaximumDataHash() {
      // given - all bytes set to 0xFF
      byte[] maxHash = new byte[32];
      for (int i = 0; i < 32; i++) {
        maxHash[i] = (byte) 0xFF;
      }
      Anchor anchor = Anchor.builder()
          .anchorUrl("https://example.com/vote.json")
          .anchorDataHash(maxHash)
          .build();

      // when
      GovVoteRationaleParams result = GovernancePoolVote.convertFromRosetta(anchor);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getDataHash()).isEqualTo("f".repeat(64));
    }

    @Test
    void shouldHandleMinimumDataHash() {
      // given - all bytes set to 0x00
      byte[] minHash = new byte[32];
      Anchor anchor = Anchor.builder()
          .anchorUrl("https://example.com/vote.json")
          .anchorDataHash(minHash)
          .build();

      // when
      GovVoteRationaleParams result = GovernancePoolVote.convertFromRosetta(anchor);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getDataHash()).isEqualTo("0".repeat(64));
    }
  }
}
