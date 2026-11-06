package org.cardanofoundation.rosetta.api.block.model.repository.postgresql;

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.jooq.*;
import org.jooq.impl.DSL;

import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.block.model.repository.TxRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.TxRepositoryCustomBase;
import org.cardanofoundation.rosetta.api.block.model.repository.util.TxRepositoryQueryBuilder;
import org.cardanofoundation.rosetta.api.search.model.Currency;
import org.cardanofoundation.rosetta.common.spring.OffsetBasedPageRequest;

import static org.cardanofoundation.rosetta.api.jooq.Tables.*;

@Slf4j
@Repository("txRepository")
@Profile({"!h2 & !test-integration"})
public class TxRepositoryPostgreSQLImpl extends TxRepositoryCustomBase implements TxRepository {

    private final PostgreSQLCurrencyConditionBuilder currencyConditionBuilder;

    public TxRepositoryPostgreSQLImpl(DSLContext dsl,
                                     TxRepositoryQueryBuilder queryBuilder) {
        super(dsl, queryBuilder);
        this.currencyConditionBuilder = new PostgreSQLCurrencyConditionBuilder();
    }

    @Override
    protected TxRepositoryQueryBuilder.CurrencyConditionBuilder getCurrencyConditionBuilder() {
        return currencyConditionBuilder;
    }

    /**
     * PostgreSQL always uses VALUES table approach for all hash set sizes.
     * This provides consistent performance and eliminates threshold complexity.
     */
    @Override
    @Transactional
    public Page<TxnEntity> searchTxnEntitiesAND(Set<String> plainTxHashes,
                                               Set<String> addressHashes,
                                               @Nullable String blockHash,
                                               @Nullable Long blockNumber,
                                               @Nullable Long maxBlock,
                                               @Nullable Boolean isSuccess,
                                               @Nullable Currency currency,
                                               OffsetBasedPageRequest offsetBasedPageRequest) {

        log.debug("Using PostgreSQL VALUES approach for AND search with {} tx hashes and {} address hashes",
                 plainTxHashes != null ? plainTxHashes.size() : 0,
                 addressHashes != null ? addressHashes.size() : 0);

        // Build base conditions without hash filters
        Condition baseConditions = queryBuilder.buildAndConditions(null, null, blockHash, blockNumber, maxBlock, isSuccess, currency, getCurrencyConditionBuilder());

        // Add plainTxHashes condition if present using VALUES table
        if (plainTxHashes != null && !plainTxHashes.isEmpty()) {
            baseConditions = baseConditions.and(createHashCondition(plainTxHashes));
        }

        // Add addressHashes condition if present using VALUES table
        if (addressHashes != null && !addressHashes.isEmpty()) {
            baseConditions = baseConditions.and(createHashCondition(addressHashes));
        }

        // Execute separate count and results queries for optimal performance
        int totalCount = executeCountQuery(baseConditions, isSuccess);
        List<? extends org.jooq.Record> results = currency != null
                ? executeIndexFirstResultsQuery(baseConditions, isSuccess, offsetBasedPageRequest)
                : executeResultsQuery(baseConditions, isSuccess, offsetBasedPageRequest);

        return createPageFromSeparateQueries(totalCount, results, offsetBasedPageRequest);
    }

