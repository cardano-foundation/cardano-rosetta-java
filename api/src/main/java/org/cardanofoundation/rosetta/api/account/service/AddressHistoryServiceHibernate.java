package org.cardanofoundation.rosetta.api.account.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.account.model.repository.AddressUtxoRepository;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.ShutdownOnFailure;
import java.util.concurrent.TimeoutException;

/**
 * Hibernate-based implementation of AddressHistoryService.
 * Uses JPA queries with server-side union via LinkedHashSet.
 * 
 * This implementation is activated when address-history.implementation=hibernate
 * or when JOOQ is not available.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "address-history.implementation", havingValue = "hibernate", matchIfMissing = true)
@RequiredArgsConstructor
public class AddressHistoryServiceHibernate implements AddressHistoryService {

    private final AddressUtxoRepository addressUtxoRepository;
    private final Clock clock;

    private int addressHistoryApiTimeoutSecs = 60;

    @PostConstruct
    public void init() {
        log.info("AddressHistoryServiceHibernate...");
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findCompleteTransactionHistoryByAddress(String address) {
        log.debug("Finding complete transaction history for address: {} using Hibernate with parallel fork-join", address);

        try (ShutdownOnFailure scope = new ShutdownOnFailure()) {
            StructuredTaskScope.Subtask<List<String>> outputTransactions = scope.fork(() -> 
                    addressUtxoRepository.findOutputTransactionsByAddress(address));

            StructuredTaskScope.Subtask<List<String>> inputTransactions = scope.fork(() ->
                    addressUtxoRepository.findInputTransactionsByAddress(address));

            scope.joinUntil(Instant.now(clock).plusSeconds(addressHistoryApiTimeoutSecs));
            scope.throwIfFailed();

            // Use LinkedHashSet to preserve order while removing duplicates
            Set<String> allTransactions = new LinkedHashSet<>();
            allTransactions.addAll(outputTransactions.get());
            allTransactions.addAll(inputTransactions.get());

            return List.copyOf(allTransactions);
        } catch (ExecutionException e) {
            log.error("Error fetching address transaction history", e);
            throw ExceptionFactory.unspecifiedError("Error fetching address transaction history, msg:%s".formatted(e.getMessage()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Error fetching address transaction history", e);
            throw ExceptionFactory.unspecifiedError("Error fetching address transaction history, msg:%s".formatted(e.getMessage()));
        } catch (TimeoutException e) {
            log.error("Timeout fetching address transaction history", e);
            throw ExceptionFactory.timeOut("timeout while fetching address transaction history from db.");
        }
    }

}
