package org.cardanofoundation.rosetta.api.block.model.repository.postgresql;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.block.model.repository.TxRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.TxRepositoryCustomBase;
import org.cardanofoundation.rosetta.api.block.model.repository.util.TxRepositoryQueryBuilder;
import org.cardanofoundation.rosetta.api.search.model.Currency;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
                                                                  Pageable pageable) {
        if (txHashes == null || txHashes.isEmpty()) {
            return searchTxnEntitiesAND(null, blockHash, blockNumber, maxBlock, isSuccess, currency, pageable);
        }

        log.debug("Using PostgreSQL VALUES approach for AND search with {} transaction hashes", txHashes.size());

        // Create VALUES table with hash values
        Table<?> hashValues = createValuesTable(txHashes);

        // Build conditions without txHashes (since we'll use JOIN)
        Condition conditions = queryBuilder.buildAndConditions(null, blockHash, blockNumber, maxBlock, isSuccess, currency, getCurrencyConditionBuilder());

        // Build single query with COUNT using window function
        var baseQueryFrom = queryBuilder.buildTransactionSelectQueryWithCount(dsl)
                .join(hashValues).on(TRANSACTION.TX_HASH.eq(hashValues.field("hash", String.class)));

        if (isSuccess != null) {
            baseQueryFrom = baseQueryFrom.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
        }

        var queryWithCount = baseQueryFrom
                .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                .leftJoin(TRANSACTION_SIZE).on(TRANSACTION.TX_HASH.eq(TRANSACTION_SIZE.TX_HASH))
                .where(conditions)
                .orderBy(TRANSACTION.SLOT.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset());

        // Single database call - get both results and count
        List<? extends Record> results = queryWithCount.fetch();

        return queryBuilder.createPageFromResultsWithCount(results, pageable);
    }

    @Override
    @Transactional
    protected Page<TxnEntity> searchTxnEntitiesORWithLargeHashSet(Set<String> txHashes,
                                                                 @Nullable String blockHash,
                                                                 @Nullable Long blockNumber,
                                                                 @Nullable Long maxBlock,
                                                                 @Nullable Boolean isSuccess,
                                                                 @Nullable Currency currency,
                                                                 Pageable pageable) {
        if (txHashes == null || txHashes.isEmpty()) {
            return searchTxnEntitiesOR(null, blockHash, blockNumber, maxBlock, isSuccess, currency, pageable);
        }

        log.debug("Using PostgreSQL VALUES approach for OR search with {} transaction hashes", txHashes.size());

        // Create VALUES table with hash values
        Table<?> hashValues = createValuesTable(txHashes);

        // Build base query for hash matches with count
        var hashQueryFrom = queryBuilder.buildTransactionSelectQueryWithCount(dsl)
                .join(hashValues).on(TRANSACTION.TX_HASH.eq(hashValues.field("hash", String.class)));

        if (isSuccess != null) {
            hashQueryFrom = hashQueryFrom.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
        }

        // Apply success and currency filters if needed
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

        var hashQuery = hashQueryFrom
                .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                .leftJoin(TRANSACTION_SIZE).on(TRANSACTION.TX_HASH.eq(TRANSACTION_SIZE.TX_HASH))
                .leftJoin(ADDRESS_UTXO).on(TRANSACTION.TX_HASH.eq(ADDRESS_UTXO.TX_HASH))
                .where(hashConditions);

        // If we have other OR conditions, create a UNION
        if (blockHash != null || blockNumber != null || maxBlock != null) {
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
                    .limit(pageable.getPageSize())
                    .offset((int) pageable.getOffset())
                    .fetch(queryBuilder::mapRecordToTxnEntity);

            return new PageImpl<>(content, pageable, totalElements);
        }

        // Only hash query needed - use single query with count
        var queryWithCount = hashQuery
                .orderBy(TRANSACTION.SLOT.desc())
                .limit(pageable.getPageSize())
                .offset((int) pageable.getOffset());

        // Single database call - get both results and count
        List<? extends org.jooq.Record> results = queryWithCount.fetch();

        return queryBuilder.createPageFromResultsWithCount(results, pageable);
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


    private static class PostgreSQLCurrencyConditionBuilder implements TxRepositoryQueryBuilder.CurrencyConditionBuilder {

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
                            "AND au.amounts::jsonb @> '[{\"policy_id\": \"" + escapedPolicyId + "\", \"asset_name\": \"" + escapedSymbol + "\"}]')");
                } else {
                    return DSL.condition("EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                            "AND au.amounts::jsonb @> '[{\"policy_id\": \"" + escapedPolicyId + "\"}]')");
                }
            }

            if (symbol != null && !symbol.trim().isEmpty()) {
                if ("lovelace".equalsIgnoreCase(symbol) || "ada".equalsIgnoreCase(symbol)) {
                    return DSL.condition("EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                            "AND au.amounts::jsonb @> '[{\"unit\": \"lovelace\"}]')");
                } else {
                    String escapedSymbol = symbol.trim().replace("\"", "\\\"");
                    return DSL.condition("EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                            "AND au.amounts::jsonb @> '[{\"asset_name\": \"" + escapedSymbol + "\"}]')");
                }
            }

            return DSL.falseCondition();
        }
    }
}