    /**
     * Results query for an AND search with a currency filter: first finds the matching transactions
     * through the token index, then sorts and pages them, and only then joins {@code block} and
     * {@code transaction_size} for the page.
     *
     * <p>With the generic query ({@link #executeResultsQuery}), the {@code ORDER BY slot DESC ... LIMIT}
     * leads PostgreSQL to walk {@code transaction} newest-first and evaluate the currency {@code EXISTS}
     * (a GIN bitmap scan on {@code address_utxo.amounts}) for every row until the page is full. For a
     * token whose transactions are not among the newest ones this takes minutes (preprod tGENS: no
     * result within 60 s). Here the filter runs in a {@code MATERIALIZED} CTE without {@code LIMIT}, so
     * it is driven by the GIN index (preprod tGENS: 28,223 transactions, page in ~430 ms), the page is a
     * top-N sort over the matches, and the joins only touch the rows of the page.</p>
     *
     * <p>Only used when a currency filter is present: without one, the matching set can be the whole
     * table, and the generic query's index walk is the right plan.</p>
     */
    private List<? extends org.jooq.Record> executeIndexFirstResultsQuery(Condition conditions,
                                                                          @Nullable Boolean isSuccess,
                                                                          OffsetBasedPageRequest offsetBasedPageRequest) {
        SelectOnConditionStep<Record3<String, Long, Integer>> filter = dsl
                .select(TRANSACTION.TX_HASH, TRANSACTION.SLOT, TRANSACTION.TX_INDEX)
                .from(TRANSACTION)
                .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH));
        if (isSuccess != null) {
            filter = filter.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
        }
        CommonTableExpression<Record3<String, Long, Integer>> matching = DSL.name("matching_tx")
                .fields("tx_hash", "slot", "tx_index")
                .asMaterialized(filter.where(conditions));

        Field<String> matchingTxHash = matching.field("tx_hash", String.class);
        Field<Long> matchingSlot = matching.field("slot", Long.class);
        Field<Integer> matchingTxIndex = matching.field("tx_index", Integer.class);
        CommonTableExpression<Record3<String, Long, Integer>> page = DSL.name("page_tx")
                .fields("tx_hash", "slot", "tx_index")
                .as(dsl.select(matchingTxHash, matchingSlot, matchingTxIndex)
                        .from(matching)
                        .orderBy(matchingSlot.desc(), matchingTxIndex.desc())
                        .limit(offsetBasedPageRequest.getLimit())
                        .offset(offsetBasedPageRequest.getOffset()));

        Field<String> pageTxHash = page.field("tx_hash", String.class);
        return dsl.with(matching).with(page)
                .select(queryBuilder.transactionSelectFields())
                .from(page)
                .join(TRANSACTION).on(TRANSACTION.TX_HASH.eq(pageTxHash))
                .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                .leftJoin(TRANSACTION_SIZE).on(TRANSACTION.TX_HASH.eq(TRANSACTION_SIZE.TX_HASH))
                .orderBy(page.field("slot", Long.class).desc(), page.field("tx_index", Integer.class).desc())
                .fetch();
    }

    @Override
    @Transactional
    public Page<TxnEntity> searchTxnEntitiesOR(Set<String> plainTxHashes,
                                              Set<String> addressHashes,
                                              @Nullable String blockHash,
                                              @Nullable Long blockNumber,
                                              @Nullable Long maxBlock,
                                              @Nullable Boolean isSuccess,
                                              @Nullable Currency currency,
                                              OffsetBasedPageRequest offsetBasedPageRequest) {

        log.debug("Using PostgreSQL VALUES approach for OR search with {} tx hashes and {} address hashes",
                 plainTxHashes != null ? plainTxHashes.size() : 0,
                 addressHashes != null ? addressHashes.size() : 0);

        // Start with null and build OR conditions properly
        Condition orConditions = null;

        // Add plainTxHashes as OR condition if present using VALUES table
        if (plainTxHashes != null && !plainTxHashes.isEmpty()) {
            orConditions = createHashCondition(plainTxHashes);
        }

        // Add addressHashes as OR condition if present using VALUES table
        if (addressHashes != null && !addressHashes.isEmpty()) {
            Condition addressCondition = createHashCondition(addressHashes);
            orConditions = orConditions == null ? addressCondition : orConditions.or(addressCondition);
        }

        // Add other OR conditions (blockHash, blockNumber, maxBlock, currency)
        if (blockHash != null) {
            Condition blockHashCondition = BLOCK.HASH.eq(blockHash);
            orConditions = orConditions == null ? blockHashCondition : orConditions.or(blockHashCondition);
        }

        if (blockNumber != null) {
            Condition blockNumberCondition = BLOCK.NUMBER.eq(blockNumber);
            orConditions = orConditions == null ? blockNumberCondition : orConditions.or(blockNumberCondition);
        }

        if (maxBlock != null) {
            Condition maxBlockCondition = BLOCK.NUMBER.le(maxBlock);
            orConditions = orConditions == null ? maxBlockCondition : orConditions.or(maxBlockCondition);
        }

        if (currency != null) {
            Condition currencyCondition = getCurrencyConditionBuilder().buildCurrencyCondition(currency);
            orConditions = orConditions == null ? currencyCondition : orConditions.or(currencyCondition);
        }

        // If no OR conditions, default to true
        if (orConditions == null) {
            orConditions = DSL.trueCondition();
        }

        // Success condition should be ANDed with the result of all OR conditions
        if (isSuccess != null) {
            Condition successCondition = isSuccess
                    ? INVALID_TRANSACTION.TX_HASH.isNull()
                    : INVALID_TRANSACTION.TX_HASH.isNotNull();
            orConditions = orConditions.and(successCondition);
        }

        // Execute separate count and results queries for optimal performance
        int totalCount = executeCountQuery(orConditions, isSuccess);
        List<? extends org.jooq.Record> results = executeResultsQuery(orConditions, isSuccess, offsetBasedPageRequest);

        return createPageFromSeparateQueries(totalCount, results, offsetBasedPageRequest);
    }
    /**
     * Creates a hash condition using VALUES table approach.
     * Used for all hash set sizes in PostgreSQL for consistency and performance.
     *
     * @param hashes Set of transaction hashes
     * @return Condition using EXISTS with VALUES table
     */
    private Condition createHashCondition(Set<String> hashes) {
        if (hashes == null || hashes.isEmpty()) {
            return DSL.trueCondition();
        }

        Table<?> valuesTable = createValuesTable(hashes);
        return DSL.exists(
            DSL.select(DSL.one())
               .from(valuesTable)
               .where(TRANSACTION.TX_HASH.eq(valuesTable.field("hash", String.class)))
        );
    }

    /**
     * Creates a VALUES table for efficient operations with any hash set size.
     * This approach provides consistent performance characteristics.
     *
     * @param hashes Set of transaction hashes
     * @return Table representing the VALUES clause
     */
    private Table<?> createValuesTable(Set<String> hashes) {
        if (hashes.isEmpty()) {
            throw new IllegalArgumentException("Hash set cannot be empty");
        }

        // Build VALUES rows
        Row1<String>[] rows = hashes.stream()
                .map(DSL::row)
                .toArray(Row1[]::new);

        // Create VALUES table with alias
        return DSL.values(rows).as("hash_values", "hash");
    }

    /**
     * PostgreSQL-specific currency condition builder using JSONB @> operator.
     */
    private static class PostgreSQLCurrencyConditionBuilder extends BaseCurrencyConditionBuilder {

        @Override
        protected Condition buildPolicyIdAndSymbolCondition(String validatedPolicyId, String validatedSymbol) {
            // unit = policyId + symbol where symbol is hex-encoded asset name
            String expectedUnit = validatedPolicyId + validatedSymbol;
            return DSL.condition(
                    "EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                    "AND au.amounts::jsonb @> jsonb_build_array(jsonb_build_object('unit', ?)))",
                    DSL.val(expectedUnit));
        }

        @Override
        protected Condition buildPolicyIdOnlyCondition(String validatedPolicyId) {
            return DSL.condition(
                    "EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                    "AND au.amounts::jsonb @> jsonb_build_array(jsonb_build_object('policy_id', ?)))",
                    DSL.val(validatedPolicyId));
        }

        @Override
        protected Condition buildLovelaceCondition() {
            return DSL.condition("EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                    "AND au.amounts::jsonb @> '[{\"unit\": \"lovelace\"}]')");
        }

        @Override
        protected Condition buildSymbolOnlyCondition(String validatedSymbol) {
            // Search for unit field ending with the hex-encoded symbol
            // Since unit = policyId + symbol, we look for units that end with the symbol
            return DSL.condition(
                    "EXISTS (SELECT 1 FROM address_utxo au, " +
                    "jsonb_array_elements(au.amounts::jsonb) AS amt " +
                    "WHERE au.tx_hash = transaction.tx_hash " +
                    "AND amt->>'unit' LIKE ? " +
                    "AND amt->>'unit' != 'lovelace')",
                    DSL.val("%" + validatedSymbol));
        }
    }

}
