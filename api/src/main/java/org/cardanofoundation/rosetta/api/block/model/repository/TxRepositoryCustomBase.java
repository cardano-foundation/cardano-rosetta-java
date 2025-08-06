package org.cardanofoundation.rosetta.api.block.model.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.block.model.repository.util.TxRepositoryQueryBuilder;
import org.cardanofoundation.rosetta.api.search.model.Currency;
import org.cardanofoundation.rosetta.common.spring.OffsetBasedPageRequest;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.cardanofoundation.rosetta.api.jooq.Tables.*;

@Slf4j
@RequiredArgsConstructor
public abstract class TxRepositoryCustomBase implements TxRepositoryCustom {

    protected final DSLContext dsl;
    protected final TxRepositoryQueryBuilder queryBuilder;

    protected static final int LARGE_HASH_SET_THRESHOLD = 10000;

    protected abstract TxRepositoryQueryBuilder.CurrencyConditionBuilder getCurrencyConditionBuilder();
    
    /**
     * Base class for currency condition builders that handles common logic.
     */
    protected abstract static class BaseCurrencyConditionBuilder implements TxRepositoryQueryBuilder.CurrencyConditionBuilder {
        
        @Override
        public final Condition buildCurrencyCondition(Currency currency) {
            String policyId = currency.getPolicyId();
            String symbol = currency.getSymbol();

            if (policyId != null && !policyId.trim().isEmpty()) {
                String escapedPolicyId = policyId.trim().replace("\"", "\\\"");

                if (symbol != null && !symbol.trim().isEmpty() &&
                        !"lovelace".equalsIgnoreCase(symbol) && !"ada".equalsIgnoreCase(symbol)) {
                    String escapedSymbol = symbol.trim().replace("\"", "\\\"");
                    return buildPolicyIdAndSymbolCondition(escapedPolicyId, escapedSymbol);
                } else {
                    return buildPolicyIdOnlyCondition(escapedPolicyId);
                }
            }

            if (symbol != null && !symbol.trim().isEmpty()) {
                if ("lovelace".equalsIgnoreCase(symbol) || "ada".equalsIgnoreCase(symbol)) {
                    return buildLovelaceCondition();
                } else {
                    String escapedSymbol = symbol.trim().replace("\"", "\\\"");
                    return buildSymbolOnlyCondition(escapedSymbol);
                }
            }

            return DSL.falseCondition();
        }
        
        // Template methods for database-specific implementations
        protected abstract Condition buildPolicyIdAndSymbolCondition(String escapedPolicyId, String escapedSymbol);
        protected abstract Condition buildPolicyIdOnlyCondition(String escapedPolicyId);
        protected abstract Condition buildLovelaceCondition();
        protected abstract Condition buildSymbolOnlyCondition(String escapedSymbol);
    }
    
    /**
     * DB-specific implementation for large hash set AND operations.
     * PostgreSQL uses VALUES table, H2 uses batched IN clauses.
     */
    protected abstract Page<TxnEntity> searchTxnEntitiesANDWithLargeHashSet(Set<String> txHashes,
                                                                           @Nullable String blockHash,
                                                                           @Nullable Long blockNumber,
                                                                           @Nullable Long maxBlock,
                                                                           @Nullable Boolean isSuccess,
                                                                           @Nullable Currency currency,
                                                                           OffsetBasedPageRequest pageable);
    
    /**
     * DB-specific implementation for large hash set OR operations.
     * PostgreSQL uses VALUES table, H2 uses batched IN clauses.
     */
    protected abstract Page<TxnEntity> searchTxnEntitiesORWithLargeHashSet(Set<String> txHashes,
                                                                          @Nullable String blockHash,
                                                                          @Nullable Long blockNumber,
                                                                          @Nullable Long maxBlock,
                                                                          @Nullable Boolean isSuccess,
                                                                          @Nullable Currency currency,
                                                                          OffsetBasedPageRequest pageable);
    
