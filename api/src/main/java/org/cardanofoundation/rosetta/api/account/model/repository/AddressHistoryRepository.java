package org.cardanofoundation.rosetta.api.account.model.repository;

import java.util.List;

/**
 * Repository interface for retrieving transaction history for addresses.
 * Provides optimized database-specific implementations using JOOQ.
 */
public interface AddressHistoryRepository {
    
    /**
     * Finds complete transaction history for an address including both outputs and inputs.
     * This includes:
     * - All transactions where the address received outputs
     * - All transactions where the address's outputs were spent as inputs
     * 
     * @param address The address to search for (can be payment or stake address)
     * @return List of transaction hashes related to the address
     */
    List<String> findCompleteTransactionHistoryByAddress(String address);

}