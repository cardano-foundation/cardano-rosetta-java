package org.cardanofoundation.rosetta.api.block.model.domain;

import com.bloxbean.cardano.client.address.Credential;
import com.bloxbean.cardano.client.transaction.spec.governance.Anchor;
import com.bloxbean.cardano.client.transaction.spec.governance.Vote;
import com.bloxbean.cardano.client.transaction.spec.governance.Voter;
import com.bloxbean.cardano.client.transaction.spec.governance.actions.GovActionId;
import lombok.*;
import org.openapitools.client.model.*;

import javax.annotation.Nullable;
import java.util.Optional;

import static com.bloxbean.cardano.client.crypto.Blake2bUtil.blake2bHash224;
import static com.bloxbean.cardano.client.transaction.spec.governance.VoterType.STAKING_POOL_KEY_HASH;
import static com.bloxbean.cardano.client.util.HexUtil.decodeHexString;
import static com.bloxbean.cardano.client.util.HexUtil.encodeHexString;
import static org.openapitools.client.model.CurveType.EDWARDS25519;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GovernancePoolVote {

    private GovActionId govActionId;
    private String poolCredentialHex;
    private Vote vote;
    private Voter voter;

    @Nullable
    private Anchor voteRationale;

    public static GovernancePoolVote convertToRosetta(PoolGovernanceVoteParams voteParams) {
        GovernancePoolVoteBuilder governanceVoteBuilder = GovernancePoolVote.builder()
                .govActionId(convertToRosetta(voteParams.getGovernanceAction()))
                .poolCredentialHex(voteParams.getPoolCredential().getHexBytes())
                .voter(convertFromRosetta(voteParams.getPoolCredential())) // for now only support pool credential
                .vote(convertToRosetta(voteParams.getVote()));

        Optional.ofNullable(voteParams.getVoteRationale()).ifPresent(govVoteRationaleParams -> {
            governanceVoteBuilder.voteRationale(convertToRosetta(govVoteRationaleParams));
        });

        return governanceVoteBuilder.build();
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
                .anchorDataHash(decodeHexString(govAnchorParams.getDataHash()))
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
        Credential credential = Credential.fromKey(blake2bHash224(decodeHexString(poolCredential.getHexBytes())));

        return new Voter(STAKING_POOL_KEY_HASH, credential);
    }

    public static PublicKey convertToRosetta(String poolCredentialHex) {
        return new PublicKey(poolCredentialHex, EDWARDS25519);
    }

    public static Vote convertToRosetta(GovVoteParams voteParams) {
        return switch (voteParams) {
            case GovVoteParams.YES -> Vote.YES;
            case GovVoteParams.NO -> Vote.NO;
            case GovVoteParams.ABSTAIN -> Vote.ABSTAIN;
        };
    }

}
