package org.cardanofoundation.rosetta.common.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenNameValidatorTest {

    @Nested
    class IsValidTests {

        @Test
        void shouldAcceptValidHexTokenName() {
            assertThat(TokenNameValidator.isValid("000de1404469616d6f6e64")).isTrue();
        }

        @Test
        void shouldAcceptEmptyHexString() {
            // \x is the special empty hex representation in Cardano
            assertThat(TokenNameValidator.isValid("\\x")).isTrue();
        }

        @Test
        void shouldAcceptEmptyString() {
            // Empty string is treated as empty hex by isEmptyHexString
            assertThat(TokenNameValidator.isValid("")).isTrue();
        }

        @Test
        void shouldAcceptSingleHexChar() {
            assertThat(TokenNameValidator.isValid("a")).isTrue();
        }

        @Test
        void shouldAcceptMaxLengthHex() {
            // 64 hex chars = maximum allowed
            assertThat(TokenNameValidator.isValid("a".repeat(64))).isTrue();
        }

        @Test
        void shouldRejectNull() {
            assertThat(TokenNameValidator.isValid(null)).isFalse();
        }

        @Test
        void shouldRejectTooLong() {
            // 65 hex chars = over maximum
            assertThat(TokenNameValidator.isValid("a".repeat(65))).isFalse();
        }

        @Test
        void shouldRejectNonHex() {
            assertThat(TokenNameValidator.isValid("Diamond")).isFalse();
        }

        @Test
        void shouldRejectSpecialCharacters() {
            assertThat(TokenNameValidator.isValid("test@123")).isFalse();
        }

        @Test
        void shouldRejectSpaces() {
            assertThat(TokenNameValidator.isValid("abc def")).isFalse();
        }

        @Test
        void shouldAcceptUppercaseHex() {
            assertThat(TokenNameValidator.isValid("ABCDEF0123456789")).isTrue();
        }

        @Test
        void shouldAcceptCIP68Prefix() {
            // CIP-68 asset with binary prefix
            assertThat(TokenNameValidator.isValid("000643b04469616d6f6e64")).isTrue();
        }
    }
}
