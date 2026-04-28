package org.cardanofoundation.rosetta.common.validation;

import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

class SymbolValidatorTest {

    @Nested
    class IsValidTests {

        @Test
        void shouldAcceptValidHexSymbol() {
            assertThat(SymbolValidator.isValid("000de1404469616d6f6e64")).isTrue();
        }

        @Test
        void shouldAcceptSingleHexChar() {
            assertThat(SymbolValidator.isValid("a")).isTrue();
        }

        @Test
        void shouldAcceptUppercaseHex() {
            assertThat(SymbolValidator.isValid("ABCDEF0123456789")).isTrue();
        }

        @Test
        void shouldAcceptCIP68Prefix() {
            assertThat(SymbolValidator.isValid("000643b04469616d6f6e64")).isTrue();
        }

        @Test
        void shouldRejectNull() {
            assertThat(SymbolValidator.isValid(null)).isFalse();
        }

        @Test
        void shouldRejectEmpty() {
            assertThat(SymbolValidator.isValid("")).isFalse();
        }

        @Test
        void shouldRejectNonHex() {
            assertThat(SymbolValidator.isValid("Diamond")).isFalse();
        }

        @Test
        void shouldRejectSpecialCharacters() {
            assertThat(SymbolValidator.isValid("test@123")).isFalse();
        }

        @Test
        void shouldRejectSpaces() {
            assertThat(SymbolValidator.isValid("abc def")).isFalse();
        }

        @Test
        void shouldRejectSqlPayload() {
            assertThat(SymbolValidator.isValid("' OR 1=1 --")).isFalse();
        }
    }

    @Nested
    class ValidateTests {

        @Test
        void shouldAcceptNull() {
            assertThatNoException().isThrownBy(() -> SymbolValidator.validate(null));
        }

        @Test
        void shouldAcceptLovelace() {
            assertThatNoException().isThrownBy(() -> SymbolValidator.validate("lovelace"));
        }

        @Test
        void shouldAcceptAda() {
            assertThatNoException().isThrownBy(() -> SymbolValidator.validate("ADA"));
        }

        @Test
        void shouldAcceptValidHexSymbol() {
            assertThatNoException().isThrownBy(() -> SymbolValidator.validate("000de1404469616d6f6e64"));
        }

        @Test
        void shouldThrowForNonHexSymbol() {
            assertThatThrownBy(() -> SymbolValidator.validate("Diamond"))
                    .isInstanceOf(ApiException.class);
        }

        @Test
        void shouldThrowForSqlPayload() {
            assertThatThrownBy(() -> SymbolValidator.validate("' OR 1=1 --"))
                    .isInstanceOf(ApiException.class);
        }
    }
}
