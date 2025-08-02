package org.cardanofoundation.rosetta.api.block.model.repository.h2;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.block.model.repository.TxRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.TxRepositoryCustomBase;
import org.cardanofoundation.rosetta.api.block.model.repository.util.TxHashTempTableManager;
import org.cardanofoundation.rosetta.api.block.model.repository.util.TxRepositoryQueryBuilder;
import org.cardanofoundation.rosetta.api.search.model.Currency;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository("txRepository")
@Profile({"h2", "test-integration"})
public class TxRepositoryH2Impl extends TxRepositoryCustomBase implements TxRepository {

    private final H2CurrencyConditionBuilder currencyConditionBuilder;

    public TxRepositoryH2Impl(DSLContext dsl, 
                             TxRepositoryQueryBuilder queryBuilder,
                             TxHashTempTableManager tempTableManager) {
        super(dsl, queryBuilder, tempTableManager);
        this.currencyConditionBuilder = new H2CurrencyConditionBuilder();
    }

    @Override
    protected TxRepositoryQueryBuilder.CurrencyConditionBuilder getCurrencyConditionBuilder() {
        return currencyConditionBuilder;
    }

    private static class H2CurrencyConditionBuilder implements TxRepositoryQueryBuilder.CurrencyConditionBuilder {
        @Override
        public Condition buildCurrencyCondition(Currency currency) {
            String policyId = currency.getPolicyId();
            String symbol = currency.getSymbol();

            if (policyId != null && !policyId.trim().isEmpty()) {
                String escapedPolicyId = policyId.trim().replace("\"", "\\\"");

                if (symbol != null && !symbol.trim().isEmpty() &&
                        !"lovelace".equalsIgnoreCase(symbol) && !"ada".equalsIgnoreCase(symbol)) {
                    String escapedSymbol = symbol.trim().replace("\"", "\\\"");

                    return DSL.condition("EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                            "AND au.amounts LIKE '%\"policy_id\":\"" + escapedPolicyId + "\"%' " +
                            "AND au.amounts LIKE '%\"asset_name\":\"" + escapedSymbol + "\"%')");
                } else {
                    return DSL.condition("EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                            "AND au.amounts LIKE '%\"policy_id\":\"" + escapedPolicyId + "\"%')");
                }
            }

            if (symbol != null && !symbol.trim().isEmpty()) {
                if ("lovelace".equalsIgnoreCase(symbol) || "ada".equalsIgnoreCase(symbol)) {
                    return DSL.condition("EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                            "AND au.amounts LIKE '%\"unit\":\"lovelace\"%')");
                } else {
                    String escapedSymbol = symbol.trim().replace("\"", "\\\"");
                    return DSL.condition("EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                            "AND au.amounts LIKE '%\"asset_name\":\"" + escapedSymbol + "\"%')");
                }
            }

            return DSL.falseCondition();
        }
    }
}