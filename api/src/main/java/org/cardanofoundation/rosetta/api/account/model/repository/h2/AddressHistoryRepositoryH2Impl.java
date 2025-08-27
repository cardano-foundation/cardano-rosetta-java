package org.cardanofoundation.rosetta.api.account.model.repository.h2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.account.model.repository.AddressHistoryRepository;
import org.jooq.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.cardanofoundation.rosetta.api.jooq.Tables.*;

/**
 * H2-specific implementation of AddressHistoryRepository using JOOQ.
 * Uses simpler queries optimized for H2's capabilities.
 */
@Slf4j
@Repository
@Profile({"h2", "test-integration"})
@RequiredArgsConstructor
public class AddressHistoryRepositoryH2Impl implements AddressHistoryRepository {

    private final DSLContext dsl;

    @Override
    @Transactional(readOnly = true)
    public List<String> findCompleteTransactionHistoryByAddress(String address) {
        log.debug("Finding complete transaction history for address: {} using H2 optimized queries", address);

        // H2 doesn't handle CTEs as efficiently as PostgreSQL, so we'll use two separate queries
        // and combine the results in Java
        
        // Query 1: Get all transactions where the address received outputs
        List<String> outputTransactions = dsl.selectDistinct(ADDRESS_UTXO.TX_HASH)
                .from(ADDRESS_UTXO)
                .where(ADDRESS_UTXO.OWNER_ADDR.eq(address)
                       .or(ADDRESS_UTXO.OWNER_STAKE_ADDR.eq(address)))
                .fetch()
                .map(Record1::value1);

        // Query 2: Get all transactions where the address's outputs were spent as inputs
        List<String> inputTransactions = dsl.selectDistinct(TX_INPUT.SPENT_TX_HASH)
                .from(TX_INPUT)
                .innerJoin(ADDRESS_UTXO)
                .on(TX_INPUT.TX_HASH.eq(ADDRESS_UTXO.TX_HASH)
                    .and(TX_INPUT.OUTPUT_INDEX.eq(ADDRESS_UTXO.OUTPUT_INDEX)))
                .where(ADDRESS_UTXO.OWNER_ADDR.eq(address)
                       .or(ADDRESS_UTXO.OWNER_STAKE_ADDR.eq(address)))
                .fetch()
                .map(Record1::value1);

        // Combine results and remove duplicates
        Set<String> allTransactions = new HashSet<>();
        allTransactions.addAll(outputTransactions);
        allTransactions.addAll(inputTransactions);

        return List.copyOf(allTransactions);
    }

}