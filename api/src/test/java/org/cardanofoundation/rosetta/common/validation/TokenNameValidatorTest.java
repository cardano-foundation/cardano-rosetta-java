package org.cardanofoundation.rosetta.common.validation;

import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

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

    @Nested
    class ValidateTests {

        @Test
        void shouldAcceptValidTokenName() {
            assertThatNoException().isThrownBy(() -> TokenNameValidator.validate("000de1404469616d6f6e64"));
        }

        @Test
        void shouldAcceptEmptyHexString() {
            assertThatNoException().isThrownBy(() -> TokenNameValidator.validate("\\x"));
        }

        @Test
        void shouldThrowForNull() {
            assertThatThrownBy(() -> TokenNameValidator.validate(null))
                    .isInstanceOf(ApiException.class)
                    .extracting("error.message")
                    .isEqualTo("Invalid token name");
        }

        @Test
        void shouldThrowForNonHex() {
            assertThatThrownBy(() -> TokenNameValidator.validate("Diamond"))
                    .isInstanceOf(ApiException.class);
        }
    }
}
