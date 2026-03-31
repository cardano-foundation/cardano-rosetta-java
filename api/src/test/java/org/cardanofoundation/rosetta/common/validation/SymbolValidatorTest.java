package org.cardanofoundation.rosetta.common.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
