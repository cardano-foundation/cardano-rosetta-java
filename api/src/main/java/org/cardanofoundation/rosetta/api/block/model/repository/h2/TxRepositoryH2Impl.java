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

    @Override
    @Transactional
    protected Page<TxnEntity> searchTxnEntitiesANDWithLargeHashSet(Set<String> txHashes,
                                                                  @Nullable String blockHash,
                                                                  @Nullable Long blockNumber,
                                                                  @Nullable Long maxBlock,
                                                                  @Nullable Boolean isSuccess,
                                                                  @Nullable Currency currency,
                                                                  OffsetBasedPageRequest pageable) {
        if (txHashes == null || txHashes.isEmpty()) {
            return searchTxnEntitiesAND(null, blockHash, blockNumber, maxBlock, isSuccess, currency, pageable);
        }

        log.debug("Using H2 batch approach for AND search with {} transaction hashes", txHashes.size());

        // For H2, we'll use a series of IN clauses with batches
        List<String> hashList = new ArrayList<>(txHashes);
        List<List<String>> batches = partitionList(hashList, H2_IN_CLAUSE_LIMIT);

        // Build base conditions without txHashes
        Condition baseConditions = queryBuilder.buildAndConditions(null, blockHash, blockNumber, maxBlock, isSuccess, currency, getCurrencyConditionBuilder());

        // Create a condition for all batches using OR
        Condition hashCondition = DSL.falseCondition();
        for (List<String> batch : batches) {
            hashCondition = hashCondition.or(TRANSACTION.TX_HASH.in(batch));
        }

        // Combine with AND
        Condition finalConditions = baseConditions.and(hashCondition);

        // Execute count query first (faster without window function)
        int totalCount = executeCountQuery(finalConditions, isSuccess, blockHash != null || blockNumber != null || maxBlock != null, false);
        
        // Execute results query (without COUNT(*) OVER())
        List<? extends org.jooq.Record> results = executeResultsQuery(finalConditions, isSuccess, false, pageable);

        return createPageFromSeparateQueries(totalCount, results, pageable);
    }

    @Override
    @Transactional
    protected Page<TxnEntity> searchTxnEntitiesORWithLargeHashSet(Set<String> txHashes,
                                                                 @Nullable String blockHash,
                                                                 @Nullable Long blockNumber,
                                                                 @Nullable Long maxBlock,
                                                                 @Nullable Boolean isSuccess,
                                                                 @Nullable Currency currency,
                                                                 OffsetBasedPageRequest pageable) {
        if (txHashes == null || txHashes.isEmpty()) {
            return searchTxnEntitiesOR(null, blockHash, blockNumber, maxBlock, isSuccess, currency, pageable);
        }

        log.debug("Using H2 batch approach for OR search with {} transaction hashes", txHashes.size());

        // For H2, we'll use a series of IN clauses with batches
        List<String> hashList = new ArrayList<>(txHashes);
        List<List<String>> batches = partitionList(hashList, H2_IN_CLAUSE_LIMIT);

        // Build base OR conditions without txHashes
        Condition baseOrConditions = queryBuilder.buildOrConditions(null, blockHash, blockNumber, maxBlock, isSuccess, currency, getCurrencyConditionBuilder());

        // Add hash conditions as OR
        for (List<String> batch : batches) {
            baseOrConditions = baseOrConditions.or(TRANSACTION.TX_HASH.in(batch));
        }

        // Execute count query first (faster without window function)
        int totalCount = executeOrCountQuery(baseOrConditions, isSuccess, true);
        
        // Execute results query (without COUNT(*) OVER())
        List<? extends org.jooq.Record> results = executeOrResultsQuery(baseOrConditions, isSuccess, true, pageable);

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

}