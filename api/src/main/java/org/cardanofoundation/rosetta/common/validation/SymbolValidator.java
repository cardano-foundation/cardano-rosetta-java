package org.cardanofoundation.rosetta.common.validation;

import org.cardanofoundation.rosetta.common.util.HexUtils;

import javax.annotation.Nullable;

/**
 * Validates Cardano native asset symbols (asset names in hex).
 * A valid symbol is a non-empty string of hexadecimal characters.
 */
public final class SymbolValidator {

    private SymbolValidator() {
        throw new IllegalArgumentException("SymbolValidator is a utility class");
    }

    /**
     * Checks whether the given string is a valid hex-encoded asset symbol.
     *
     * @param symbol the symbol to validate, may be null
     * @return true if non-null, non-empty, and contains only hex characters
     */
    public static boolean isValid(@Nullable String symbol) {
        return symbol != null && !symbol.isEmpty() && HexUtils.isHexString(symbol);
    }
}
