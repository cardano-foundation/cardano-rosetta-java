package org.cardanofoundation.rosetta.common.util;

import javax.annotation.Nullable;

/**
 * Utility class for hexadecimal string validation and operations.
 */
public final class HexUtils {

    private HexUtils() {
        throw new IllegalArgumentException("HexUtils is a utility class, a constructor is private");
    }

    /**
     * Validates if a string contains only hexadecimal characters (0-9, a-f, A-F).
     * Empty strings and null values are considered invalid.
     *
     * @param str the string to validate
     * @return true if the string is a valid hexadecimal string, false otherwise
     */
    public static boolean isHexString(@Nullable String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        // Use simple regex validation since Guava's canDecode requires even-length strings
        // (it validates byte arrays), but we need to validate any hex string
        return str.matches("^[0-9a-fA-F]+$");
    }

}
