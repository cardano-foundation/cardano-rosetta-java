package org.cardanofoundation.rosetta.api.block.model.repository.h2;

import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.block.model.repository.TxRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.TxRepositoryCustomBase;
import org.cardanofoundation.rosetta.api.block.model.repository.util.TxRepositoryQueryBuilder;
import org.cardanofoundation.rosetta.api.search.model.Currency;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Profile;
import org.cardanofoundation.rosetta.common.spring.OffsetBasedPageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static org.cardanofoundation.rosetta.api.jooq.Tables.*;

@Slf4j
@Repository("txRepository")
@Profile({"h2", "test-integration"})
public class TxRepositoryH2Impl extends TxRepositoryCustomBase implements TxRepository {

    private final H2CurrencyConditionBuilder currencyConditionBuilder;

    private static final int H2_IN_CLAUSE_LIMIT = 1000; // H2 can handle smaller batches efficiently

    public TxRepositoryH2Impl(DSLContext dsl, 
                             TxRepositoryQueryBuilder queryBuilder) {
        super(dsl, queryBuilder);
        this.currencyConditionBuilder = new H2CurrencyConditionBuilder();
    }

    @Override
    protected TxRepositoryQueryBuilder.CurrencyConditionBuilder getCurrencyConditionBuilder() {
        return currencyConditionBuilder;
    }

    /**
     * H2 implementation using simple IN clauses with batching for integration testing.
     * Optimized for simplicity rather than maximum performance.
     */
    @Override
    @Transactional
    public Page<TxnEntity> searchTxnEntitiesAND(Set<String> txHashes,
                                               Set<String> addressHashes,
                                               @Nullable String blockHash,
                                               @Nullable Long blockNumber,
                                               @Nullable Long maxBlock,
                                               @Nullable Boolean isSuccess,
                                               @Nullable Currency currency,
                                               OffsetBasedPageRequest pageable) {
        
        log.debug("Using H2 simple approach for AND search with {} tx hashes and {} address hashes", 
                 txHashes != null ? txHashes.size() : 0,
                 addressHashes != null ? addressHashes.size() : 0);

        // Build base conditions without hash filters
        Condition baseConditions = queryBuilder.buildAndConditions(null, null, blockHash, blockNumber, maxBlock, isSuccess, currency, getCurrencyConditionBuilder());

        // Add txHashes condition (AND logic)
        if (txHashes != null && !txHashes.isEmpty()) {
            baseConditions = baseConditions.and(createBatchedInCondition(txHashes));
        }

        // Add addressHashes condition (AND logic)
        if (addressHashes != null && !addressHashes.isEmpty()) {
            baseConditions = baseConditions.and(createBatchedInCondition(addressHashes));
        }

        // Execute separate count and results queries for optimal performance
        int totalCount = executeCountQuery(baseConditions, isSuccess);
        List<? extends org.jooq.Record> results = executeResultsQuery(baseConditions, isSuccess, pageable);

        return createPageFromSeparateQueries(totalCount, results, pageable);
    }