    /**
     * Utility method for partitioning lists (used by H2 implementation).
     */
    protected static <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            int end = Math.min(i + batchSize, list.size());
            batches.add(list.subList(i, end));
        }
        return batches;
    }
    
    /**
     * Builds a base query with common JOINs based on the required conditions.
     */
    protected SelectJoinStep<?> buildBaseQueryWithJoins(@Nullable Boolean isSuccess, boolean needsCurrencyJoin) {
        var baseQuery = queryBuilder.buildTransactionSelectQuery(dsl);
        
        if (isSuccess != null) {
            baseQuery = baseQuery.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
        }
        
        if (needsCurrencyJoin) {
            baseQuery = baseQuery.leftJoin(ADDRESS_UTXO).on(TRANSACTION.TX_HASH.eq(ADDRESS_UTXO.TX_HASH));
        }
        
        return baseQuery
                .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                .leftJoin(TRANSACTION_SIZE).on(TRANSACTION.TX_HASH.eq(TRANSACTION_SIZE.TX_HASH));
    }
    
    /**
     * Builds a base query with COUNT and common JOINs (for H2 implementation using window functions).
     */
    protected SelectJoinStep<?> buildBaseQueryWithCountAndJoins(@Nullable Boolean isSuccess, boolean needsCurrencyJoin) {
        var baseQuery = queryBuilder.buildTransactionSelectQueryWithCount(dsl);
        
        if (isSuccess != null) {
            baseQuery = baseQuery.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
        }
        
        if (needsCurrencyJoin) {
            baseQuery = baseQuery.leftJoin(ADDRESS_UTXO).on(TRANSACTION.TX_HASH.eq(ADDRESS_UTXO.TX_HASH));
        }
        
        return baseQuery
                .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                .leftJoin(TRANSACTION_SIZE).on(TRANSACTION.TX_HASH.eq(TRANSACTION_SIZE.TX_HASH));
    }
    
    /**
     * Creates a page result using separate count and results queries (PostgreSQL approach).
     */
    protected Page<TxnEntity> createPageFromSeparateQueries(int totalCount, List<? extends org.jooq.Record> results, OffsetBasedPageRequest pageable) {
        return queryBuilder.createPageFromSeparateQueries(results, totalCount, pageable);
    }
    
    /**
     * Creates a page result using window function count (H2 approach).
     */
    protected Page<TxnEntity> createPageFromResultsWithCount(List<? extends org.jooq.Record> results, OffsetBasedPageRequest pageable) {
        return queryBuilder.createPageFromResultsWithCount(results, pageable);
    }

    @Override
    public List<TxnEntity> findTransactionsByBlockHash(String blockHash) {
        return dsl.select(
                        TRANSACTION.TX_HASH,
                        TRANSACTION.BLOCK_HASH,
                        TRANSACTION.INPUTS,
                        TRANSACTION.OUTPUTS,
                        TRANSACTION.FEE,
                        TRANSACTION.SLOT,
                        BLOCK.HASH.as("joined_block_hash"),
                        BLOCK.NUMBER.as("joined_block_number"),
                        BLOCK.SLOT.as("joined_block_slot"),
                        TRANSACTION_SIZE.SIZE.as("joined_tx_size"),
                        TRANSACTION_SIZE.SCRIPT_SIZE.as("joined_tx_script_size")
                )
                .from(TRANSACTION)
                .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                .leftJoin(TRANSACTION_SIZE).on(TRANSACTION.TX_HASH.eq(TRANSACTION_SIZE.TX_HASH))
                .where(TRANSACTION.BLOCK_HASH.eq(blockHash))
                .fetch(queryBuilder::mapRecordToTxnEntity);
    }

    @Override
    @Transactional
    public Page<TxnEntity> searchTxnEntitiesAND(Set<String> txHashes,
                                               @Nullable String blockHash,
                                               @Nullable Long blockNumber,
                                               @Nullable Long maxBlock,
                                               @Nullable Boolean isSuccess,
                                               @Nullable Currency currency,
                                               OffsetBasedPageRequest offsetBasedPageRequest) {
        
        if (txHashes.size() > LARGE_HASH_SET_THRESHOLD) {
            return searchTxnEntitiesANDWithLargeHashSet(txHashes, blockHash, blockNumber, maxBlock, isSuccess, currency, offsetBasedPageRequest);
        }

        // Build conditions
        Condition conditions = queryBuilder.buildAndConditions(txHashes, blockHash, blockNumber, maxBlock, isSuccess, currency, getCurrencyConditionBuilder());
        
        // Execute count query first (much faster without window function)
        int totalCount = executeCountQuery(conditions, isSuccess, blockHash != null || blockNumber != null || maxBlock != null, currency != null);
        
        // Execute results query (without COUNT(*) OVER())
        List<? extends org.jooq.Record> results = executeResultsQuery(conditions, isSuccess, currency != null, offsetBasedPageRequest);

        return queryBuilder.createPageFromSeparateQueries(results, totalCount, offsetBasedPageRequest);
    }

    @Override
    @Transactional
    public Page<TxnEntity> searchTxnEntitiesOR(Set<String> txHashes,
                                              @Nullable String blockHash,
                                              @Nullable Long blockNumber,
                                              @Nullable Long maxBlock,
                                              @Nullable Boolean isSuccess,
                                              @Nullable Currency currency,
                                              OffsetBasedPageRequest offsetBasedPageRequest) {
        if (txHashes.size() > LARGE_HASH_SET_THRESHOLD) {
            return searchTxnEntitiesORWithLargeHashSet(txHashes, blockHash, blockNumber, maxBlock, isSuccess, currency, offsetBasedPageRequest);
        }

        // Build OR conditions
        Condition orConditions = queryBuilder.buildOrConditions(txHashes, blockHash, blockNumber, maxBlock, isSuccess, currency, getCurrencyConditionBuilder());
        
        // Execute count query first (much faster without window function)
        int totalCount = executeOrCountQuery(orConditions, isSuccess, currency != null);
        
        // Execute results query (without COUNT(*) OVER())
        List<? extends org.jooq.Record> results = executeOrResultsQuery(orConditions, isSuccess, currency != null, offsetBasedPageRequest);

        return queryBuilder.createPageFromSeparateQueries(results, totalCount, offsetBasedPageRequest);
    }

    /**
     * Executes a separate count query for AND operations.
     * This is much faster than using COUNT(*) OVER() window function.
     * 
     * NOTE: Currency conditions use EXISTS subqueries - no JOIN needed!
     */
    protected int executeCountQuery(Condition conditions,
                                    @Nullable Boolean isSuccess,
                                    boolean needsBlockJoin,
                                    boolean needsCurrencyJoin) {
        var countQuery = queryBuilder.buildCountQueryWithJoins(dsl, needsBlockJoin, isSuccess != null);
        
        // Currency filtering uses EXISTS subqueries in the condition - no JOIN needed
        // Adding LEFT JOIN address_utxo would cause redundant table access and poor performance
        
        return countQuery.where(conditions).fetchOne(0, Integer.class);
    }

    /**
     * Executes a separate results query for AND operations.
     * This eliminates the need for window functions and subqueries.
     * 
     * NOTE: Currency conditions use EXISTS subqueries - no JOIN needed!
     */
    protected List<? extends org.jooq.Record> executeResultsQuery(Condition conditions, @Nullable Boolean isSuccess, boolean needsCurrencyJoin, OffsetBasedPageRequest offsetBasedPageRequest) {
        var baseQuery = queryBuilder.buildTransactionSelectQuery(dsl);
        
        if (isSuccess != null) {
            baseQuery = baseQuery.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
        }
        
        // Currency filtering uses EXISTS subqueries in the condition - no JOIN needed
        // Adding LEFT JOIN address_utxo would cause redundant table access and poor performance
        
        return baseQuery
                .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                .leftJoin(TRANSACTION_SIZE).on(TRANSACTION.TX_HASH.eq(TRANSACTION_SIZE.TX_HASH))
                .where(conditions)
                .orderBy(TRANSACTION.SLOT.desc())
                .limit(offsetBasedPageRequest.getLimit())
                .offset(offsetBasedPageRequest.getOffset())
                .fetch();
    }

    /**
     * Executes a separate count query for OR operations.
     * 
     * NOTE: Currency conditions use EXISTS subqueries - no JOIN needed!
     */
    protected int executeOrCountQuery(Condition conditions, @Nullable Boolean isSuccess, boolean needsCurrencyJoin) {
        var countQuery = queryBuilder.buildCountQueryWithJoins(dsl, true, isSuccess != null); // OR queries always need block join
        
        // Currency filtering uses EXISTS subqueries in the condition - no JOIN needed
        // Adding LEFT JOIN address_utxo would cause redundant table access and poor performance
        
        return countQuery.where(conditions).fetchOne(0, Integer.class);
    }

    /**
     * Executes a separate results query for OR operations.
     * 
     * NOTE: Currency conditions use EXISTS subqueries - no JOIN needed!
     */
    protected List<? extends org.jooq.Record> executeOrResultsQuery(Condition conditions, @Nullable Boolean isSuccess, boolean needsCurrencyJoin, OffsetBasedPageRequest offsetBasedPageRequest) {
        var baseQuery = queryBuilder.buildTransactionSelectQuery(dsl);
        
        if (isSuccess != null) {
            baseQuery = baseQuery.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
        }
        
        // Currency filtering uses EXISTS subqueries in the condition - no JOIN needed
        // Adding LEFT JOIN address_utxo would cause redundant table access and poor performance
        
        return baseQuery
                .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                .leftJoin(TRANSACTION_SIZE).on(TRANSACTION.TX_HASH.eq(TRANSACTION_SIZE.TX_HASH))
                .where(conditions)
                .orderBy(TRANSACTION.SLOT.desc())
                .limit(offsetBasedPageRequest.getLimit())
                .offset(offsetBasedPageRequest.getOffset())
                .fetch();
    }

}
