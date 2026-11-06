package org.cardanofoundation.rosetta.api.block.model.repository.postgresql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.block.model.repository.util.TxRepositoryQueryBuilder;
import org.cardanofoundation.rosetta.api.search.model.Currency;
import org.cardanofoundation.rosetta.common.spring.SimpleOffsetBasedPageRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the SQL that {@link TxRepositoryPostgreSQLImpl} sends for transaction searches. The
 * PostgreSQL implementation is not covered by the H2-based repository integration tests, so the
 * statements are captured with a jOOQ {@link MockDataProvider} in the PostgreSQL dialect.
 */
class TxRepositoryPostgreSQLImplCurrencySearchTest {

    private static final String POLICY_ID = "c6e65ba7878b2f8ea0ad39287d3e2fd256dc5c4160fc19bdf4c4d87e";
    private static final String SYMBOL = "7447454e53";
    private static final Currency TOKEN = Currency.builder().policyId(POLICY_ID).symbol(SYMBOL).decimals(6).build();

    private final List<String> statements = new ArrayList<>();
    private final List<List<Object>> bindings = new ArrayList<>();
    private TxRepositoryPostgreSQLImpl repository;

    @BeforeEach
    void setUp() {
        DSLContext create = DSL.using(SQLDialect.POSTGRES);
        MockDataProvider provider = ctx -> {
            statements.add(normalise(ctx.sql()));
            bindings.add(Arrays.asList(ctx.bindings()));
            if (ctx.sql().toLowerCase().startsWith("select count")) {
                Field<Integer> count = DSL.field("count", Integer.class);
                Result<Record1<Integer>> result = create.newResult(count);
                result.add(create.newRecord(count).values(0));
                return new MockResult[] {new MockResult(1, result)};
            }
            return new MockResult[] {new MockResult(0, create.newResult())};
        };
        DSLContext dsl = DSL.using(new MockConnection(provider), SQLDialect.POSTGRES);
        repository = new TxRepositoryPostgreSQLImpl(dsl, new TxRepositoryQueryBuilder(new ObjectMapper()));
    }

    /**
     * Lower case, no quotes and no schema prefix (the generated jOOQ classes carry the schema they were
     * generated from, which is mapped at runtime), so assertions don't depend on rendering settings.
     */
    private static String normalise(String sql) {
        return sql.toLowerCase().replace("\"", "").replaceAll("\\b(public|preprod)\\.", "");
    }

    private String resultsQuery() {
        assertThat(statements).hasSize(2); // count query, then results query
        return statements.get(1);
    }

    @Nested
    @DisplayName("AND search with a currency filter")
    class CurrencyFilter {

        @Test
        @DisplayName("finds the matching transactions in a MATERIALIZED CTE without LIMIT")
        void matchesInMaterializedCteWithoutLimit() {
            repository.searchTxnEntitiesAND(Set.of(), Set.of(), null, null, null, null, TOKEN,
                    new SimpleOffsetBasedPageRequest(0, 5));

            String sql = resultsQuery();
            assertThat(sql).startsWith("with matching_tx(tx_hash, slot, tx_index) as materialized (");
            String matching = sql.substring(0, sql.indexOf("page_tx("));
            assertThat(matching)
                    .contains("exists (select 1 from address_utxo au where au.tx_hash = transaction.tx_hash")
                    .contains("au.amounts::jsonb @> jsonb_build_array(jsonb_build_object('unit', ?))")
                    .doesNotContain("limit")
                    .doesNotContain("fetch next")
                    .doesNotContain("offset");
        }

        @Test
        @DisplayName("sorts and pages the matches, then joins block and transaction_size for the page only")
        void pagesMatchesThenJoins() {
            repository.searchTxnEntitiesAND(Set.of(), Set.of(), null, null, null, null, TOKEN,
                    new SimpleOffsetBasedPageRequest(10, 5));

            String sql = resultsQuery();
            String page = sql.substring(sql.indexOf("page_tx("), sql.indexOf(") select "));
            assertThat(page)
                    .contains("from matching_tx order by matching_tx.slot desc, matching_tx.tx_index desc")
                    .containsAnyOf("offset ? rows fetch next ? rows only", "limit ? offset ?");
            String outer = sql.substring(sql.indexOf(") select ") + 1);
            assertThat(outer)
                    .contains("from page_tx join transaction on transaction.tx_hash = page_tx.tx_hash")
                    .contains("left outer join block on transaction.block_hash = block.hash")
                    .contains("left outer join transaction_size on transaction.tx_hash = transaction_size.tx_hash")
                    .endsWith("order by page_tx.slot desc, page_tx.tx_index desc");

            assertThat(bindings.get(1)).contains(POLICY_ID + SYMBOL, 10L, 5L);
        }

        @Test
        @DisplayName("applies the success filter inside the CTE")
        void successFilterInsideCte() {
            repository.searchTxnEntitiesAND(Set.of(), Set.of(), null, null, null, true, TOKEN,
                    new SimpleOffsetBasedPageRequest(0, 5));

            String matching = resultsQuery().substring(0, resultsQuery().indexOf("page_tx("));
            assertThat(matching)
                    .contains("left outer join invalid_transaction on transaction.tx_hash = invalid_transaction.tx_hash")
                    .contains("invalid_transaction.tx_hash is null");
        }
    }

    @Nested
    @DisplayName("AND search without a currency filter")
    class NoCurrencyFilter {

        @Test
        @DisplayName("keeps the generic query: ordered index walk with LIMIT, no CTE")
        void keepsGenericQuery() {
            repository.searchTxnEntitiesAND(Set.of(), Set.of(), null, null, 100L, null, null,
                    new SimpleOffsetBasedPageRequest(0, 5));

            assertThat(resultsQuery())
                    .doesNotContain("matching_tx")
                    .startsWith("select transaction.tx_hash")
                    .contains("order by transaction.slot desc, transaction.tx_index desc");
        }
    }
}
