package org.cardanofoundation.rosetta.api.block.model.repository;

import org.cardanofoundation.rosetta.api.block.model.repository.util.TxRepositoryQueryBuilder;
import org.cardanofoundation.rosetta.api.search.model.Currency;
import org.jooq.Condition;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BaseCurrencyConditionBuilderTest {

    /**
     * Concrete test implementation of BaseCurrencyConditionBuilder
     * that captures what values are passed to the template methods.
     */
    private static class TestCurrencyConditionBuilder extends TxRepositoryCustomBase.BaseCurrencyConditionBuilder {

        String lastPolicyId;
        String lastSymbol;
        String lastCalledMethod;

        @Override
        protected Condition buildPolicyIdAndSymbolCondition(String validatedPolicyId, String validatedSymbol) {
            lastCalledMethod = "policyIdAndSymbol";
            lastPolicyId = validatedPolicyId;
            lastSymbol = validatedSymbol;
            return DSL.trueCondition();
        }

        @Override
        protected Condition buildPolicyIdOnlyCondition(String validatedPolicyId) {
            lastCalledMethod = "policyIdOnly";
            lastPolicyId = validatedPolicyId;
            return DSL.trueCondition();
        }

        @Override
        protected Condition buildLovelaceCondition() {
            lastCalledMethod = "lovelace";
            return DSL.trueCondition();
        }

        @Override
        protected Condition buildSymbolOnlyCondition(String validatedSymbol) {
            lastCalledMethod = "symbolOnly";
            lastSymbol = validatedSymbol;
            return DSL.trueCondition();
        }
    }

    @Nested
    class PolicyIdInputValidationTests {

        @Test
        void shouldRejectPolicyId_withUnsanitizedInput() {
            // Given
            TestCurrencyConditionBuilder builder = new TestCurrencyConditionBuilder();
            Currency currency = Currency.builder()
                    .policyId("'}]') OR 1=1 --")
                    .build();

            // When & Then
            assertThatThrownBy(() -> builder.buildCurrencyCondition(currency))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("policyId must contain only hex characters");
        }

        @Test
        void shouldRejectPolicyId_withQuotes() {
            // Given
            TestCurrencyConditionBuilder builder = new TestCurrencyConditionBuilder();
            Currency currency = Currency.builder()
                    .policyId("\"malicious\"")
                    .build();

            // When & Then
            assertThatThrownBy(() -> builder.buildCurrencyCondition(currency))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("policyId must contain only hex characters");
        }

        @Test
        void shouldRejectPolicyId_withSpecialCharacters() {
            // Given
            TestCurrencyConditionBuilder builder = new TestCurrencyConditionBuilder();
            Currency currency = Currency.builder()
                    .policyId("'; DROP TABLE transaction; --")
                    .build();

            // When & Then
            assertThatThrownBy(() -> builder.buildCurrencyCondition(currency))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("policyId must contain only hex characters");
        }

        @Test
        void shouldAcceptValidHexPolicyId() {
            // Given
            TestCurrencyConditionBuilder builder = new TestCurrencyConditionBuilder();
            String validPolicyId = "d97e36383ae494e72b736ace04080f2953934626376ee06cf84adeb4";
            Currency currency = Currency.builder()
                    .policyId(validPolicyId)
                    .build();

            // When
            Condition result = builder.buildCurrencyCondition(currency);

            // Then
            assertThat(result).isNotNull();
            assertThat(builder.lastCalledMethod).isEqualTo("policyIdOnly");
            assertThat(builder.lastPolicyId).isEqualTo(validPolicyId);
        }

        @Test
        void shouldAcceptValidHexPolicyIdWithSymbol() {
            // Given
            TestCurrencyConditionBuilder builder = new TestCurrencyConditionBuilder();
            String validPolicyId = "d97e36383ae494e72b736ace04080f2953934626376ee06cf84adeb4";
            String validSymbol = "000de1404469616d6f6e64";
            Currency currency = Currency.builder()
                    .policyId(validPolicyId)
                    .symbol(validSymbol)
                    .build();

            // When
            Condition result = builder.buildCurrencyCondition(currency);

            // Then
            assertThat(result).isNotNull();
            assertThat(builder.lastCalledMethod).isEqualTo("policyIdAndSymbol");
            assertThat(builder.lastPolicyId).isEqualTo(validPolicyId);
            assertThat(builder.lastSymbol).isEqualTo(validSymbol);
        }
    }

    @Nested
    class SymbolInputValidationTests {

        @Test
        void shouldRejectSymbol_withUnsanitizedInput() {
            // Given
            TestCurrencyConditionBuilder builder = new TestCurrencyConditionBuilder();
            Currency currency = Currency.builder()
                    .symbol("' OR 1=1 --")
                    .build();

            // When & Then
            assertThatThrownBy(() -> builder.buildCurrencyCondition(currency))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("symbol must contain only hex characters");
        }

        @Test
        void shouldRejectSymbol_withNonHexWhenPolicyIdPresent() {
            // Given
            TestCurrencyConditionBuilder builder = new TestCurrencyConditionBuilder();
            Currency currency = Currency.builder()
                    .policyId("d97e36383ae494e72b736ace04080f2953934626376ee06cf84adeb4")
                    .symbol("not-hex!")
                    .build();

            // When & Then
            assertThatThrownBy(() -> builder.buildCurrencyCondition(currency))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("symbol must contain only hex characters");
        }

        @Test
        void shouldAllowLovelaceSymbol_withoutHexValidation() {
            // Given
            TestCurrencyConditionBuilder builder = new TestCurrencyConditionBuilder();
            Currency currency = Currency.builder()
                    .symbol("lovelace")
                    .build();

            // When
            Condition result = builder.buildCurrencyCondition(currency);

            // Then - lovelace is a special case, not hex validated
            assertThat(result).isNotNull();
            assertThat(builder.lastCalledMethod).isEqualTo("lovelace");
        }

        @Test
        void shouldAllowAdaSymbol_withoutHexValidation() {
            // Given
            TestCurrencyConditionBuilder builder = new TestCurrencyConditionBuilder();
            Currency currency = Currency.builder()
                    .symbol("ADA")
                    .build();

            // When
            Condition result = builder.buildCurrencyCondition(currency);

            // Then
            assertThat(result).isNotNull();
            assertThat(builder.lastCalledMethod).isEqualTo("lovelace");
        }
    }

    @Nested
    class EdgeCaseTests {

        @Test
        void shouldReturnFalseCondition_whenBothFieldsAreNull() {
            // Given
            TestCurrencyConditionBuilder builder = new TestCurrencyConditionBuilder();
            Currency currency = Currency.builder().build();

            // When
            Condition result = builder.buildCurrencyCondition(currency);

            // Then
            assertThat(result).isNotNull();
            assertThat(builder.lastCalledMethod).isNull(); // no template method called
        }

        @Test
        void shouldReturnFalseCondition_whenBothFieldsAreEmpty() {
            // Given
            TestCurrencyConditionBuilder builder = new TestCurrencyConditionBuilder();
            Currency currency = Currency.builder()
                    .policyId("")
                    .symbol("")
                    .build();

            // When
            Condition result = builder.buildCurrencyCondition(currency);

            // Then
            assertThat(result).isNotNull();
            assertThat(builder.lastCalledMethod).isNull();
        }

        @Test
        void shouldTrimWhitespace_fromPolicyId() {
            // Given
            TestCurrencyConditionBuilder builder = new TestCurrencyConditionBuilder();
            String validPolicyId = "d97e36383ae494e72b736ace04080f2953934626376ee06cf84adeb4";
            Currency currency = Currency.builder()
                    .policyId("  " + validPolicyId + "  ")
                    .build();

            // When
            Condition result = builder.buildCurrencyCondition(currency);

            // Then
            assertThat(result).isNotNull();
            assertThat(builder.lastPolicyId).isEqualTo(validPolicyId);
        }
    }
}
