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
                }

                return buildPolicyIdOnlyCondition(escapedPolicyId);
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
     * Builds a base query with common JOINs.
     * Currency filtering uses EXISTS subqueries, so no currency JOIN is ever needed.
     * Always includes all JOINs for consistency between count and results queries.
     */
    protected SelectJoinStep<?> buildBaseResultsQuery(@Nullable Boolean isSuccess) {
        var baseQuery = queryBuilder.buildTransactionSelectQuery(dsl);
        
        // Always include block JOIN for consistency with count queries
        baseQuery = baseQuery.leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH));
        
        if (isSuccess != null) {
            baseQuery = baseQuery.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
        }
        
        return baseQuery.leftJoin(TRANSACTION_SIZE).on(TRANSACTION.TX_HASH.eq(TRANSACTION_SIZE.TX_HASH));
    }
    
    /**
     * Builds a count query with necessary JOINs.
     * Currency filtering uses EXISTS subqueries, so no currency JOIN is ever needed.
     * Always includes block JOIN for consistency with results queries.
     */
    protected SelectJoinStep<org.jooq.Record1<Integer>> buildBaseCountQuery(@Nullable Boolean isSuccess) {
        var countQuery = dsl.selectCount().from(TRANSACTION);
        
        // Always include block JOIN for consistency with results queries
        countQuery = countQuery.leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH));
        
        if (isSuccess != null) {
            countQuery = countQuery.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
        }

        return countQuery;
    }
    
    /**
     * Creates a page result using separate count and results queries.
     * This is the only supported approach - window functions are not used.
     */
    protected Page<TxnEntity> createPageFromSeparateQueries(int totalCount, List<? extends org.jooq.Record> results, OffsetBasedPageRequest pageable) {
        return queryBuilder.createPageFromSeparateQueries(results, totalCount, pageable);
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

    /**
     * Base implementation that delegates to database-specific implementations.
     * No threshold logic - each DB implementation handles all sizes optimally.
     */
    @Override
    @Transactional
    public abstract Page<TxnEntity> searchTxnEntitiesAND(Set<String> txHashes,
                                                        Set<String> addressHashes,
                                                        @Nullable String blockHash,
                                                        @Nullable Long blockNumber,
                                                        @Nullable Long maxBlock,
                                                        @Nullable Boolean isSuccess,
                                                        @Nullable Currency currency,
                                                        OffsetBasedPageRequest offsetBasedPageRequest);

    /**
     * Base implementation that delegates to database-specific implementations.
     * No threshold logic - each DB implementation handles all sizes optimally.
     */
    @Override
    @Transactional
    public abstract Page<TxnEntity> searchTxnEntitiesOR(Set<String> txHashes,
                                                       Set<String> addressHashes,
                                                       @Nullable String blockHash,
                                                       @Nullable Long blockNumber,
                                                       @Nullable Long maxBlock,
                                                       @Nullable Boolean isSuccess,
                                                       @Nullable Currency currency,
                                                       OffsetBasedPageRequest offsetBasedPageRequest);

    /**
     * Generic method to execute a count query with proper JOINs.
     * Ensures count and results queries use identical conditions and JOINs.
     * Currency conditions use EXISTS subqueries - no JOIN needed.
     */
    protected int executeCountQuery(Condition conditions, @Nullable Boolean isSuccess) {
        return buildBaseCountQuery(isSuccess)
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