    @Override
    @Transactional
    public Page<TxnEntity> searchTxnEntitiesOR(Set<String> txHashes,
                                              Set<String> addressHashes,
                                              @Nullable String blockHash,
                                              @Nullable Long blockNumber,
                                              @Nullable Long maxBlock,
                                              @Nullable Boolean isSuccess,
                                              @Nullable Currency currency,
                                              OffsetBasedPageRequest pageable) {
        
        log.debug("Using H2 simple approach for OR search with {} tx hashes and {} address hashes", 
                 txHashes != null ? txHashes.size() : 0,
                 addressHashes != null ? addressHashes.size() : 0);

        // Start with null and build OR conditions properly
        Condition orConditions = null;
        
        // Add txHashes as OR condition if present
        if (txHashes != null && !txHashes.isEmpty()) {
            orConditions = createBatchedInCondition(txHashes);
        }
        
        // Add addressHashes as OR condition if present
        if (addressHashes != null && !addressHashes.isEmpty()) {
            Condition addressCondition = createBatchedInCondition(addressHashes);
            orConditions = orConditions == null ? addressCondition : orConditions.or(addressCondition);
        }
        
        // Add other OR conditions (blockHash, blockNumber, maxBlock, currency)
        if (blockHash != null) {
            Condition blockHashCondition = BLOCK.HASH.eq(blockHash);
            orConditions = orConditions == null ? blockHashCondition : orConditions.or(blockHashCondition);
        }
        
        if (blockNumber != null) {
            Condition blockNumberCondition = BLOCK.NUMBER.eq(blockNumber);
            orConditions = orConditions == null ? blockNumberCondition : orConditions.or(blockNumberCondition);
        }
        
        if (maxBlock != null) {
            Condition maxBlockCondition = BLOCK.NUMBER.le(maxBlock);
            orConditions = orConditions == null ? maxBlockCondition : orConditions.or(maxBlockCondition);
        }
        
        if (currency != null) {
            Condition currencyCondition = getCurrencyConditionBuilder().buildCurrencyCondition(currency);
            orConditions = orConditions == null ? currencyCondition : orConditions.or(currencyCondition);
        }
        
        // If no OR conditions, default to true
        if (orConditions == null) {
            orConditions = DSL.trueCondition();
        }
        
        // Success condition should be ANDed with the result of all OR conditions
        if (isSuccess != null) {
            Condition successCondition = isSuccess
                    ? INVALID_TRANSACTION.TX_HASH.isNull()
                    : INVALID_TRANSACTION.TX_HASH.isNotNull();
            orConditions = orConditions.and(successCondition);
        }

        // Execute separate count and results queries for optimal performance
        int totalCount = executeCountQuery(orConditions, isSuccess);
        List<? extends org.jooq.Record> results = executeResultsQuery(orConditions, isSuccess, pageable);

        return createPageFromSeparateQueries(totalCount, results, pageable);
    }

    /**
     * H2-specific currency condition builder using LIKE operator for JSON string matching.
     */
    private static class H2CurrencyConditionBuilder extends BaseCurrencyConditionBuilder {
        
        @Override
        protected Condition buildPolicyIdAndSymbolCondition(String escapedPolicyId, String escapedSymbol) {
            return DSL.condition("EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                    "AND au.amounts LIKE '%\"policy_id\":\"" + escapedPolicyId + "\"%' " +
                    "AND au.amounts LIKE '%\"asset_name\":\"" + escapedSymbol + "\"%')");
        }

        @Override
        protected Condition buildPolicyIdOnlyCondition(String escapedPolicyId) {
            return DSL.condition("EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                    "AND au.amounts LIKE '%\"policy_id\":\"" + escapedPolicyId + "\"%')");
        }

        @Override
        protected Condition buildLovelaceCondition() {
            return DSL.condition("EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                    "AND au.amounts LIKE '%\"unit\":\"lovelace\"%')");
        }

        @Override
        protected Condition buildSymbolOnlyCondition(String escapedSymbol) {
            return DSL.condition("EXISTS (SELECT 1 FROM address_utxo au WHERE au.tx_hash = transaction.tx_hash " +
                    "AND au.amounts LIKE '%\"asset_name\":\"" + escapedSymbol + "\"%')");
        }
    }

    /**
     * Creates a batched IN condition for H2, handling IN clause size limitations.
     * Simple approach suitable for integration testing.
     */
    private Condition createBatchedInCondition(Set<String> hashes) {
        if (hashes == null || hashes.isEmpty()) {
            return DSL.trueCondition();
        }
        
        List<String> hashList = new ArrayList<>(hashes);
        
        // For small sets, use simple IN clause
        if (hashList.size() <= H2_IN_CLAUSE_LIMIT) {
            return TRANSACTION.TX_HASH.in(hashList);
        }
        
        // For larger sets, batch the IN clauses
        List<List<String>> batches = partitionList(hashList, H2_IN_CLAUSE_LIMIT);
        Condition batchedCondition = DSL.falseCondition();
        for (List<String> batch : batches) {
            batchedCondition = batchedCondition.or(TRANSACTION.TX_HASH.in(batch));
        }
        return batchedCondition;
    }
    
    /**
     * Utility method for partitioning lists into smaller batches.
     */
    private static <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            int end = Math.min(i + batchSize, list.size());
            batches.add(list.subList(i, end));
        }
        return batches;
    }

}