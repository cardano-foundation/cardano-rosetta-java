package org.cardanofoundation.rosetta.api.account.model.repository.postgresql;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.account.model.repository.AddressHistoryRepository;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.cardanofoundation.rosetta.api.jooq.Tables.*;

/**
 * PostgreSQL-specific implementation of AddressHistoryRepository using JOOQ.
 * Optimized for PostgreSQL using CTEs (Common Table Expressions).
 */
@Slf4j
@Repository
@Profile({"!h2 & !test-integration"})
@RequiredArgsConstructor
public class AddressHistoryRepositoryPostgresImpl implements AddressHistoryRepository {

    private final DSLContext dsl;

    @Override
    @Transactional(readOnly = true)
    public List<String> findCompleteTransactionHistoryByAddress(String address) {
        // CTE for address outputs - all transactions where the address received outputs
        CommonTableExpression<Record1<String>> addressOutputs = DSL.name("address_outputs")
                .fields("tx_hash")
                .as(
                    dsl.selectDistinct(ADDRESS_UTXO.TX_HASH)
                       .from(ADDRESS_UTXO)
                       .where(ADDRESS_UTXO.OWNER_ADDR.eq(address)
                              .or(ADDRESS_UTXO.OWNER_STAKE_ADDR.eq(address)))
                );

        // CTE for address inputs - all transactions where the address's outputs were spent
        CommonTableExpression<Record1<String>> addressInputs = DSL.name("address_inputs")
                .fields("tx_hash")
                .as(
                    dsl.selectDistinct(TX_INPUT.SPENT_TX_HASH.as("tx_hash"))
                       .from(TX_INPUT)
                       .innerJoin(ADDRESS_UTXO)
                       .on(TX_INPUT.TX_HASH.eq(ADDRESS_UTXO.TX_HASH)
                           .and(TX_INPUT.OUTPUT_INDEX.eq(ADDRESS_UTXO.OUTPUT_INDEX)))
                       .where(ADDRESS_UTXO.OWNER_ADDR.eq(address)
                              .or(ADDRESS_UTXO.OWNER_STAKE_ADDR.eq(address)))
                );

        return dsl.with(addressOutputs)
                .with(addressInputs)
                .select(DSL.field("tx_hash", String.class))
                .from(addressOutputs)
                .union(
                    dsl.select(DSL.field("tx_hash", String.class))
                       .from(addressInputs)
                )
                .fetch()
                .map(Record1::value1);
    }

}