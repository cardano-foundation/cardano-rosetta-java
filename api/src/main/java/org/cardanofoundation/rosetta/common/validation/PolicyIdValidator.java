package org.cardanofoundation.rosetta.common.validation;

import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.cardanofoundation.rosetta.common.util.HexUtils;

import javax.annotation.Nullable;

/**
 * Validates Cardano policy IDs.
 * A valid policy ID is exactly {@value Constants#POLICY_ID_LENGTH} hexadecimal characters.
 */
public final class PolicyIdValidator {

    private PolicyIdValidator() {
        throw new IllegalArgumentException("PolicyIdValidator is a utility class");
    }

    /**
     * Checks whether the given string is a valid Cardano policy ID.
     * A valid policy ID must be exactly 56 hex characters (0-9, a-f, A-F).
     *
     * @param policyId the policy ID to validate, may be null
     * @return true if valid, false if null or invalid
     */
    public static boolean isValid(@Nullable String policyId) {
        return policyId != null
                && policyId.length() == Constants.POLICY_ID_LENGTH
                && HexUtils.isHexString(policyId);
    }

    /**
     * Validates that the given policy ID is valid, if present.
     * Null values are accepted (no policy ID provided). Non-null values must be
     * exactly 56 hex characters.
     *
     * @param policyId the policy ID to validate, may be null
     * @throws org.cardanofoundation.rosetta.common.exception.ApiException if non-null and invalid
     */
    public static void validate(@Nullable String policyId) {
        if (policyId != null && !isValid(policyId)) {
            throw ExceptionFactory.invalidPolicyIdError("Given policy id is " + policyId);
        }
    }
}
