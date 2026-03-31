package org.cardanofoundation.rosetta.common.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
