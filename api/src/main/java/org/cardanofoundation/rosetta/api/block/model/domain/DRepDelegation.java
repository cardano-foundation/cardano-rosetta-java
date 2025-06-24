package org.cardanofoundation.rosetta.api.block.model.domain;

import java.util.Arrays;

import lombok.*;

import com.bloxbean.cardano.client.transaction.spec.governance.DRepType;
import com.bloxbean.cardano.client.util.HexUtil;
import org.openapitools.client.model.DRepParams;
import org.openapitools.client.model.DRepTypeParams;

import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DRepDelegation {

    private String txHash;
    private long certIndex;
    private String address;
    private DRep drep;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class DRep {

        private String drepId;
        private DRepType drepType;

        public static DRep convertDRepToRosetta(DRepParams dRepParams) {
            if (dRepParams == null || dRepParams.getType() == null) {
                throw ExceptionFactory.missingDrep();
            }

            DRepType dRepType = switch (dRepParams.getType()) {
                case KEY_HASH -> DRepType.ADDR_KEYHASH;
                case SCRIPT_HASH -> DRepType.SCRIPTHASH;
                case ABSTAIN -> DRepType.ABSTAIN;
                case NO_CONFIDENCE -> DRepType.NO_CONFIDENCE;
            };

            if (dRepType == DRepType.ABSTAIN || dRepType == DRepType.NO_CONFIDENCE) {
                return new DRep(null, dRepType);
            }

            if (dRepParams.getId() == null || dRepParams.getId().isBlank()) {
                throw ExceptionFactory.missingDRepId();
            }

            byte[] idBytes = HexUtil.decodeHexString(dRepParams.getId());

            if (idBytes.length == 29) {
                byte tag = idBytes[0];
                byte[] stripped = Arrays.copyOfRange(idBytes, 1, 29);
                String strippedHex = HexUtil.encodeHexString(stripped);

                DRepType dRepHashType = switch (tag) {
                    case 0x22 -> DRepType.ADDR_KEYHASH;
                    case 0x23 -> DRepType.SCRIPTHASH;
                    default -> throw ExceptionFactory.invalidDrepType();
                };

                if (!dRepHashType.equals(dRepType)) {
                    throw ExceptionFactory.invalidDrepType();
                }

                return new DRep(strippedHex, dRepHashType);
            }

            return new DRep(dRepParams.getId(), dRepType);
        }

        public static DRepParams convertDRepFromRosetta(DRep drep) {
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
