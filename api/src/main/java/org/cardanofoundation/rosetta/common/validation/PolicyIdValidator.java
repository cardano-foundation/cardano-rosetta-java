package org.cardanofoundation.rosetta.common.validation;

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
     * Validates the given policy ID and throws if invalid.
     *
     * @param policyId the policy ID to validate
     * @param fieldName the field name for the error message
     * @return the validated policy ID (trimmed)
     * @throws IllegalArgumentException if the policy ID is not valid hex
     */
    public static String requireValid(String policyId, String fieldName) {
        String trimmed = policyId.trim();
        if (!isValid(trimmed)) {
            throw new IllegalArgumentException(
                    fieldName + " must be exactly " + Constants.POLICY_ID_LENGTH
                            + " hex characters, got: " + trimmed);
        }
        return trimmed;
    }

    /**
     * Checks whether the given string contains only hexadecimal characters.
     * This is a looser check than {@link #isValid(String)} — it does not enforce length.
     * Intended for defense-in-depth at the repository layer where any-length hex values
     * (e.g. symbol, unit) need to be validated before query construction.
     *
     * @param value the value to check
     * @return true if non-null, non-empty, and contains only hex characters
     */
    public static boolean isHexOnly(@Nullable String value) {
        return value != null && !value.isEmpty() && HexUtils.isHexString(value);
    }

    /**
     * Validates that the given value contains only hex characters and throws if not.
     *
     * @param value the value to validate
     * @param fieldName the field name for the error message
     * @return the validated value (trimmed)
     * @throws IllegalArgumentException if the value contains non-hex characters
     */
    public static String requireHexOnly(String value, String fieldName) {
        String trimmed = value.trim();
        if (!isHexOnly(trimmed)) {
            throw new IllegalArgumentException(
                    fieldName + " must contain only hex characters, got: " + trimmed);
        }
        return trimmed;
    }
}
