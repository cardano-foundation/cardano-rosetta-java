package org.cardanofoundation.rosetta.api.block.model.repository.postgresql;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.block.model.repository.TxRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.TxRepositoryCustomBase;
import org.cardanofoundation.rosetta.api.block.model.repository.util.TxRepositoryQueryBuilder;
import org.cardanofoundation.rosetta.api.search.model.Currency;
import org.cardanofoundation.rosetta.common.spring.OffsetBasedPageRequest;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

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

    @Override
    @Transactional
    protected Page<TxnEntity> searchTxnEntitiesANDWithLargeHashSet(Set<String> txHashes,
                                                                  @Nullable String blockHash,
                                                                  @Nullable Long blockNumber,
                                                                  @Nullable Long maxBlock,
                                                                  @Nullable Boolean isSuccess,
                                                                  @Nullable Currency currency,
                                                                  OffsetBasedPageRequest offsetBasedPageRequest) {
        if (txHashes == null || txHashes.isEmpty()) {
            return searchTxnEntitiesAND(txHashes, blockHash, blockNumber, maxBlock, isSuccess, currency, offsetBasedPageRequest);
        }

        log.debug("Using PostgreSQL VALUES approach for AND search with {} transaction hashes", txHashes.size());

        // Create VALUES table with hash values
        Table<?> hashValues = createValuesTable(txHashes);

        // Build base conditions without txHashes
        Condition baseConditions = queryBuilder.buildAndConditions(null, blockHash, blockNumber, maxBlock, isSuccess, currency, getCurrencyConditionBuilder());
        
        boolean needsBlockJoin = blockHash != null || blockNumber != null || maxBlock != null;
        
        // Execute count query first (much faster without window function)
        int totalCount = executeCountQueryWithValues(hashValues, baseConditions, isSuccess, needsBlockJoin);
        
        // Execute results query (without COUNT(*) OVER())
        List<? extends org.jooq.Record> results = executeResultsQueryWithValues(hashValues, baseConditions, isSuccess, offsetBasedPageRequest);

        return createPageFromSeparateQueries(totalCount, results, offsetBasedPageRequest);
    }

    @Override
    @Transactional
    protected Page<TxnEntity> searchTxnEntitiesORWithLargeHashSet(Set<String> txHashes,
                                                                 @Nullable String blockHash,
                                                                 @Nullable Long blockNumber,
                                                                 @Nullable Long maxBlock,
                                                                 @Nullable Boolean isSuccess,
                                                                 @Nullable Currency currency,
                                                                 OffsetBasedPageRequest offsetBasedPageRequest) {
        if (txHashes.isEmpty()) {
            return searchTxnEntitiesOR(txHashes, blockHash, blockNumber, maxBlock, isSuccess, currency, offsetBasedPageRequest);
        }

        log.debug("Using PostgreSQL VALUES approach for OR search with {} transaction hashes", txHashes.size());

        // Create VALUES table with hash values
        Table<?> hashValues = createValuesTable(txHashes);

        // Build base conditions for hash filtering
        Condition hashConditions = DSL.trueCondition();
        if (isSuccess != null) {
            Condition successCondition = isSuccess
                    ? INVALID_TRANSACTION.TX_HASH.isNull()
                    : INVALID_TRANSACTION.TX_HASH.isNotNull();
            hashConditions = hashConditions.and(successCondition);
        }
        if (currency != null) {
            hashConditions = hashConditions.and(getCurrencyConditionBuilder().buildCurrencyCondition(currency));
        }

        // If we have other OR conditions, fall back to the original approach for now
        // TODO: Optimize complex OR + hash set cases later to avoid window functions
        if (blockHash != null || blockNumber != null || maxBlock != null) {
            log.warn("Using legacy window function approach for complex OR query with {} hashes - consider optimizing", txHashes.size());
            return handleComplexORLegacy(hashValues, hashConditions, blockHash, blockNumber, maxBlock, isSuccess, currency, offsetBasedPageRequest);
        }

        // Simple case: only hash filtering - use separate count and results queries
        // Execute count query first
        int totalCount = executeCountQueryWithValues(hashValues, hashConditions, isSuccess, false); // No block join for simple hash filtering
        
        // Execute results query 
        List<? extends org.jooq.Record> results = executeResultsQueryWithValues(hashValues, hashConditions, isSuccess, offsetBasedPageRequest);

        return createPageFromSeparateQueries(totalCount, results, offsetBasedPageRequest);
    }
    
    /**
     * Legacy approach for complex OR queries with hash filtering.
     * Uses window functions temporarily until complex UNION logic is optimized.
     */
    private Page<TxnEntity> handleComplexORLegacy(Table<?> hashValues,
                                                  Condition hashConditions,
                                                  @Nullable String blockHash,
                                                  @Nullable Long blockNumber, @Nullable Long maxBlock,
                                                  @Nullable Boolean isSuccess, @Nullable Currency currency,
                                                OffsetBasedPageRequest offsetBasedPageRequest) {
        // Simplified legacy approach - use existing working logic with window functions
        // TODO: Replace with proper separate query optimization later

        // Build base query for hash matches with count
        var hashQueryFrom = queryBuilder.buildTransactionSelectQueryWithCount(dsl)
                .join(hashValues).on(TRANSACTION.TX_HASH.eq(hashValues.field("hash", String.class)));

        if (isSuccess != null) {
            hashQueryFrom = hashQueryFrom.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
        }

        var hashQuery = hashQueryFrom
                .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                .leftJoin(TRANSACTION_SIZE).on(TRANSACTION.TX_HASH.eq(TRANSACTION_SIZE.TX_HASH))
                .leftJoin(ADDRESS_UTXO).on(TRANSACTION.TX_HASH.eq(ADDRESS_UTXO.TX_HASH))
                .where(hashConditions);

        // Build the other OR conditions
        Condition otherOrConditions = queryBuilder.buildOrConditions(null, blockHash, blockNumber, maxBlock, isSuccess, currency, getCurrencyConditionBuilder());
        
        var otherQueryFrom = queryBuilder.buildTransactionSelectQueryWithCount(dsl);

        if (isSuccess != null) {
            otherQueryFrom = otherQueryFrom.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
        }

        var otherQuery = otherQueryFrom
                .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                .leftJoin(TRANSACTION_SIZE).on(TRANSACTION.TX_HASH.eq(TRANSACTION_SIZE.TX_HASH))
                .leftJoin(ADDRESS_UTXO).on(TRANSACTION.TX_HASH.eq(ADDRESS_UTXO.TX_HASH))
                .where(otherOrConditions);

        // Combine with UNION
        var unionQuery = hashQuery.union(otherQuery)
                .orderBy(DSL.field("slot").desc());

        // Count query
        var countQuery = dsl.selectCount()
                .from(DSL.table("({0}) as union_result", unionQuery));

        Integer totalElements = countQuery.fetchOne(0, Integer.class);

        List<TxnEntity> content = unionQuery
                .limit(offsetBasedPageRequest.getLimit())
                .offset((int) offsetBasedPageRequest.getOffset())
                .fetch(queryBuilder::mapRecordToTxnEntity);

        return new PageImpl<>(content, offsetBasedPageRequest, totalElements);
    }

    /**
     * Creates a VALUES table for efficient JOIN operations with large hash sets.
     * This approach is more performant than using ANY(array) for large datasets.
     * 
     * @param txHashes Set of transaction hashes
     * @return Table representing the VALUES clause
     */
    private Table<?> createValuesTable(Set<String> txHashes) {
        if (txHashes.isEmpty()) {
            throw new IllegalArgumentException("Hash set cannot be empty");
        }

        // Build VALUES rows
        Row1<String>[] rows = txHashes.stream()
                .map(DSL::row)
                .toArray(Row1[]::new);

        // Create VALUES table with alias
        return DSL.values(rows).as("hash_values", "hash");
    }
    
    /**
     * Executes a count query using VALUES table for efficient hash filtering.
     * Uses the same JOIN logic as base class but adds VALUES JOIN.
     */
    private int executeCountQueryWithValues(Table<?> hashValues, Condition baseConditions, @Nullable Boolean isSuccess, boolean needsBlockJoin) {
        var countQuery = buildBaseCountQuery(isSuccess, needsBlockJoin)
                .join(hashValues).on(TRANSACTION.TX_HASH.eq(hashValues.field("hash", String.class)));

        return countQuery.where(baseConditions).fetchOne(0, Integer.class);
    }
    
    /**
     * Executes a results query using VALUES table for efficient hash filtering.
     * Uses the same JOIN logic as base class but adds VALUES JOIN.
     */
    private List<? extends org.jooq.Record> executeResultsQueryWithValues(Table<?> hashValues, Condition baseConditions, @Nullable Boolean isSuccess, OffsetBasedPageRequest offsetBasedPageRequest) {
        var baseQuery = buildBaseResultsQuery(isSuccess)
                .join(hashValues).on(TRANSACTION.TX_HASH.eq(hashValues.field("hash", String.class)));
        
        return baseQuery
                .where(baseConditions)
                .orderBy(TRANSACTION.SLOT.desc())
                .limit(offsetBasedPageRequest.getLimit())
                .offset(offsetBasedPageRequest.getOffset())
                .fetch();
    }

    /**
     * PostgreSQL-specific currency condition builder using JSONB @> operator.
     */
    private static class PostgreSQLCurrencyConditionBuilder extends BaseCurrencyConditionBuilder {
        
        @Override
        protected Condition buildPolicyIdAndSymbolCondition(String escapedPolicyId, String escapedSymbol) {
            return DSL.condition("EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                    "AND au.amounts::jsonb @> '[{\"policy_id\": \"" + escapedPolicyId + "\", \"asset_name\": \"" + escapedSymbol + "\"}]')");
        }

        @Override
        protected Condition buildPolicyIdOnlyCondition(String escapedPolicyId) {
            return DSL.condition("EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                    "AND au.amounts::jsonb @> '[{\"policy_id\": \"" + escapedPolicyId + "\"}]')");
        }

        @Override
        protected Condition buildLovelaceCondition() {
            return DSL.condition("EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                    "AND au.amounts::jsonb @> '[{\"unit\": \"lovelace\"}]')");
        }

        @Override
        protected Condition buildSymbolOnlyCondition(String escapedSymbol) {
            return DSL.condition("EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                    "AND au.amounts::jsonb @> '[{\"asset_name\": \"" + escapedSymbol + "\"}]')");
        }
    }

}