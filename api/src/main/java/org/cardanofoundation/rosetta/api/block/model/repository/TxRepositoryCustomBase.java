package org.cardanofoundation.rosetta.api.block.model.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.block.model.repository.util.TxHashTempTableManager;
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
    protected final TxHashTempTableManager tempTableManager;

    protected static final int TEMP_TABLE_THRESHOLD = 10000;

    protected abstract TxRepositoryQueryBuilder.CurrencyConditionBuilder getCurrencyConditionBuilder();

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
        
        if (txHashes != null && txHashes.size() > TEMP_TABLE_THRESHOLD) {
            return searchTxnEntitiesANDWithTempTable(txHashes, blockHash, blockNumber, maxBlock, isSuccess, currency, pageable);
        }

        Condition conditions = queryBuilder.buildAndConditions(txHashes, blockHash, blockNumber, maxBlock, isSuccess, currency, getCurrencyConditionBuilder());

        var baseQueryFrom = queryBuilder.buildTransactionSelectQuery(dsl);

        if (isSuccess != null) {
            baseQueryFrom = baseQueryFrom.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
        }

        var baseQuery = baseQueryFrom
                .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                .leftJoin(TRANSACTION_SIZE).on(TRANSACTION.TX_HASH.eq(TRANSACTION_SIZE.TX_HASH))
                .where(TRANSACTION.TX_HASH.in(
                        queryBuilder.buildSubquery(dsl, conditions, isSuccess)
                ))
                .orderBy(TRANSACTION.SLOT.desc());

        var countQueryFrom = queryBuilder.buildCountQuery(dsl);

        if (isSuccess != null) {
            countQueryFrom = countQueryFrom.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
        }

        var countQuery = countQueryFrom
                .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                .where(TRANSACTION.TX_HASH.in(
                        queryBuilder.buildSubquery(dsl, conditions, isSuccess)
                ));

        Integer totalElements = countQuery.fetchOne(0, Integer.class);

        List<TxnEntity> content = baseQuery
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch(queryBuilder::mapRecordToTxnEntity);

        return new PageImpl<>(content, pageable, totalElements);
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
        
        if (txHashes != null && txHashes.size() > TEMP_TABLE_THRESHOLD) {
            return searchTxnEntitiesORWithTempTable(txHashes, blockHash, blockNumber, maxBlock, isSuccess, currency, pageable);
        }

        Condition orConditions = queryBuilder.buildOrConditions(txHashes, blockHash, blockNumber, maxBlock, isSuccess, currency, getCurrencyConditionBuilder());

        var baseQueryFrom = queryBuilder.buildTransactionSelectQuery(dsl);

        if (isSuccess != null) {
            baseQueryFrom = baseQueryFrom.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
        }

        var baseQuery = baseQueryFrom
                .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                .leftJoin(TRANSACTION_SIZE).on(TRANSACTION.TX_HASH.eq(TRANSACTION_SIZE.TX_HASH))
                .where(TRANSACTION.TX_HASH.in(
                        queryBuilder.buildOrSubquery(dsl, orConditions, isSuccess)
                ))
                .orderBy(TRANSACTION.SLOT.desc());

        var countQueryFrom = queryBuilder.buildCountQuery(dsl);

        if (isSuccess != null) {
            countQueryFrom = countQueryFrom.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
        }

        var countQuery = countQueryFrom
                .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                .where(TRANSACTION.TX_HASH.in(
                        queryBuilder.buildOrSubquery(dsl, orConditions, isSuccess)
                ));

        Integer totalElements = countQuery.fetchOne(0, Integer.class);

        List<TxnEntity> content = baseQuery
                .limit(pageable.getPageSize())
                .offset((int) pageable.getOffset())
                .fetch(queryBuilder::mapRecordToTxnEntity);

        return new PageImpl<>(content, pageable, totalElements);
    }

    @Transactional
    protected Page<TxnEntity> searchTxnEntitiesANDWithTempTable(@Nullable Set<String> txHashes,
                                                              @Nullable String blockHash,
                                                              @Nullable Long blockNumber,
                                                              @Nullable Long maxBlock,
                                                              @Nullable Boolean isSuccess,
                                                              @Nullable Currency currency,
                                                              Pageable pageable) {
        if (txHashes == null || txHashes.isEmpty()) {
            return searchTxnEntitiesAND(null, blockHash, blockNumber, maxBlock, isSuccess, currency, pageable);
        }

        log.debug("Using temporary table approach for AND search with {} transaction hashes", txHashes.size());

        String tempTableName = null;
        try {
            tempTableName = tempTableManager.createAndPopulateTempTable(txHashes);
            Table<?> tempTable = tempTableManager.getTempTable(tempTableName);
            var tempTxHashField = tempTableManager.getTxHashField(tempTable);

            Condition conditions = queryBuilder.buildAndConditions(null, blockHash, blockNumber, maxBlock, isSuccess, currency, getCurrencyConditionBuilder());

            var baseQueryFrom = queryBuilder.buildTransactionSelectQuery(dsl)
                    .join(tempTable).on(TRANSACTION.TX_HASH.eq(tempTxHashField));

            if (isSuccess != null) {
                baseQueryFrom = baseQueryFrom.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
            }

            var baseQuery = baseQueryFrom
                    .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                    .leftJoin(TRANSACTION_SIZE).on(TRANSACTION.TX_HASH.eq(TRANSACTION_SIZE.TX_HASH))
                    .where(conditions)
                    .orderBy(TRANSACTION.SLOT.desc());

            var countQueryFrom = queryBuilder.buildCountQuery(dsl)
                    .join(tempTable).on(TRANSACTION.TX_HASH.eq(tempTxHashField));

            if (isSuccess != null) {
                countQueryFrom = countQueryFrom.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
            }

            var countQuery = countQueryFrom
                    .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                    .where(conditions);

            Integer totalElements = countQuery.fetchOne(0, Integer.class);

            List<TxnEntity> content = baseQuery
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch(queryBuilder::mapRecordToTxnEntity);

            return new PageImpl<>(content, pageable, totalElements);
            
        } finally {
            if (tempTableName != null) {
                try {
                    tempTableManager.dropTempTableIfExists(tempTableName);
                } catch (Exception e) {
                    log.warn("Failed to clean up temporary table {}: {}", tempTableName, e.getMessage());
                }
            }
        }
    }

    @Transactional
    protected Page<TxnEntity> searchTxnEntitiesORWithTempTable(@Nullable Set<String> txHashes,
                                                             @Nullable String blockHash,
                                                             @Nullable Long blockNumber,
                                                             @Nullable Long maxBlock,
                                                             @Nullable Boolean isSuccess,
                                                             @Nullable Currency currency,
                                                             Pageable pageable) {
        if (txHashes == null || txHashes.isEmpty()) {
            return searchTxnEntitiesOR(null, blockHash, blockNumber, maxBlock, isSuccess, currency, pageable);
        }

        log.debug("Using temporary table approach for OR search with {} transaction hashes", txHashes.size());

        String tempTableName = null;
        try {
            tempTableName = tempTableManager.createAndPopulateTempTable(txHashes);
            Table<?> tempTable = tempTableManager.getTempTable(tempTableName);
            var tempTxHashField = tempTableManager.getTxHashField(tempTable);

            var tempTableQueryFrom = queryBuilder.buildTransactionSelectQuery(dsl)
                    .join(tempTable).on(TRANSACTION.TX_HASH.eq(tempTxHashField))
                    .leftJoin(ADDRESS_UTXO).on(TRANSACTION.TX_HASH.eq(ADDRESS_UTXO.TX_HASH));

            if (isSuccess != null) {
                tempTableQueryFrom = tempTableQueryFrom.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
            }

            Condition andConditions = buildTempTableAndConditions(isSuccess, currency);

            var tempTableQuery = tempTableQueryFrom
                    .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                    .leftJoin(TRANSACTION_SIZE).on(TRANSACTION.TX_HASH.eq(TRANSACTION_SIZE.TX_HASH))
                    .where(andConditions);

            if (blockHash != null || blockNumber != null || maxBlock != null) {
                Condition otherOrConditions = queryBuilder.buildOrConditions(null, blockHash, blockNumber, maxBlock, isSuccess, currency, getCurrencyConditionBuilder());
                
                var otherQueryFrom = queryBuilder.buildTransactionSelectQuery(dsl)
                        .leftJoin(ADDRESS_UTXO).on(TRANSACTION.TX_HASH.eq(ADDRESS_UTXO.TX_HASH));

                if (isSuccess != null) {
                    otherQueryFrom = otherQueryFrom.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
                }

                var otherQuery = otherQueryFrom
                        .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                        .leftJoin(TRANSACTION_SIZE).on(TRANSACTION.TX_HASH.eq(TRANSACTION_SIZE.TX_HASH))
                        .where(otherOrConditions);

                var unionQuery = tempTableQuery.union(otherQuery)
                        .orderBy(DSL.field("slot").desc());

                var countQuery = dsl.selectCount()
                        .from(DSL.table("({0}) as union_result", unionQuery));

                Integer totalElements = countQuery.fetchOne(0, Integer.class);

                List<TxnEntity> content = unionQuery
                        .limit(pageable.getPageSize())
                        .offset((int) pageable.getOffset())
                        .fetch(queryBuilder::mapRecordToTxnEntity);

                return new PageImpl<>(content, pageable, totalElements);
            } else {
                var baseQuery = tempTableQuery.orderBy(TRANSACTION.SLOT.desc());

                var countQuery = dsl.selectCount()
                        .from(TRANSACTION)
                        .join(tempTable).on(TRANSACTION.TX_HASH.eq(tempTxHashField))
                        .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                        .where(andConditions);

                Integer totalElements = countQuery.fetchOne(0, Integer.class);

                List<TxnEntity> content = baseQuery
                        .limit(pageable.getPageSize())
                        .offset((int) pageable.getOffset())
                        .fetch(queryBuilder::mapRecordToTxnEntity);

                return new PageImpl<>(content, pageable, totalElements);
            }
            
        } finally {
            if (tempTableName != null) {
                try {
                    tempTableManager.dropTempTableIfExists(tempTableName);
                } catch (Exception e) {
                    log.warn("Failed to clean up temporary table {}: {}", tempTableName, e.getMessage());
                }
            }
        }
    }

    private Condition buildTempTableAndConditions(@Nullable Boolean isSuccess, @Nullable Currency currency) {
        Condition andConditions = DSL.trueCondition();
        if (isSuccess != null) {
            Condition successCondition = isSuccess
                    ? INVALID_TRANSACTION.TX_HASH.isNull()
                    : INVALID_TRANSACTION.TX_HASH.isNotNull();
            andConditions = andConditions.and(successCondition);
        }
        if (currency != null) {
            andConditions = andConditions.and(getCurrencyConditionBuilder().buildCurrencyCondition(currency));
        }
        return andConditions;
    }
}