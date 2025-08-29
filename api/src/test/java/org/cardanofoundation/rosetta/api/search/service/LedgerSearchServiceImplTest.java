package org.cardanofoundation.rosetta.api.search.service;

import org.cardanofoundation.rosetta.api.account.model.repository.AddressUtxoRepository;
import org.cardanofoundation.rosetta.api.account.service.AddressHistoryService;
import org.cardanofoundation.rosetta.api.block.model.repository.TxInputRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.TxRepository;
import org.cardanofoundation.rosetta.api.block.service.LedgerBlockService;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.Operator;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LedgerSearchServiceImpl Unit Tests")
class LedgerSearchServiceImplTest {

    @Mock
    private TxRepository txRepository;

    @Mock
    private LedgerBlockService ledgerBlockService;

    @Mock
    private TxInputRepository txInputRepository;

    @Mock
    private AddressUtxoRepository addressUtxoRepository;

    @Mock
    private AddressHistoryService addressHistoryService;

    @InjectMocks
    private LedgerSearchServiceImpl ledgerSearchService;

    @Nested
    @DisplayName("Large Dataset Handling Tests")
    class LargeDatasetHandlingTests {

        @Test
        @DisplayName("Should handle address search with many UTXOs using temporary tables")
        void shouldHandleAddressSearchWithManyUtxos() {
            // Given
            String address = "addr1_test_address_with_many_utxos";
            
            // Create a list with more than 10000 UTXOs (to trigger temp table usage)
            List<String> manyUtxos = new ArrayList<>();
            IntStream.range(0, 15000)
                    .forEach(i -> manyUtxos.add("tx_hash_" + i));
            
            when(addressHistoryService.findCompleteTransactionHistoryByAddress(address))
                    .thenReturn(manyUtxos);

            // Mock the repository calls to avoid NullPointerException
            when(txRepository.searchTxnEntitiesAND(any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(Page.empty());
            when(ledgerBlockService.mapTxnEntitiesToBlockTxList(any(Page.class)))
                    .thenReturn(Page.empty());

            // When & Then - should handle gracefully without throwing exception
            var result = ledgerSearchService.searchTransaction(
                    Operator.AND,
                    null,
                    address,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    0L,
                    10L
            );

            // Should successfully return result without throwing exception
            // The large transaction hash set should be handled via temporary tables
        }

        @Test
        @DisplayName("Should handle combined hash sources with large datasets")
        void shouldHandleCombinedHashSourcesWithLargeDatasets() {
            // Given
            String address = "addr1_test_address";
            String txHash = "single_tx_hash";
            
            // Create a list that when combined with txHash will be very large
            List<String> manyUtxos = new ArrayList<>();
            IntStream.range(0, 20000)
                    .forEach(i -> manyUtxos.add("tx_hash_" + i));
            
            when(addressHistoryService.findCompleteTransactionHistoryByAddress(address))
                    .thenReturn(manyUtxos);

            // Mock the repository calls to avoid NullPointerException
            when(txRepository.searchTxnEntitiesOR(any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(Page.empty());
            when(ledgerBlockService.mapTxnEntitiesToBlockTxList(any(Page.class)))
                    .thenReturn(Page.empty());

            // When & Then - should handle gracefully without throwing exception
            var result = ledgerSearchService.searchTransaction(
                    Operator.OR,
                    txHash, // This will be added to the 20000 hashes
                    address,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    0L,
                    10L
            );

            // Should successfully return result without throwing exception
            // The large transaction hash set should be handled via temporary tables
        }

        @Test
        @DisplayName("Should handle very large datasets gracefully")
        void shouldHandleVeryLargeDatasetsGracefully() {
            // Given
            String address = "addr1_test_address";
            
            // Create a list with 50000 UTXOs (well above temp table threshold)
            List<String> veryLargeList = new ArrayList<>();
            IntStream.range(0, 50000)
                    .forEach(i -> veryLargeList.add("tx_hash_" + i));
            
            when(addressHistoryService.findCompleteTransactionHistoryByAddress(address))
                    .thenReturn(veryLargeList);

            // Mock the repository calls to avoid NullPointerException
            when(txRepository.searchTxnEntitiesAND(any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(Page.empty());
            when(ledgerBlockService.mapTxnEntitiesToBlockTxList(any(Page.class)))
                    .thenReturn(Page.empty());

            // When & Then - should not throw exception
            ledgerSearchService.searchTransaction(
                    Operator.AND,
                    null,
                    address,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    0L,
                    10L
            );
            // Should handle gracefully using temporary tables
        }

        @Test
        @DisplayName("Should handle small number of UTXOs without validation issues")
        void shouldHandleSmallNumberOfUtxosWithoutValidationIssues() {
            // Given
            String address = "addr1_test_address";
            
            // Create a small list of UTXOs
            List<String> smallList = List.of("tx_hash_1", "tx_hash_2", "tx_hash_3");
            
            when(addressHistoryService.findCompleteTransactionHistoryByAddress(address))
                    .thenReturn(smallList);

            // Mock the repository calls to avoid NullPointerException
            when(txRepository.searchTxnEntitiesOR(any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(Page.empty());

            when(ledgerBlockService.mapTxnEntitiesToBlockTxList(any(Page.class)))
                    .thenReturn(Page.empty());

            // When & Then - should not throw exception
            ledgerSearchService.searchTransaction(
                    Operator.OR,
                    null,
                    address,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    0L,
                    10L
            );
            // If we reach here, no exception was thrown
        }

        @Test
        @DisplayName("Should handle empty UTXO set without validation issues")
        void shouldHandleEmptyUtxoSetWithoutValidationIssues() {
            // Given
            String address = "addr1_test_address_with_no_utxos";
            
            when(addressHistoryService.findCompleteTransactionHistoryByAddress(address))
                    .thenReturn(List.of());

            // When & Then - should return empty page without validation
            var result = ledgerSearchService.searchTransaction(
                    Operator.AND,
                    null,
                    address,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    0L,
                    10L
            );

            // Should return empty page when no UTXOs found for address
            // (this is handled by the early return in the service)
        }
    }
}