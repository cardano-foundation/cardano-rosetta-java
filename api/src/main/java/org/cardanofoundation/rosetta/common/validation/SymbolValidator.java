package org.cardanofoundation.rosetta.common.validation;

import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.util.Constants;
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

    /**
     * Validates that a currency symbol is hex-encoded for native assets.
     * ADA and lovelace symbols are skipped (not hex-encoded).
     * Null symbols are accepted.
     *
     * @param symbol the currency symbol to validate, may be null
     * @throws org.cardanofoundation.rosetta.common.exception.ApiException if non-ADA symbol is not valid hex
     */
    public static void validate(@Nullable String symbol) {
        if (symbol == null
                || Constants.LOVELACE.equalsIgnoreCase(symbol)
                || Constants.ADA.equals(symbol)) {
            return;
        }
        if (!isValid(symbol)) {
            throw ExceptionFactory.currencySymbolNotHex(symbol);
        }
    }
}
