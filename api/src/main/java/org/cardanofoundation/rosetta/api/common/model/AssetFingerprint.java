package org.cardanofoundation.rosetta.api.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.cardanofoundation.rosetta.common.util.Constants;

import javax.annotation.Nullable;

import static org.cardanofoundation.rosetta.common.util.HexUtils.isHexString;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class AssetFingerprint {

    private final String policyId;
    private final String symbol; // assetName as hex

    public String toSubject() {
        return policyId + symbol;
    }

    public String toUnit() {
        return policyId + symbol;
    }

    public static AssetFingerprint fromUnit(String unit) {
        return fromSubject(unit);
    }

    /**
     * Creates an AssetFingerprint from separate policyId and symbol.
     * This is a convenience factory method for internal use when policyId and symbol are already separated.
     * For parsing external subject strings, use {@link #fromSubject(String)} which includes validation.
     *
     * @param policyId the policy ID
     * @param symbol the symbol
     * @return AssetFingerprint instance
     */
    public static AssetFingerprint of(String policyId, String symbol) {
        return new AssetFingerprint(policyId, symbol);
    }

    /**
     * Creates an AssetFingerprint from a subject string (policyId + symbol in hex).
     *
     * @param subject the concatenated policyId and symbol in hex format
     * @return AssetFingerprint instance, or null if subject is invalid
     */
    @Nullable
    public static AssetFingerprint fromSubject(@Nullable String subject) {
        if (subject == null || subject.length() < Constants.POLICY_ID_LENGTH) {
            throw new NullPointerException("subject is null");
        }

        // Validate that subject is valid hex
        if (!isHexString(subject)) {
            throw new IllegalArgumentException("subject is not a hex string");
        }

        String policyId = subject.substring(0, Constants.POLICY_ID_LENGTH);
        String symbol = subject.substring(Constants.POLICY_ID_LENGTH);

        return new AssetFingerprint(policyId, symbol);
    }

}
