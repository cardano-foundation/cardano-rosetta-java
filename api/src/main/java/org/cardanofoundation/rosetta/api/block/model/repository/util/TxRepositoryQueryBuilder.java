package org.cardanofoundation.rosetta.api.block.model.repository.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.TransactionSizeEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;
import org.cardanofoundation.rosetta.api.search.model.Currency;
import org.cardanofoundation.rosetta.common.spring.OffsetBasedPageRequest;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.cardanofoundation.rosetta.api.jooq.Tables.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class TxRepositoryQueryBuilder {

    private final ObjectMapper objectMapper;

    public interface CurrencyConditionBuilder {
        Condition buildCurrencyCondition(Currency currency);
    }

    public SelectJoinStep<Record12<String, String, JSONB, JSONB, Long, Long, Integer, String, Long, Long, Integer, Integer>> buildTransactionSelectQuery(DSLContext dsl) {
        return dsl.select(
                TRANSACTION.TX_HASH,
                TRANSACTION.BLOCK_HASH,
                TRANSACTION.INPUTS,
                TRANSACTION.OUTPUTS,
                TRANSACTION.FEE,
                TRANSACTION.SLOT,
                TRANSACTION.TX_INDEX,
                BLOCK.HASH.as("joined_block_hash"),
                BLOCK.NUMBER.as("joined_block_number"),
                BLOCK.SLOT.as("joined_block_slot"),
                TRANSACTION_SIZE.SIZE.as("joined_tx_size"),
                TRANSACTION_SIZE.SCRIPT_SIZE.as("joined_tx_script_size")
        ).from(TRANSACTION);
    }

    public Condition buildAndConditions(@Nullable Set<String> txHashes,
                                       @Nullable Set<String> addressHashes,
                                       @Nullable String blockHash,
                                       @Nullable Long blockNumber,
                                       @Nullable Long maxBlock,
                                       @Nullable Boolean isSuccess,
                                       @Nullable Currency currency,
                                       CurrencyConditionBuilder currencyConditionBuilder) {
        Condition condition = DSL.trueCondition();

        // For AND logic: transactions must match BOTH tx hash filter AND address hash filter
        if (txHashes != null && !txHashes.isEmpty()) {
            condition = condition.and(TRANSACTION.TX_HASH.in(txHashes));
        }

        if (addressHashes != null && !addressHashes.isEmpty()) {
            condition = condition.and(TRANSACTION.TX_HASH.in(addressHashes));
        }

        if (maxBlock != null) {
            condition = condition.and(BLOCK.NUMBER.le(maxBlock));
        }

        if (blockHash != null) {
            condition = condition.and(BLOCK.HASH.eq(blockHash));
        }

        if (blockNumber != null) {
            condition = condition.and(BLOCK.NUMBER.eq(blockNumber));
        }

        if (isSuccess != null) {
            Condition successCondition = isSuccess
                    ? INVALID_TRANSACTION.TX_HASH.isNull()
                    : INVALID_TRANSACTION.TX_HASH.isNotNull();

            condition = condition.and(successCondition);
        }

        if (currency != null) {
            condition = condition.and(currencyConditionBuilder.buildCurrencyCondition(currency));
        }

        return condition;
    }

    public Condition buildOrConditions(@Nullable Set<String> txHashes,
                                      @Nullable Set<String> addressHashes,
                                      @Nullable String blockHash,
                                      @Nullable Long blockNumber,
                                      @Nullable Long maxBlock,
                                      @Nullable Boolean isSuccess,
                                      @Nullable Currency currency,
                                      CurrencyConditionBuilder currencyConditionBuilder) {
        Condition orCondition = null;
        
        // For OR logic: include transactions that match tx hash OR address hash
        if (txHashes != null && !txHashes.isEmpty()) {
            orCondition = TRANSACTION.TX_HASH.in(txHashes);
        }

        if (addressHashes != null && !addressHashes.isEmpty()) {
            orCondition = orCondition == null 
                    ? TRANSACTION.TX_HASH.in(addressHashes)
                    : orCondition.or(TRANSACTION.TX_HASH.in(addressHashes));
        }

        if (maxBlock != null) {
            orCondition = orCondition == null ? BLOCK.NUMBER.le(maxBlock) : orCondition.or(BLOCK.NUMBER.le(maxBlock));
        }

        if (blockHash != null) {
            orCondition = orCondition == null ? BLOCK.HASH.eq(blockHash) : orCondition.or(BLOCK.HASH.eq(blockHash));
        }

        if (blockNumber != null) {
            orCondition = orCondition == null ? BLOCK.NUMBER.eq(blockNumber) : orCondition.or(BLOCK.NUMBER.eq(blockNumber));
        }

        if (currency != null) {
            orCondition = orCondition == null 
                    ? currencyConditionBuilder.buildCurrencyCondition(currency)
                    : orCondition.or(currencyConditionBuilder.buildCurrencyCondition(currency));
        }

        if (orCondition == null) {
            orCondition = DSL.trueCondition();
        }

        // Success condition should be ANDed with the result of all OR conditions
        // It acts as a filter on the entire result set
        if (isSuccess != null) {
            Condition successCondition = isSuccess
                    ? INVALID_TRANSACTION.TX_HASH.isNull()
                    : INVALID_TRANSACTION.TX_HASH.isNotNull();
            orCondition = orCondition.and(successCondition);
        }

        return orCondition;
    }

    public TxnEntity mapRecordToTxnEntity(org.jooq.Record record) {
        String txHash = record.get(TRANSACTION.TX_HASH);
        JSONB inputs = record.get(TRANSACTION.INPUTS);
        JSONB outputs = record.get(TRANSACTION.OUTPUTS);

        @Nullable BigInteger fee = Optional.ofNullable(record.get(TRANSACTION.FEE))
                .map(BigInteger::valueOf).orElse(null);

        @Nullable Integer txIndex = record.get(TRANSACTION.TX_INDEX);

        @Nullable BlockEntity blockEntity = null;
        String blockHashFromRecord = record.get("joined_block_hash", String.class);
        if (blockHashFromRecord != null) {
            blockEntity = BlockEntity.builder()
                    .hash(blockHashFromRecord)
                    .number(record.get("joined_block_number", Long.class))
                    .slot(record.get("joined_block_slot", Long.class))
                    .build();
        }

        return TxnEntity.builder()
                .txHash(txHash)
                .block(blockEntity)
                .sizeEntity(getTransactionSizeEntity(record, txHash, blockEntity))
                .inputKeys(readUtxoKeys(inputs, txHash))
                .outputKeys(readUtxoKeys(outputs, txHash))
                .fee(fee)
                .txIndex(txIndex)
                .build();
    }

    @Nullable
    private static TransactionSizeEntity getTransactionSizeEntity(org.jooq.Record record,
                                                                 String txHash,
                                                                 BlockEntity blockEntity) {
        TransactionSizeEntity sizeEntity = null;
        Integer size = record.get("joined_tx_size", Integer.class);
        Integer scriptSize = record.get("joined_tx_script_size", Integer.class);

        if (size != null || scriptSize != null) {
            sizeEntity = new TransactionSizeEntity(
                    txHash,
                    blockEntity != null ? blockEntity.getNumber() : 0L,
                    size != null ? size : 0,
                    scriptSize != null ? scriptSize : 0
            );
        }

        return sizeEntity;
    }

    private List<UtxoKey> readUtxoKeys(@Nullable JSONB jsonB,
                                      String txHash) {
        if (jsonB == null) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(jsonB.data(), new TypeReference<List<UtxoKey>>() {});
        } catch (Exception e) {
            log.warn("Failed to deserialize input keys for tx {}: {}", txHash, e.getMessage());
        }

        return Collections.emptyList();
    }

    // Window function methods have been removed to enforce separate count/results query pattern

    /**
     * Creates a Page from separate results and count queries.
     * This approach provides much better performance for large datasets.
     */
    public Page<TxnEntity> createPageFromSeparateQueries(List<? extends org.jooq.Record> results,
                                                         int totalCount,
                                                         OffsetBasedPageRequest pageable) {
        if (results.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, totalCount);
        }

        // Map all records to entities
        List<TxnEntity> entities = results.stream()
                .map(this::mapRecordToTxnEntity)
                .toList();

        return new PageImpl<>(entities, pageable, totalCount);
    }

}
