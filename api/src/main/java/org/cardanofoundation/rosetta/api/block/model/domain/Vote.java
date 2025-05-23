package org.cardanofoundation.rosetta.api.block.model.domain;

import com.bloxbean.cardano.client.transaction.spec.governance.VoterType;
import lombok.*;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.openapitools.client.model.VoterParams;
import org.openapitools.client.model.VoterTypeParams;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vote {

    private String txHash;
    private long index;

    private String address;
    private Voter voter;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Voter {

        private String voterId;
        private VoterType voterType;

        public static Vote.Voter convertVoterToRosetta(VoterParams voterParams) {
            VoterType voteType = switch (voterParams.getType()) {
                case VoterTypeParams.DREP_KEY_HASH -> VoterType.DREP_KEY_HASH;
                case VoterTypeParams.STAKING_POOL_KEY_HASH -> VoterType.STAKING_POOL_KEY_HASH;
                case VoterTypeParams.DREP_SCRIPT_HASH -> VoterType.DREP_SCRIPT_HASH;
            };

            return new Vote.Voter(voterParams.getId(), voteType);
        }

        public static VoterParams convertVoterFromRosetta(Vote.Voter voter) {
            VoterTypeParams voterTypeParams = switch (voter.getVoterType()) {
                case VoterType.DREP_KEY_HASH -> VoterTypeParams.DREP_KEY_HASH;
                case VoterType.DREP_SCRIPT_HASH -> VoterTypeParams.DREP_SCRIPT_HASH;
                case VoterType.STAKING_POOL_KEY_HASH -> VoterTypeParams.STAKING_POOL_KEY_HASH;
                default -> throw ExceptionFactory.invalidVoterType();
            };

            return new VoterParams()
                    .id(voter.getVoterId())
                    .type(voterTypeParams);
        }

    }

}
