package org.cardanofoundation.rosetta.common.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolicyIdValidatorTest {

    private static final String VALID_POLICY_ID = "d97e36383ae494e72b736ace04080f2953934626376ee06cf84adeb4";

    @Nested
    class IsValidTests {

        @Test
        void shouldAcceptValid56CharHexPolicyId() {
            assertThat(PolicyIdValidator.isValid(VALID_POLICY_ID)).isTrue();
        }

        @Test
        void shouldAcceptUppercaseHex() {
            assertThat(PolicyIdValidator.isValid("D97E36383AE494E72B736ACE04080F2953934626376EE06CF84ADEB4")).isTrue();
        }

        @Test
        void shouldAcceptMixedCaseHex() {
            assertThat(PolicyIdValidator.isValid("d97E36383ae494E72b736ACE04080f2953934626376ee06cf84aDEB4")).isTrue();
        }

        @Test
        void shouldRejectNull() {
            assertThat(PolicyIdValidator.isValid(null)).isFalse();
        }

        @Test
        void shouldRejectEmptyString() {
            assertThat(PolicyIdValidator.isValid("")).isFalse();
        }

        @Test
        void shouldRejectTooShort() {
            assertThat(PolicyIdValidator.isValid("abcdef1234")).isFalse();
        }

        @Test
        void shouldRejectTooLong() {
            assertThat(PolicyIdValidator.isValid("a".repeat(57))).isFalse();
        }

        @Test
        void shouldRejectNonHexCharacters() {
            // 56 chars but contains 'g' and 'X'
            assertThat(PolicyIdValidator.isValid("g97e36383ae494e72b736ace04080f2953934626376ee06cf84adeX4")).isFalse();
        }

        @Test
        void shouldRejectSqlInjectionPayload() {
            assertThat(PolicyIdValidator.isValid("'}]') OR 1=1 --")).isFalse();
        }

        @Test
        void shouldRejectDropTablePayload() {
            assertThat(PolicyIdValidator.isValid("'; DROP TABLE transaction; --")).isFalse();
        }

        @Test
        void shouldReject55Chars() {
            assertThat(PolicyIdValidator.isValid("a".repeat(55))).isFalse();
        }
    }

    @Nested
    class RequireValidTests {

        @Test
        void shouldReturnTrimmedValueWhenValid() {
            String result = PolicyIdValidator.requireValid("  " + VALID_POLICY_ID + "  ", "policyId");
            assertThat(result).isEqualTo(VALID_POLICY_ID);
        }

        @Test
        void shouldThrowForInvalidPolicyId() {
            assertThatThrownBy(() -> PolicyIdValidator.requireValid("invalid", "policyId"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("policyId")
                    .hasMessageContaining("56 hex characters");
        }
    }

    @Nested
    class IsHexOnlyTests {

        @Test
        void shouldAcceptAnyLengthHexString() {
            assertThat(PolicyIdValidator.isHexOnly("abcdef0123456789")).isTrue();
        }

        @Test
        void shouldAcceptSingleChar() {
            assertThat(PolicyIdValidator.isHexOnly("a")).isTrue();
        }

        @Test
        void shouldRejectNull() {
            assertThat(PolicyIdValidator.isHexOnly(null)).isFalse();
        }

        @Test
        void shouldRejectEmpty() {
            assertThat(PolicyIdValidator.isHexOnly("")).isFalse();
        }

        @Test
        void shouldRejectNonHex() {
            assertThat(PolicyIdValidator.isHexOnly("not-hex!")).isFalse();
        }

        @Test
        void shouldRejectSpaces() {
            assertThat(PolicyIdValidator.isHexOnly("abc def")).isFalse();
        }

        @Test
        void shouldRejectSqlPayload() {
            assertThat(PolicyIdValidator.isHexOnly("' OR 1=1 --")).isFalse();
        }
    }

    @Nested
    class RequireHexOnlyTests {

        @Test
        void shouldReturnTrimmedHexValue() {
            String result = PolicyIdValidator.requireHexOnly("  abcdef  ", "field");
            assertThat(result).isEqualTo("abcdef");
        }

        @Test
        void shouldThrowForNonHexValue() {
            assertThatThrownBy(() -> PolicyIdValidator.requireHexOnly("not-hex!", "symbol"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("symbol")
                    .hasMessageContaining("hex characters");
        }

        @Test
        void shouldThrowForSqlInjection() {
            assertThatThrownBy(() -> PolicyIdValidator.requireHexOnly("'; DROP TABLE t; --", "policyId"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
