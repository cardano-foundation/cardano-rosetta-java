package org.cardanofoundation.rosetta.api.common.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssetFingerprintTest {

    private static final String POLICY_ID = "1e349c9bdea19fd6c147626a5260bc44b71635f398b67c59881df209";
    private static final String SYMBOL_HEX = "504154415445";
    private static final String SUBJECT = POLICY_ID + SYMBOL_HEX;

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should create AssetFingerprint from valid subject")
        void shouldCreateFromValidSubject() {
            // when
            AssetFingerprint result = AssetFingerprint.fromSubject(SUBJECT);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getPolicyId()).isEqualTo(POLICY_ID);
            assertThat(result.getSymbol()).isEqualTo(SYMBOL_HEX);
        }

        @Test
        @DisplayName("Should create AssetFingerprint from valid unit")
        void shouldCreateFromValidUnit() {
            // when
            AssetFingerprint result = AssetFingerprint.fromUnit(SUBJECT);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getPolicyId()).isEqualTo(POLICY_ID);
            assertThat(result.getSymbol()).isEqualTo(SYMBOL_HEX);
        }

        @Test
        @DisplayName("Should create AssetFingerprint with empty symbol")
        void shouldCreateWithEmptySymbol() {
            // given
            String subjectWithoutSymbol = POLICY_ID;

            // when
            AssetFingerprint result = AssetFingerprint.fromSubject(subjectWithoutSymbol);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getPolicyId()).isEqualTo(POLICY_ID);
            assertThat(result.getSymbol()).isEmpty();
        }

        @Test
        @DisplayName("Should handle uppercase hex characters")
        void shouldHandleUppercaseHex() {
            // given
            String upperCaseSubject = POLICY_ID.toUpperCase() + SYMBOL_HEX.toUpperCase();

            // when
            AssetFingerprint result = AssetFingerprint.fromSubject(upperCaseSubject);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getPolicyId()).isEqualTo(POLICY_ID.toUpperCase());
            assertThat(result.getSymbol()).isEqualTo(SYMBOL_HEX.toUpperCase());
        }

        @Test
        @DisplayName("Should handle mixed case hex characters")
        void shouldHandleMixedCaseHex() {
            // given
            String mixedCaseSubject = "1E349c9bDEA19fd6c147626a5260bc44b71635f398b67c59881df209504154415445";

            // when
            AssetFingerprint result = AssetFingerprint.fromSubject(mixedCaseSubject);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getPolicyId()).isEqualTo("1E349c9bDEA19fd6c147626a5260bc44b71635f398b67c59881df209");
            assertThat(result.getSymbol()).isEqualTo("504154415445");
        }

        @Test
        @DisplayName("Should throw NullPointerException for null subject")
        void shouldThrowNullPointerExceptionForNullSubject() {
            // when/then
            assertThatThrownBy(() -> AssetFingerprint.fromSubject(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("subject is null");
        }

        @Test
        @DisplayName("Should throw NullPointerException for empty subject")
        void shouldThrowNullPointerExceptionForEmptySubject() {
            // when/then
            assertThatThrownBy(() -> AssetFingerprint.fromSubject(""))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("subject is null");
        }

        @Test
        @DisplayName("Should throw NullPointerException for subject shorter than policy ID length")
        void shouldThrowNullPointerExceptionForShortSubject() {
            // given
            String shortSubject = "1e349c9bdea19fd6c147626a5260bc44b71635f398b67c59881df2"; // 55 chars

            // when/then
            assertThatThrownBy(() -> AssetFingerprint.fromSubject(shortSubject))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("subject is null");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for non-hex characters")
        void shouldThrowIllegalArgumentExceptionForNonHexCharacters() {
            // given
            String invalidSubject = "1e349c9bdea19fd6c147626a5260bc44b71635f398b67c59881df20G504154415445"; // contains 'G'

            // when/then
            assertThatThrownBy(() -> AssetFingerprint.fromSubject(invalidSubject))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("subject is not a hex string");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for subject with special characters")
        void shouldThrowIllegalArgumentExceptionForSpecialCharacters() {
            // given
            String invalidSubject = POLICY_ID + "-" + SYMBOL_HEX;

            // when/then
            assertThatThrownBy(() -> AssetFingerprint.fromSubject(invalidSubject))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("subject is not a hex string");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for subject with spaces")
        void shouldThrowIllegalArgumentExceptionForSubjectWithSpaces() {
            // given
            String invalidSubject = POLICY_ID + " " + SYMBOL_HEX;

            // when/then
            assertThatThrownBy(() -> AssetFingerprint.fromSubject(invalidSubject))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("subject is not a hex string");
        }
    }

    @Nested
    @DisplayName("Conversion Method Tests")
    class ConversionMethodTests {

        @Test
        @DisplayName("Should convert to subject correctly")
        void shouldConvertToSubject() {
            // given
            AssetFingerprint fingerprint = AssetFingerprint.of(POLICY_ID, SYMBOL_HEX);

            // when
            String result = fingerprint.toSubject();

            // then
            assertThat(result).isEqualTo(SUBJECT);
        }

        @Test
        @DisplayName("Should convert to unit correctly")
        void shouldConvertToUnit() {
            // given
            AssetFingerprint fingerprint = AssetFingerprint.of(POLICY_ID, SYMBOL_HEX);

            // when
            String result = fingerprint.toUnit();

            // then
            assertThat(result).isEqualTo(SUBJECT);
        }

        @Test
        @DisplayName("toSubject and toUnit should return same value")
        void toSubjectAndToUnitShouldReturnSameValue() {
            // given
            AssetFingerprint fingerprint = AssetFingerprint.of(POLICY_ID, SYMBOL_HEX);

            // when
            String subject = fingerprint.toSubject();
            String unit = fingerprint.toUnit();

            // then
            assertThat(subject).isEqualTo(unit);
        }

        @Test
        @DisplayName("Should handle empty symbol in conversion")
        void shouldHandleEmptySymbolInConversion() {
            // given
            AssetFingerprint fingerprint = AssetFingerprint.of(POLICY_ID, "");

            // when
            String result = fingerprint.toSubject();

            // then
            assertThat(result).isEqualTo(POLICY_ID);
        }
    }

    @Nested
    @DisplayName("Round-Trip Tests")
    class RoundTripTests {

        @Test
        @DisplayName("Should preserve data through fromSubject -> toSubject round-trip")
        void shouldPreserveDataThroughSubjectRoundTrip() {
            // when
            AssetFingerprint fingerprint = AssetFingerprint.fromSubject(SUBJECT);
            String roundTripped = fingerprint.toSubject();

            // then
            assertThat(roundTripped).isEqualTo(SUBJECT);
        }

        @Test
        @DisplayName("Should preserve data through fromUnit -> toUnit round-trip")
        void shouldPreserveDataThroughUnitRoundTrip() {
            // when
            AssetFingerprint fingerprint = AssetFingerprint.fromUnit(SUBJECT);
            String roundTripped = fingerprint.toUnit();

            // then
            assertThat(roundTripped).isEqualTo(SUBJECT);
        }

        @Test
        @DisplayName("Should preserve data with empty symbol through round-trip")
        void shouldPreserveEmptySymbolThroughRoundTrip() {
            // given
            String policyIdOnly = POLICY_ID;

            // when
            AssetFingerprint fingerprint = AssetFingerprint.fromSubject(policyIdOnly);
            String roundTripped = fingerprint.toSubject();

            // then
            assertThat(roundTripped).isEqualTo(policyIdOnly);
        }
    }

    @Nested
    @DisplayName("Equality and HashCode Tests")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when policyId and symbol match")
        void shouldBeEqualWhenFieldsMatch() {
            // given
            AssetFingerprint fingerprint1 = AssetFingerprint.of(POLICY_ID, SYMBOL_HEX);
            AssetFingerprint fingerprint2 = AssetFingerprint.of(POLICY_ID, SYMBOL_HEX);

            // then
            assertThat(fingerprint1).isEqualTo(fingerprint2);
            assertThat(fingerprint1.hashCode()).isEqualTo(fingerprint2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when policyId differs")
        void shouldNotBeEqualWhenPolicyIdDiffers() {
            // given
            AssetFingerprint fingerprint1 = AssetFingerprint.of(POLICY_ID, SYMBOL_HEX);
            AssetFingerprint fingerprint2 = AssetFingerprint.of("ffffffffffffffffffffffffffffffffffffffffffffffffffffffff", SYMBOL_HEX);

            // then
            assertThat(fingerprint1).isNotEqualTo(fingerprint2);
        }

        @Test
        @DisplayName("Should not be equal when symbol differs")
        void shouldNotBeEqualWhenSymbolDiffers() {
            // given
            AssetFingerprint fingerprint1 = AssetFingerprint.of(POLICY_ID, SYMBOL_HEX);
            AssetFingerprint fingerprint2 = AssetFingerprint.of(POLICY_ID, "123456");

            // then
            assertThat(fingerprint1).isNotEqualTo(fingerprint2);
        }

        @Test
        @DisplayName("Fingerprints created from same subject should be equal")
        void fingerprintsFromSameSubjectShouldBeEqual() {
            // when
            AssetFingerprint fingerprint1 = AssetFingerprint.fromSubject(SUBJECT);
            AssetFingerprint fingerprint2 = AssetFingerprint.fromSubject(SUBJECT);

            // then
            assertThat(fingerprint1).isEqualTo(fingerprint2);
            assertThat(fingerprint1.hashCode()).isEqualTo(fingerprint2.hashCode());
        }
    }
}
