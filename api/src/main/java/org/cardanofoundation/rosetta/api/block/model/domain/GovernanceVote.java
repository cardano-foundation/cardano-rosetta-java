package org.cardanofoundation.rosetta.api.block.model.domain;

import java.util.Optional;
import javax.annotation.Nullable;

import lombok.*;

import com.bloxbean.cardano.client.address.Credential;
import com.bloxbean.cardano.client.address.CredentialType;
import com.bloxbean.cardano.client.transaction.spec.governance.Anchor;
import com.bloxbean.cardano.client.transaction.spec.governance.Vote;
import com.bloxbean.cardano.client.transaction.spec.governance.Voter;
import com.bloxbean.cardano.client.transaction.spec.governance.actions.GovActionId;
import com.bloxbean.cardano.client.util.HexUtil;
import org.openapitools.client.model.*;

import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;

import static com.bloxbean.cardano.client.transaction.spec.governance.VoterType.STAKING_POOL_KEY_HASH;
import static com.bloxbean.cardano.client.util.HexUtil.encodeHexString;
import static org.openapitools.client.model.CurveType.EDWARDS25519;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GovernanceVote {

    private GovActionId govActionId;
    private Vote vote;
    private Voter voter;

    @Nullable
    private Anchor voteRationale;

    public static GovernanceVote convertToRosetta(PoolGovernanceVoteParams voteParams) {
        GovernanceVoteBuilder governanceVoteBuilder = GovernanceVote.builder()
                .govActionId(convertToRosetta(voteParams.getGovernanceAction()))
                .voter(convertFromRosetta(voteParams.getPoolCredential())) // for now only support pool credential
                .vote(convertToRosetta(voteParams.getVote()));

        Optional.ofNullable(voteParams.getVoteRationale()).ifPresent(govVoteRationaleParams -> {
            governanceVoteBuilder.voteRationale(convertToRosetta(govVoteRationaleParams));
        });

        return governanceVoteBuilder.build();
    }

    public static PoolGovernanceVoteParams convertFromRosetta(GovernanceVote governanceVote) {
        PoolGovernanceVoteParams.PoolGovernanceVoteParamsBuilder builder = PoolGovernanceVoteParams.builder();

        builder.governanceAction(convertFromRosetta(governanceVote.getGovActionId()));
        builder.poolCredential(convertToRosetta(governanceVote.getVoter()));
        builder.vote(convertFromRosetta(governanceVote.getVote()));

        Optional.ofNullable(governanceVote.getVoteRationale()).ifPresent(anchor -> {
            builder.voteRationale(convertFromRosetta(anchor));
        });

        return builder.build();
    }

    public static GovVoteRationaleParams convertFromRosetta(Anchor anchor) {
        return GovVoteRationaleParams.builder()
                .url(anchor.getAnchorUrl())
                .dataHash(encodeHexString(anchor.getAnchorDataHash()))
                .build();
    }

    public static Anchor convertToRosetta(GovVoteRationaleParams govAnchorParams) {
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

    public static GovActionId convertToRosetta(GovActionParams govActionIdParams) {
        return GovActionId.builder()
                .govActionIndex(govActionIdParams.getIndex())
                .transactionId(govActionIdParams.getTxId())
                .build();
    }

    public static GovActionParams convertFromRosetta(GovActionId govActionId) {
        return GovActionParams.builder()
                .index(govActionId.getGovActionIndex())
                .txId(govActionId.getTransactionId())
                .build();
    }

    public static Voter convertFromRosetta(PublicKey poolCredential) {
        Credential credential = Credential.fromKey(poolCredential.getHexBytes());

        return new Voter(STAKING_POOL_KEY_HASH, credential);
    }

    public static PublicKey convertToRosetta(Voter voter) {
        if (voter.getType() != STAKING_POOL_KEY_HASH) {
            throw ExceptionFactory.governanceOnlyPoolVotingPossible();
        }

        if (voter.getCredential().getType() != CredentialType.Key) {
            throw ExceptionFactory.governanceKeyHashOnlySupported();
        }

        byte[] credentialBytes = voter.getCredential().getBytes();

        return new PublicKey(encodeHexString(credentialBytes), EDWARDS25519);
    }

    private static String convertFromRosetta(Credential credential) {
        return encodeHexString(credential.getBytes());
    }

    public static Vote convertToRosetta(GovVoteParams voteParams) {
        return switch (voteParams) {
            case GovVoteParams.YES -> Vote.YES;
            case GovVoteParams.NO -> Vote.NO;
            case GovVoteParams.ABSTAIN -> Vote.ABSTAIN;
        };
    }

}
