package org.cardanofoundation.rosetta.api.block.model.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.TransactionSizeEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.cardanofoundation.rosetta.api.jooq.Tables.*;

@Slf4j
@Repository("txRepository")
@RequiredArgsConstructor
public class TxRepositoryCustomImpl implements TxRepository {

  private final DSLContext dsl;
  private final ObjectMapper objectMapper;

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
            .fetch(this::mapRecordToTxnEntity);
  }

  @Override
  public Page<TxnEntity> searchTxnEntitiesAND(@Nullable Set<String> txHashes,
                                              @Nullable String blockHash,
                                              @Nullable Long blockNumber,
                                              @Nullable Long maxBlock,
                                              @Nullable Boolean isSuccess,
                                              Pageable pageable) {

    // Build the inner subquery conditions
    Condition conditions = buildAndConditions(txHashes, blockHash, blockNumber, maxBlock, isSuccess);

    var baseQuery = dsl.select(
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
            .leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH))
            .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
            .leftJoin(TRANSACTION_SIZE).on(TRANSACTION.TX_HASH.eq(TRANSACTION_SIZE.TX_HASH))
            .where(TRANSACTION.TX_HASH.in(
                    dsl.selectDistinct(TRANSACTION.TX_HASH)
                            .from(TRANSACTION)
                            .leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH))
                            .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                            .where(conditions)
            ))
            .orderBy(TRANSACTION.SLOT.desc());

    // Count query for pagination
    var countQuery = dsl.selectCount()
            .from(TRANSACTION)
            .leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH))
            .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
            .where(TRANSACTION.TX_HASH.in(
                    dsl.selectDistinct(TRANSACTION.TX_HASH)
                            .from(TRANSACTION)
                            .leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH))
                            .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                            .where(conditions)
            ));

    Integer totalElements = countQuery.fetchOne(0, Integer.class);

    // Apply pagination and fetch results
    List<TxnEntity> content = baseQuery
            .limit(pageable.getPageSize())
            .offset(pageable.getOffset())
            .fetch(this::mapRecordToTxnEntity);

    return new PageImpl<>(content, pageable, totalElements);
  }

  @Override
  public Page<TxnEntity> searchTxnEntitiesOR(@Nullable Set<String> txHashes,
                                             @Nullable String blockHash,
                                             @Nullable Long blockNumber,
                                             @Nullable Long maxBlock,
                                             @Nullable Boolean isSuccess,
                                             Pageable pageable) {

    // Build the inner subquery conditions with OR logic
    Condition conditions = buildOrConditions(txHashes, blockHash, blockNumber, maxBlock, isSuccess);

    var baseQuery = dsl.select(
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
            .leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH))
            .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
            .leftJoin(TRANSACTION_SIZE).on(TRANSACTION.TX_HASH.eq(TRANSACTION_SIZE.TX_HASH))
            .where(TRANSACTION.TX_HASH.in(
                    dsl.selectDistinct(TRANSACTION.TX_HASH)
                            .from(TRANSACTION)
                            .leftJoin(ADDRESS_UTXO).on(TRANSACTION.TX_HASH.eq(ADDRESS_UTXO.TX_HASH))
                            .leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH))
                            .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                            .where(conditions)
            ))
            .orderBy(TRANSACTION.SLOT.desc());

    // Count query for pagination
    var countQuery = dsl.selectCount()
            .from(TRANSACTION)
            .leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH))
            .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
            .where(TRANSACTION.TX_HASH.in(
                    dsl.selectDistinct(TRANSACTION.TX_HASH)
                            .from(TRANSACTION)
                            .leftJoin(ADDRESS_UTXO).on(TRANSACTION.TX_HASH.eq(ADDRESS_UTXO.TX_HASH))
                            .leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH))
                            .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
                            .where(conditions)
            ));

    Integer totalElements = countQuery.fetchOne(0, Integer.class);

    // Apply pagination and fetch results
    List<TxnEntity> content = baseQuery
            .limit(pageable.getPageSize())
            .offset((int) pageable.getOffset())
            .fetch(this::mapRecordToTxnEntity);

    return new PageImpl<>(content, pageable, totalElements);
  }

  private Condition buildAndConditions(@Nullable Set<String> txHashes,
                                       @Nullable String blockHash,
                                       @Nullable Long blockNumber,
                                       @Nullable Long maxBlock,
                                       @Nullable Boolean isSuccess) {
    Condition condition = DSL.trueCondition(); // Start with always true condition

    if (txHashes != null && !txHashes.isEmpty()) {
      condition = condition.and(TRANSACTION.TX_HASH.in(txHashes));
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

    return condition;
  }

  private Condition buildOrConditions(@Nullable Set<String> txHashes,
                                      @Nullable String blockHash,
                                      @Nullable Long blockNumber,
                                      @Nullable Long maxBlock,
                                      @Nullable Boolean isSuccess) {
    Condition orCondition = null;

    // Build OR conditions for the main search criteria
    if (txHashes != null && !txHashes.isEmpty()) {
      orCondition = TRANSACTION.TX_HASH.in(txHashes);
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

    // If no OR conditions were specified, use true condition
    if (orCondition == null) {
      orCondition = DSL.trueCondition();
    }

    // isSuccess should be AND condition even in OR mode
    if (isSuccess != null) {
      Condition successCondition = isSuccess
              ? INVALID_TRANSACTION.TX_HASH.isNull()
              : INVALID_TRANSACTION.TX_HASH.isNotNull();
      orCondition = orCondition.and(successCondition);
    }

    return orCondition;
  }

  private TxnEntity mapRecordToTxnEntity(Record record) {
    String txHash = record.get(TRANSACTION.TX_HASH);
    String blockHashValue = record.get(TRANSACTION.BLOCK_HASH);
    JSONB inputs = record.get(TRANSACTION.INPUTS);
    JSONB outputs = record.get(TRANSACTION.OUTPUTS);
    Long fee = record.get(TRANSACTION.FEE);
    Long slot = record.get(TRANSACTION.SLOT);

    // Create block entity
    BlockEntity blockEntity = null;
    String blockHashFromRecord = record.get("joined_block_hash", String.class);
    if (blockHashFromRecord != null) {
      blockEntity = BlockEntity.builder()
              .hash(blockHashFromRecord)
              .number(record.get("joined_block_number", Long.class))
              .slot(record.get("joined_block_slot", Long.class))
              .build();
    }

    // Convert JSONB to List<UtxoKey>
    List<UtxoKey> inputKeys = null;
    List<UtxoKey> outputKeys = null;

    if (inputs != null) {
      try {
        inputKeys = objectMapper.readValue(inputs.data(), new TypeReference<List<UtxoKey>>() {});
      } catch (Exception e) {
        log.warn("Failed to deserialize input keys for tx {}: {}", txHash, e.getMessage());
        inputKeys = Collections.emptyList();
      }
    }

    if (outputs != null) {
      try {
        outputKeys = objectMapper.readValue(outputs.data(), new TypeReference<List<UtxoKey>>() {});
      } catch (Exception e) {
        log.warn("Failed to deserialize output keys for tx {}: {}", txHash, e.getMessage());
        outputKeys = Collections.emptyList();
      }
    }

    // Create transaction size entity
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

    return TxnEntity.builder()
            .txHash(txHash)
            .block(blockEntity)
            .sizeEntity(sizeEntity)
            .inputKeys(inputKeys)
            .outputKeys(outputKeys)
            .fee(fee != null ? BigInteger.valueOf(fee) : null)
            .build();
  }
}

