package org.cardanofoundation.rosetta.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class HexUtilsTest {

    @Test
    @DisplayName("Should return true for valid lowercase hex strings")
    void shouldReturnTrueForValidLowercaseHex() {
        assertThat(HexUtils.isHexString("deadbeef")).isTrue();
        assertThat(HexUtils.isHexString("0123456789abcdef")).isTrue();
        assertThat(HexUtils.isHexString("a")).isTrue();
        assertThat(HexUtils.isHexString("000de1404469616d6f6e64486f6f76657332363938")).isTrue();
    }

    @Test
    @DisplayName("Should return true for valid uppercase hex strings")
    void shouldReturnTrueForValidUppercaseHex() {
        assertThat(HexUtils.isHexString("DEADBEEF")).isTrue();
        assertThat(HexUtils.isHexString("0123456789ABCDEF")).isTrue();
        assertThat(HexUtils.isHexString("A")).isTrue();
        assertThat(HexUtils.isHexString("000DE1404469616D6F6E64486F6F76657332363938")).isTrue();
    }

    @Test
    @DisplayName("Should return true for valid mixed case hex strings")
    void shouldReturnTrueForValidMixedCaseHex() {
        assertThat(HexUtils.isHexString("DeAdBeEf")).isTrue();
        assertThat(HexUtils.isHexString("0123456789AbCdEf")).isTrue();
        assertThat(HexUtils.isHexString("aB")).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Should return false for null or empty strings")
    void shouldReturnFalseForNullOrEmpty(String input) {
        assertThat(HexUtils.isHexString(input)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "test",           // ASCII text
        "hello world",    // ASCII with space
        "0xdeadbeef",     // Hex with prefix
        "g",              // Invalid hex char
        "123xyz",         // Mixed valid and invalid
        "Diamond",        // ASCII name (issue #610 example)
        "!@#$",           // Special characters
        " deadbeef",      // Leading space
        "deadbeef ",      // Trailing space
        "dead beef"       // Space in middle
    })
    @DisplayName("Should return false for invalid hex strings")
    void shouldReturnFalseForInvalidHex(String input) {
        assertThat(HexUtils.isHexString(input)).isFalse();
    }

    @Test
    @DisplayName("Should handle CIP-68 asset names with binary prefixes")
    void shouldHandleCIP68Assets() {
        // CIP-68 assets have binary prefixes like (000643b0) followed by hex-encoded name
        String cip68AssetName = "000643b04469616d6f6e64"; // (000643b0) + hex("Diamond")
        assertThat(HexUtils.isHexString(cip68AssetName)).isTrue();
    }

    @Test
    @DisplayName("Should validate policy IDs correctly")
    void shouldValidatePolicyIds() {
        // Standard Cardano policy ID (56 hex chars)
        String policyId = "e16c2dc8ae937e8d3790c7fd7168d7b994621ba14ca11415f39fed72";
        assertThat(HexUtils.isHexString(policyId)).isTrue();
    }
}
