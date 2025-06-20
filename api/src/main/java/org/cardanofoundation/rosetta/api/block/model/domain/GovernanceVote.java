package org.cardanofoundation.rosetta.api.block.model.domain;

import com.bloxbean.cardano.client.address.Credential;
import com.bloxbean.cardano.client.transaction.spec.governance.Anchor;
import com.bloxbean.cardano.client.transaction.spec.governance.Vote;
import com.bloxbean.cardano.client.transaction.spec.governance.Voter;
import com.bloxbean.cardano.client.transaction.spec.governance.VoterType;
import com.bloxbean.cardano.client.transaction.spec.governance.actions.GovActionId;
import com.bloxbean.cardano.client.util.HexUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.openapitools.client.model.*;

import javax.annotation.Nullable;
import java.util.Optional;

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

    public static GovernanceVote convertToRosetta(PoolGovernanceVoteParams voteParams) {
        GovernanceVoteBuilder governanceVoteBuilder = GovernanceVote.builder()
                .govActionId(convertToRosetta(voteParams.getActionId()))
                .voter(convertToRosetta(voteParams.getVoter()))
                .vote(convertToRosetta(voteParams.getVote()));

        Optional.ofNullable(voteParams.getAnchor()).ifPresent(govAnchorParams -> {
            governanceVoteBuilder.anchor(convertToRosetta(govAnchorParams));
        });

        return governanceVoteBuilder.build();
    }

    public static PoolGovernanceVoteParams convertFromRosetta(GovernanceVote governanceVote) {
        PoolGovernanceVoteParams.PoolGovernanceVoteParamsBuilder builder = PoolGovernanceVoteParams.builder();

        builder.actionId(convertFromRosetta(governanceVote.getGovActionId()));
        builder.voter(convertFromRosetta(governanceVote.getVoter()));
        builder.vote(convertFromRosetta(governanceVote.getVote()));

        Optional.ofNullable(governanceVote.getAnchor()).ifPresent(govAnchorParams -> {
            builder.anchor(convertFromRosetta(govAnchorParams));
        });

        return builder.build();
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
            case YES -> GovVoteParams.YES;
            case NO -> GovVoteParams.NO;
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
            case GovVoteParams.YES -> Vote.YES;
            case GovVoteParams.NO -> Vote.NO;
            case GovVoteParams.ABSTAIN -> Vote.ABSTAIN;
        };
    }

}
