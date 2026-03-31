package org.cardanofoundation.rosetta.common.validation;

import org.cardanofoundation.rosetta.common.util.Constants;
import org.cardanofoundation.rosetta.common.util.HexUtils;

import javax.annotation.Nullable;

import static org.cardanofoundation.rosetta.common.util.Formatters.isEmptyHexString;

/**
 * Validates Cardano token names (asset names).
 * A valid token name is 0 to {@value Constants#ASSET_NAME_LENGTH} hexadecimal characters,
 * or the special empty hex string {@code \x}.
 */
public final class TokenNameValidator {

    private TokenNameValidator() {
        throw new IllegalArgumentException("TokenNameValidator is a utility class");
    }

    /**
     * Checks whether the given string is a valid Cardano token name.
     * Valid token names are hex-encoded with a maximum length of 64 characters,
     * or the empty hex string representation.
     *
     * @param name the token name to validate, may be null
     * @return true if valid, false if null or invalid
     */
    public static boolean isValid(@Nullable String name) {
        if (name == null) {
            return false;
        }
        if (isEmptyHexString(name)) {
            return true;
        }
        return name.length() <= Constants.ASSET_NAME_LENGTH && HexUtils.isHexString(name);
    }
}
