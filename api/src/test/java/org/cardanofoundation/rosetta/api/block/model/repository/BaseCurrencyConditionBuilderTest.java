package org.cardanofoundation.rosetta.api.block.model.repository;

import org.cardanofoundation.rosetta.api.search.model.Currency;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.jooq.Condition;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BaseCurrencyConditionBuilderTest {

    private static final String VALID_POLICY_ID = "d97e36383ae494e72b736ace04080f2953934626376ee06cf84adeb4";
    private static final String VALID_SYMBOL = "000de1404469616d6f6e64";

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
    class PolicyIdValidationTests {

        @Test
        void shouldRejectPolicyId_withSqlInjection() {
            TestCurrencyConditionBuilder builder = new TestCurrencyConditionBuilder();
            Currency currency = Currency.builder()
                    .policyId("'}]') OR 1=1 --")
                    .build();

            assertThatThrownBy(() -> builder.buildCurrencyCondition(currency))
                    .isInstanceOf(ApiException.class);
        }

        @Test
        void shouldRejectPolicyId_withSpecialCharacters() {
            TestCurrencyConditionBuilder builder = new TestCurrencyConditionBuilder();
            Currency currency = Currency.builder()
                    .policyId("'; DROP TABLE transaction; --")
                    .build();

            assertThatThrownBy(() -> builder.buildCurrencyCondition(currency))
                    .isInstanceOf(ApiException.class);
        }

        @Test
        void shouldAcceptValidHexPolicyId() {
            TestCurrencyConditionBuilder builder = new TestCurrencyConditionBuilder();
            Currency currency = Currency.builder()
                    .policyId(VALID_POLICY_ID)
                    .build();

            Condition result = builder.buildCurrencyCondition(currency);

            assertThat(result).isNotNull();
            assertThat(builder.lastCalledMethod).isEqualTo("policyIdOnly");
            assertThat(builder.lastPolicyId).isEqualTo(VALID_POLICY_ID);
        }

        @Test
        void shouldAcceptValidHexPolicyIdWithSymbol() {
            TestCurrencyConditionBuilder builder = new TestCurrencyConditionBuilder();
            Currency currency = Currency.builder()
                    .policyId(VALID_POLICY_ID)
                    .symbol(VALID_SYMBOL)
                    .build();

            Condition result = builder.buildCurrencyCondition(currency);

            assertThat(result).isNotNull();
            assertThat(builder.lastCalledMethod).isEqualTo("policyIdAndSymbol");
            assertThat(builder.lastPolicyId).isEqualTo(VALID_POLICY_ID);
            assertThat(builder.lastSymbol).isEqualTo(VALID_SYMBOL);
        }
    }

    @Nested
    class SymbolValidationTests {

        @Test
        void shouldRejectSymbol_withSqlInjection() {
            TestCurrencyConditionBuilder builder = new TestCurrencyConditionBuilder();
            Currency currency = Currency.builder()
                    .symbol("' OR 1=1 --")
                    .build();

            assertThatThrownBy(() -> builder.buildCurrencyCondition(currency))
                    .isInstanceOf(ApiException.class);
        }

        @Test
        void shouldRejectSymbol_withNonHexWhenPolicyIdPresent() {
            TestCurrencyConditionBuilder builder = new TestCurrencyConditionBuilder();
            Currency currency = Currency.builder()
                    .policyId(VALID_POLICY_ID)
                    .symbol("not-hex!")
                    .build();

            assertThatThrownBy(() -> builder.buildCurrencyCondition(currency))
                    .isInstanceOf(ApiException.class);
        }

        @Test
        void shouldAllowLovelaceSymbol() {
            TestCurrencyConditionBuilder builder = new TestCurrencyConditionBuilder();
            Currency currency = Currency.builder()
                    .symbol("lovelace")
                    .build();

            Condition result = builder.buildCurrencyCondition(currency);

            assertThat(result).isNotNull();
            assertThat(builder.lastCalledMethod).isEqualTo("lovelace");
        }

        @Test
        void shouldAllowAdaSymbol() {
            TestCurrencyConditionBuilder builder = new TestCurrencyConditionBuilder();
            Currency currency = Currency.builder()
                    .symbol("ADA")
                    .build();

            Condition result = builder.buildCurrencyCondition(currency);

            assertThat(result).isNotNull();
            assertThat(builder.lastCalledMethod).isEqualTo("lovelace");
        }
    }

    @Nested
    class EdgeCaseTests {

        @Test
        void shouldReturnFalseCondition_whenBothFieldsAreNull() {
            TestCurrencyConditionBuilder builder = new TestCurrencyConditionBuilder();
            Currency currency = Currency.builder().build();

            Condition result = builder.buildCurrencyCondition(currency);

            assertThat(result).isNotNull();
            assertThat(builder.lastCalledMethod).isNull();
        }

        @Test
        void shouldReturnFalseCondition_whenBothFieldsAreEmpty() {
            TestCurrencyConditionBuilder builder = new TestCurrencyConditionBuilder();
            Currency currency = Currency.builder()
                    .policyId("")
                    .symbol("")
                    .build();

            Condition result = builder.buildCurrencyCondition(currency);

            assertThat(result).isNotNull();
            assertThat(builder.lastCalledMethod).isNull();
        }
    }
}
