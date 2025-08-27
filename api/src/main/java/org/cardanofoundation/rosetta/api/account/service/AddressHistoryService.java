package org.cardanofoundation.rosetta.api.account.service;

import java.util.List;

/**
 * Service for retrieving transaction history for addresses.
 * This is the service layer that orchestrates data access and business logic.
 */
public interface AddressHistoryService {
    
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