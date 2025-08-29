package org.cardanofoundation.rosetta.api.account.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.account.model.repository.AddressHistoryRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * JOOQ-based implementation of AddressHistoryService.
 * Delegates to the database-specific JOOQ repository implementations for optimal performance.
 * 
 * This implementation is activated when address-history.implementation=jooq
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "address-history.implementation", havingValue = "jooq", matchIfMissing = true)
@RequiredArgsConstructor
public class AddressHistoryServiceJooq implements AddressHistoryService {

    private final AddressHistoryRepository addressHistoryRepository;

    @PostConstruct
    public void init() {
        log.info("AddressHistoryServiceJooq...");
    }

    @Override
    public List<String> findCompleteTransactionHistoryByAddress(String address) {
        log.debug("Using JOOQ-based address history repository for address: {}", address);

        return addressHistoryRepository.findCompleteTransactionHistoryByAddress(address);
    }

}