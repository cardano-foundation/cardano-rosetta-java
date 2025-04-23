package org.cardanofoundation.rosetta.api.block.model.domain;

import lombok.*;

import com.bloxbean.cardano.client.transaction.spec.governance.DRepType;
import org.openapitools.client.model.DRepParams;
import org.openapitools.client.model.DRepTypeParams;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class DRepDelegation {

    private String txHash;
    private long certIndex;
    private String address;
    private DRep drep;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class DRep {

        private String drepId;
        private DRepType drepType;

        public static DRepDelegation.DRep convertDRepToRosetta(DRepParams dRepParams) {
            DRepType dRepType = switch (dRepParams.getType()) {
                case KEY_HASH -> DRepType.ADDR_KEYHASH;
                case SCRIPT_HASH -> DRepType.SCRIPTHASH;
                case ABSTAIN -> DRepType.ABSTAIN;
                case NO_CONFIDENCE -> DRepType.NO_CONFIDENCE;
            };

            return new DRepDelegation.DRep(dRepParams.getId(), dRepType);
        }

        public static DRepParams convertDRepFromRosetta(DRepDelegation.DRep drep) {
            DRepTypeParams dRepTypeParams = switch (drep.getDrepType()) {
                case ADDR_KEYHASH -> DRepTypeParams.KEY_HASH;
                case SCRIPTHASH -> DRepTypeParams.SCRIPT_HASH;
                case ABSTAIN -> DRepTypeParams.ABSTAIN;
                case NO_CONFIDENCE -> DRepTypeParams.NO_CONFIDENCE;
            };

            return new DRepParams()
                    .id(drep.getDrepId())
                    .type(dRepTypeParams);
        }

    }

}
