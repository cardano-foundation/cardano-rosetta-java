package org.cardanofoundation.rosetta.api.block.model.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.TransactionSizeEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;
import org.cardanofoundation.rosetta.api.search.model.Currency;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.cardanofoundation.rosetta.api.jooq.Tables.*;

@Slf4j
@Repository("txRepository")
@RequiredArgsConstructor
public class TxRepositoryCustomImpl implements TxRepository {

  private final DSLContext dsl;
  private final ObjectMapper objectMapper;
  private final Environment environment;

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
                                              @Nullable Currency currency,
                                              Pageable pageable) {

    // Build the inner subquery conditions
    Condition conditions = buildAndConditions(txHashes, blockHash, blockNumber, maxBlock, isSuccess, currency);

    // Build main query with conditional INVALID_TRANSACTION join
    var baseQueryFrom = dsl.select(
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
            .from(TRANSACTION);

    // Conditionally add INVALID_TRANSACTION join only when isSuccess is not null
    if (isSuccess != null) {
      baseQueryFrom = baseQueryFrom.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
    }

    var baseQuery = baseQueryFrom
            .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
            .leftJoin(TRANSACTION_SIZE).on(TRANSACTION.TX_HASH.eq(TRANSACTION_SIZE.TX_HASH))
            .where(TRANSACTION.TX_HASH.in(
                    buildSubquery(conditions, isSuccess)
            ))
            .orderBy(TRANSACTION.SLOT.desc());

    // Build count query with conditional INVALID_TRANSACTION join
    var countQueryFrom = dsl.selectCount()
            .from(TRANSACTION);

    // Conditionally add INVALID_TRANSACTION join only when isSuccess is not null
    if (isSuccess != null) {
      countQueryFrom = countQueryFrom.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
    }

    var countQuery = countQueryFrom
            .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
            .where(TRANSACTION.TX_HASH.in(
                    buildSubquery(conditions, isSuccess)
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
                                             @Nullable Currency currency,
                                             Pageable pageable) {

    // Build the inner subquery conditions with OR logic
    Condition conditions = buildOrConditions(txHashes, blockHash, blockNumber, maxBlock, isSuccess, currency);

    // Build main query with conditional INVALID_TRANSACTION join
    var baseQueryFrom = dsl.select(
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
            .from(TRANSACTION);

    // Conditionally add INVALID_TRANSACTION join only when isSuccess is not null
    if (isSuccess != null) {
      baseQueryFrom = baseQueryFrom.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
    }

    var baseQuery = baseQueryFrom
            .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
            .leftJoin(TRANSACTION_SIZE).on(TRANSACTION.TX_HASH.eq(TRANSACTION_SIZE.TX_HASH))
            .where(TRANSACTION.TX_HASH.in(
                    buildOrSubquery(conditions, isSuccess)
            ))
            .orderBy(TRANSACTION.SLOT.desc());

    // Build count query with conditional INVALID_TRANSACTION join
    var countQueryFrom = dsl.selectCount()
            .from(TRANSACTION);

    // Conditionally add INVALID_TRANSACTION join only when isSuccess is not null
    if (isSuccess != null) {
      countQueryFrom = countQueryFrom.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
    }

    var countQuery = countQueryFrom
            .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
            .where(TRANSACTION.TX_HASH.in(
                    buildOrSubquery(conditions, isSuccess)
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
                                       @Nullable Boolean isSuccess,
                                       @Nullable Currency currency) {
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

    // Only add isSuccess condition if it's not null
    // The INVALID_TRANSACTION join is conditionally added in the calling methods
    if (isSuccess != null) {
      Condition successCondition = isSuccess
              ? INVALID_TRANSACTION.TX_HASH.isNull()
              : INVALID_TRANSACTION.TX_HASH.isNotNull();

      condition = condition.and(successCondition);
    }

    // Add currency filtering condition
    if (currency != null) {
      condition = condition.and(buildCurrencyCondition(currency));
    }

    return condition;
  }

  private Condition buildOrConditions(@Nullable Set<String> txHashes,
                                      @Nullable String blockHash,
                                      @Nullable Long blockNumber,
                                      @Nullable Long maxBlock,
                                      @Nullable Boolean isSuccess,
                                      @Nullable Currency currency) {
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
    // The INVALID_TRANSACTION join is conditionally added in the calling methods
    if (isSuccess != null) {
      Condition successCondition = isSuccess
              ? INVALID_TRANSACTION.TX_HASH.isNull()
              : INVALID_TRANSACTION.TX_HASH.isNotNull();
      orCondition = orCondition.and(successCondition);
    }

    // Currency filtering should be AND condition even in OR mode
    if (currency != null) {
      orCondition = orCondition.and(buildCurrencyCondition(currency));
    }

    return orCondition;
  }

  /**
   * Builds a subquery for AND search with conditional INVALID_TRANSACTION join
   */
  private SelectConditionStep<Record1<String>> buildSubquery(Condition conditions, @Nullable Boolean isSuccess) {
    var subqueryFrom = dsl.selectDistinct(TRANSACTION.TX_HASH)
            .from(TRANSACTION);

    // Conditionally add INVALID_TRANSACTION join only when isSuccess is not null
    if (isSuccess != null) {
      subqueryFrom = subqueryFrom.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
    }

    return subqueryFrom
            .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
            .where(conditions);
  }

  /**
   * Builds a subquery for OR search with conditional INVALID_TRANSACTION join
   */
  private SelectConditionStep<Record1<String>> buildOrSubquery(Condition conditions, @Nullable Boolean isSuccess) {
    var subqueryFrom = dsl.selectDistinct(TRANSACTION.TX_HASH)
            .from(TRANSACTION)
            .leftJoin(ADDRESS_UTXO).on(TRANSACTION.TX_HASH.eq(ADDRESS_UTXO.TX_HASH));

    // Conditionally add INVALID_TRANSACTION join only when isSuccess is not null
    if (isSuccess != null) {
      subqueryFrom = subqueryFrom.leftJoin(INVALID_TRANSACTION).on(TRANSACTION.TX_HASH.eq(INVALID_TRANSACTION.TX_HASH));
    }

    return subqueryFrom
            .leftJoin(BLOCK).on(TRANSACTION.BLOCK_HASH.eq(BLOCK.HASH))
            .where(conditions);
  }

  private Condition buildCurrencyCondition(Currency currency) {
    // Currency object contains symbol, policyId, and decimals
    // Strategy: Use policyId if available, otherwise fall back to symbol

    String policyId = currency.getPolicyId();
    String symbol = currency.getSymbol();

    // Determine SQL dialect to use appropriate JSON operators
    // Check for h2 profile first, as JOOQ might still be configured for PostgreSQL in application-h2.yaml

    boolean isH2Profile = environment.acceptsProfiles(Profiles.of("test-integration", "h2"));
    boolean isPostgreSQL = !isH2Profile && dsl.configuration().dialect().family() == SQLDialect.POSTGRES;

    // If we have a policy ID, use it for precise matching
    if (policyId != null && !policyId.trim().isEmpty()) {
      String escapedPolicyId = policyId.trim().replace("\"", "\\\"");

      // If we also have a symbol (asset name), match both policy ID and asset name
      if (symbol != null && !symbol.trim().isEmpty() &&
              !"lovelace".equalsIgnoreCase(symbol) && !"ada".equalsIgnoreCase(symbol)) {
        String escapedSymbol = symbol.trim().replace("\"", "\\\"");

        if (isPostgreSQL) {
          return DSL.condition("EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                  "AND au.amounts::jsonb @> '[{\"policy_id\": \"" + escapedPolicyId + "\", \"asset_name\": \"" + escapedSymbol + "\"}]')");
        } else {
          // H2 - use LIKE with JSON string matching
          return DSL.condition("EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                  "AND au.amounts LIKE '%\"policy_id\":\"" + escapedPolicyId + "\"%' " +
                  "AND au.amounts LIKE '%\"asset_name\":\"" + escapedSymbol + "\"%')");
        }
      } else {
        // Policy ID only - search for any asset with this policy ID
        if (isPostgreSQL) {
          return DSL.condition("EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                  "AND au.amounts::jsonb @> '[{\"policy_id\": \"" + escapedPolicyId + "\"}]')");
        } else {
          // H2 - use LIKE with JSON string matching
          return DSL.condition("EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                  "AND au.amounts LIKE '%\"policy_id\":\"" + escapedPolicyId + "\"%')");
        }
      }
    }

    // Fall back to symbol-based matching
    if (symbol != null && !symbol.trim().isEmpty()) {
      if ("lovelace".equalsIgnoreCase(symbol) || "ada".equalsIgnoreCase(symbol)) {
        // For ADA/lovelace, we check for unit = "lovelace" in the amounts array
        if (isPostgreSQL) {
          return DSL.condition("EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                  "AND au.amounts::jsonb @> '[{\"unit\": \"lovelace\"}]')");
        } else {
          // H2 - use LIKE with JSON string matching
          return DSL.condition("EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                  "AND au.amounts LIKE '%\"unit\":\"lovelace\"%')");
        }
      } else {
        // Search by asset name (symbol) across all policy IDs
        String escapedSymbol = symbol.trim().replace("\"", "\\\"");

        if (isPostgreSQL) {
          return DSL.condition("EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                  "AND au.amounts::jsonb @> '[{\"asset_name\": \"" + escapedSymbol + "\"}]')");
        } else {
          // H2 - use LIKE with JSON string matching
          return DSL.condition("EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                  "AND au.amounts LIKE '%\"asset_name\":\"" + escapedSymbol + "\"%')");
        }
      }
    }

    // If neither policy ID nor symbol is provided, return a condition that matches nothing
    return DSL.falseCondition();
  }

  private TxnEntity mapRecordToTxnEntity(Record record) {
    String txHash = record.get(TRANSACTION.TX_HASH);
    //String blockHashValue = record.get(TRANSACTION.BLOCK_HASH);
    JSONB inputs = record.get(TRANSACTION.INPUTS);
    JSONB outputs = record.get(TRANSACTION.OUTPUTS);
    BigInteger fee = Optional.ofNullable(record.get(TRANSACTION.FEE)).map(BigInteger::valueOf).orElse(null);
    //Long slot = record.get(TRANSACTION.SLOT);

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

    return TxnEntity.builder()
            .txHash(txHash)
            .block(blockEntity)
            .sizeEntity(getTransactionSizeEntity(record, txHash, blockEntity))
            .inputKeys(readUtxoKeys(inputs, txHash))
            .outputKeys(readUtxoKeys(outputs, txHash))
            .fee(fee)
            .build();
  }

  @Nullable
  private static TransactionSizeEntity getTransactionSizeEntity(Record record,
                                                                String txHash,
                                                                BlockEntity blockEntity) {
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

}
