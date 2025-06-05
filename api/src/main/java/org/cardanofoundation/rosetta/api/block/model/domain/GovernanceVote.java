package org.cardanofoundation.rosetta.api.block.model.domain;

import java.util.Optional;
import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.bloxbean.cardano.client.address.Credential;
import com.bloxbean.cardano.client.transaction.spec.governance.Anchor;
import com.bloxbean.cardano.client.transaction.spec.governance.Vote;
import com.bloxbean.cardano.client.transaction.spec.governance.Voter;
import com.bloxbean.cardano.client.transaction.spec.governance.VoterType;
import com.bloxbean.cardano.client.transaction.spec.governance.actions.GovActionId;
import com.bloxbean.cardano.client.util.HexUtil;
import io.vavr.Tuple4;
import org.openapitools.client.model.*;

import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceVote {

    private GovActionId govActionId;
    private Vote vote;
    private Voter voter;

    @Nullable
    private Anchor anchor;

    public static GovernanceVote convertToRosetta(Tuple4<GovActionIdParams,
                                                  GovVoterParams,
                                                  GovVoteParams,
                                                  Optional<GovAnchorParams>> params) {
        GovernanceVoteBuilder governanceVoteBuilder = GovernanceVote.builder()
                .govActionId(convertToRosetta(params._1()))
                .voter(convertToRosetta(params._2()))
                .vote(convertToRosetta(params._3));

        if (params._4().isPresent()) {
            governanceVoteBuilder.anchor(convertToRosetta(params._4().orElseThrow()));
        }

        return governanceVoteBuilder.build();
    }

    public static Tuple4<
            GovActionIdParams,
            GovVoterParams,
            GovVoteParams,
            GovAnchorParams>
                convertFromRosetta(GovernanceVote governanceVote) {

        var govAnchorParams = governanceVote.getAnchor() != null ? convertFromRosetta(governanceVote.getAnchor()) : null;

        return new Tuple4<>(
                convertFromRosetta(governanceVote.getGovActionId()),
                convertFromRosetta(governanceVote.getVoter()),
                convertFromRosetta(governanceVote.getVote()),
                govAnchorParams
        );
    }

    public static GovAnchorParams convertFromRosetta(Anchor anchor) {
        return GovAnchorParams.builder()
                .url(anchor.getAnchorUrl())
                .dataHash(HexUtil.encodeHexString(anchor.getAnchorDataHash()))
                .build();
    }

    public static Anchor convertToRosetta(GovAnchorParams govAnchorParams) {
        return Anchor.builder()
                .anchorUrl(govAnchorParams.getUrl())
                .anchorDataHash(HexUtil.decodeHexString(govAnchorParams.getDataHash()))
                .build();
    }

    public static GovVoteParams convertFromRosetta(Vote vote) {
        return switch (vote) {
            case YES -> GovVoteParams.TRUE;
            case NO -> GovVoteParams.FALSE;
            case ABSTAIN -> GovVoteParams.ABSTAIN;
        };
    }

    public static GovActionId convertToRosetta(GovActionIdParams govActionIdParams) {
        return GovActionId.builder()
                .govActionIndex(govActionIdParams.getIndex())
                .transactionId(govActionIdParams.getTxId())
                .build();
    }

    public static GovActionIdParams convertFromRosetta(GovActionId govActionId) {
        return GovActionIdParams.builder()
                .index(govActionId.getGovActionIndex())
                .txId(govActionId.getTransactionId())
                .build();
    }

    public static Voter convertToRosetta(GovVoterParams voterParams) {
        String voterId = voterParams.getId();

        VoterType voteType = switch (voterParams.getType()) {
            case GovVoterTypeParams.DREP_KEY_HASH -> VoterType.DREP_KEY_HASH;
            case GovVoterTypeParams.STAKING_POOL_KEY_HASH -> VoterType.STAKING_POOL_KEY_HASH;
            case GovVoterTypeParams.DREP_SCRIPT_HASH -> VoterType.DREP_SCRIPT_HASH;
        };

        return new Voter(voteType, credentialToRosetta(voterId, voteType));
    }

    public static GovVoterParams convertFromRosetta(Voter voter) {
        GovVoterTypeParams govVoterTypeParams = switch (voter.getType()) {
            case VoterType.DREP_KEY_HASH -> GovVoterTypeParams.DREP_KEY_HASH;
            case VoterType.STAKING_POOL_KEY_HASH -> GovVoterTypeParams.STAKING_POOL_KEY_HASH;
            case VoterType.DREP_SCRIPT_HASH -> GovVoterTypeParams.DREP_SCRIPT_HASH;
            default -> throw ExceptionFactory.unsupportedVoterType();
        };

        return GovVoterParams.builder()
                .id(convertFromRosetta(voter.getCredential()))
                .type(govVoterTypeParams)
                .build();
    }

    private static Credential credentialToRosetta(String voterId, VoterType voterType) {
        return switch (voterType) {
            case DREP_KEY_HASH, STAKING_POOL_KEY_HASH -> Credential.fromKey(voterId);
            case DREP_SCRIPT_HASH -> Credential.fromScript(voterId);
            default -> throw ExceptionFactory.unsupportedVoterType();
        };
    }

    private static String convertFromRosetta(Credential credential) {
        return HexUtil.encodeHexString(credential.getBytes());
    }

    public static Vote convertToRosetta(GovVoteParams voteParams) {
        return switch (voteParams) {
            case GovVoteParams.TRUE -> Vote.YES;
            case GovVoteParams.FALSE -> Vote.NO;
            case GovVoteParams.ABSTAIN -> Vote.ABSTAIN;
        };
    }

}
