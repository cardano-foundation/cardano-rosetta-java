package org.cardanofoundation.rosetta.api.block.model.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.block.model.repository.util.TxRepositoryQueryBuilder;
import org.cardanofoundation.rosetta.api.search.model.Currency;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
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
    
    protected abstract Page<TxnEntity> searchTxnEntitiesANDWithLargeHashSet(Set<String> txHashes,
                                                                           @Nullable String blockHash,
                                                                           @Nullable Long blockNumber,
                                                                           @Nullable Long maxBlock,
                                                                           @Nullable Boolean isSuccess,
                                                                           @Nullable Currency currency,
                                                                           Pageable pageable);
    
    protected abstract Page<TxnEntity> searchTxnEntitiesORWithLargeHashSet(Set<String> txHashes,
                                                                          @Nullable String blockHash,
                                                                          @Nullable Long blockNumber,
                                                                          @Nullable Long maxBlock,
                                                                          @Nullable Boolean isSuccess,
                                                                          @Nullable Currency currency,
                                                                          Pageable pageable);

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
    public Page<TxnEntity> searchTxnEntitiesAND(@Nullable Set<String> txHashes,
                                               @Nullable String blockHash,
                                               @Nullable Long blockNumber,
                                               @Nullable Long maxBlock,
                                               @Nullable Boolean isSuccess,
                                               @Nullable Currency currency,
                                               Pageable pageable) {
        
        if (txHashes != null && txHashes.size() > LARGE_HASH_SET_THRESHOLD) {
            return searchTxnEntitiesANDWithLargeHashSet(txHashes, blockHash, blockNumber, maxBlock, isSuccess, currency, pageable);
        }

        // Build conditions
        Condition conditions = queryBuilder.buildAndConditions(txHashes, blockHash, blockNumber, maxBlock, isSuccess, currency, getCurrencyConditionBuilder());

        // Build single query with count using window function
        var baseQueryFrom = queryBuilder.buildTransactionSelectQueryWithCount(dsl);

        if (isSuccess != null) {
            baseQueryFrom = baseQueryFrom.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
        }

        var queryWithCount = baseQueryFrom
                .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                .leftJoin(TRANSACTION_SIZE).on(TRANSACTION.TX_HASH.eq(TRANSACTION_SIZE.TX_HASH))
                .where(TRANSACTION.TX_HASH.in(
                        queryBuilder.buildSubquery(dsl, conditions, isSuccess)
                ))
                .orderBy(TRANSACTION.SLOT.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset());

        // Single database call - get both results and count
        List<? extends org.jooq.Record> results = queryWithCount.fetch();
        return queryBuilder.createPageFromResultsWithCount(results, pageable);
    }

    @Override
    @Transactional
    public Page<TxnEntity> searchTxnEntitiesOR(@Nullable Set<String> txHashes,
                                              @Nullable String blockHash,
                                              @Nullable Long blockNumber,
                                              @Nullable Long maxBlock,
                                              @Nullable Boolean isSuccess,
                                              @Nullable Currency currency,
                                              Pageable pageable) {
        
        if (txHashes != null && txHashes.size() > LARGE_HASH_SET_THRESHOLD) {
            return searchTxnEntitiesORWithLargeHashSet(txHashes, blockHash, blockNumber, maxBlock, isSuccess, currency, pageable);
        }

        // Build OR conditions
        Condition orConditions = queryBuilder.buildOrConditions(txHashes, blockHash, blockNumber, maxBlock, isSuccess, currency, getCurrencyConditionBuilder());

        // Build single query with count using window function
        var baseQueryFrom = queryBuilder.buildTransactionSelectQueryWithCount(dsl);

        if (isSuccess != null) {
            baseQueryFrom = baseQueryFrom.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
        }

        var queryWithCount = baseQueryFrom
                .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                .leftJoin(TRANSACTION_SIZE).on(TRANSACTION.TX_HASH.eq(TRANSACTION_SIZE.TX_HASH))
                .where(TRANSACTION.TX_HASH.in(
                        queryBuilder.buildOrSubquery(dsl, orConditions, isSuccess)
                ))
                .orderBy(TRANSACTION.SLOT.desc())
                .limit(pageable.getPageSize())
                .offset((int) pageable.getOffset());

        // Single database call - get both results and count
        List<? extends org.jooq.Record> results = queryWithCount.fetch();
        return queryBuilder.createPageFromResultsWithCount(results, pageable);
    }

}