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
     * Builds a base query with common JOINs.
     * Currency filtering uses EXISTS subqueries, so no currency JOIN is ever needed.
     */
    protected SelectJoinStep<?> buildBaseResultsQuery(@Nullable Boolean isSuccess) {
        var baseQuery = queryBuilder.buildTransactionSelectQuery(dsl);
        
        if (isSuccess != null) {
            baseQuery = baseQuery.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
        }
        
        return baseQuery
                .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                .leftJoin(TRANSACTION_SIZE).on(TRANSACTION.TX_HASH.eq(TRANSACTION_SIZE.TX_HASH));
    }
    
    /**
     * Builds a count query with necessary JOINs based on filtering requirements.
     * Currency filtering uses EXISTS subqueries, so no currency JOIN is ever needed.
     */
    protected SelectJoinStep<org.jooq.Record1<Integer>> buildBaseCountQuery(@Nullable Boolean isSuccess, boolean needsBlockJoin) {
        var countQuery = dsl.selectCount().from(TRANSACTION);
        
        if (needsBlockJoin) {
            countQuery = countQuery.leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH));
        }
        
        if (isSuccess != null) {
            countQuery = countQuery.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
        }
        
        return countQuery;
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
        
        boolean needsBlockJoin = blockHash != null || blockNumber != null || maxBlock != null;
        
        // Execute count query first (much faster without window function)
        int totalCount = executeCountQuery(conditions, isSuccess, needsBlockJoin);
        
        // Execute results query (without COUNT(*) OVER())
        List<? extends org.jooq.Record> results = executeResultsQuery(conditions, isSuccess, offsetBasedPageRequest);

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
        
        // OR queries always need block join due to OR conditions on block fields
        boolean needsBlockJoin = true;
        
        // Execute count query first (much faster without window function)
        int totalCount = executeCountQuery(orConditions, isSuccess, needsBlockJoin);
        
        // Execute results query (without COUNT(*) OVER())
        List<? extends org.jooq.Record> results = executeResultsQuery(orConditions, isSuccess, offsetBasedPageRequest);

        return queryBuilder.createPageFromSeparateQueries(results, totalCount, offsetBasedPageRequest);
    }

    /**
     * Generic method to execute a count query with proper JOINs.
     * Ensures count and results queries use identical conditions and JOINs.
     * Currency conditions use EXISTS subqueries - no JOIN needed.
     */
    protected int executeCountQuery(Condition conditions, @Nullable Boolean isSuccess, boolean needsBlockJoin) {
        return buildBaseCountQuery(isSuccess, needsBlockJoin)
                .where(conditions)
                .fetchOne(0, Integer.class);
    }

    /**
     * Generic method to execute a results query with proper JOINs and pagination.
     * Ensures count and results queries use identical conditions and JOINs.
     * Currency conditions use EXISTS subqueries - no JOIN needed.
     */
    protected List<? extends org.jooq.Record> executeResultsQuery(Condition conditions, 
                                                                  @Nullable Boolean isSuccess, 
                                                                  OffsetBasedPageRequest offsetBasedPageRequest) {
        return buildBaseResultsQuery(isSuccess)
                .where(conditions)
                .orderBy(TRANSACTION.SLOT.desc())
                .limit(offsetBasedPageRequest.getLimit())
                .offset(offsetBasedPageRequest.getOffset())
                .fetch();
    }

}
