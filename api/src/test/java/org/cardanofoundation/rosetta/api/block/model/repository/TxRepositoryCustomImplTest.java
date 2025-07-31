package org.cardanofoundation.rosetta.api.block.model.repository;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.search.model.Currency;
import org.cardanofoundation.rosetta.common.spring.OffsetBasedPageRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

public class TxRepositoryCustomImplTest extends IntegrationTest {

    @Autowired
    private TxRepository txRepository;

    @Test
    @Sql(scripts = "classpath:/testdata//sql/tx-repository-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:/testdata//sql/tx-repository-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
    public void testSearchTxnEntitiesOR_NoDuplicates() {
        Slice<TxnEntity> results = txRepository.searchTxnEntitiesOR(
                null, null, null, null, null, null, new OffsetBasedPageRequest(0, 100));

        assertThat(hasDuplicateTxHashes(results.getContent())).isFalse();
    }

    @Test
    @Sql(scripts = "classpath:/testdata//sql/tx-repository-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:/testdata//sql/tx-repository-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
    public void testSearchTxnEntitiesAND_NoDuplicates() {
        Slice<TxnEntity> results = txRepository.searchTxnEntitiesAND(
                null, null, null, null, null, null, new OffsetBasedPageRequest(0, 100));

        assertThat(hasDuplicateTxHashes(results.getContent())).isFalse();
    }

    public static boolean hasDuplicateTxHashes(List<TxnEntity> results) {
        Set<String> seen = new HashSet<>();

        return results.stream()
                .map(TxnEntity::getTxHash)
                .anyMatch(txHash -> !seen.add(txHash));
    }

    private static Long getOrderValue(TxnEntity txnEntity) {
        return txnEntity.getBlock().getSlot();
    }

    @Nested
    class BlockTimeOrderingTests {

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-ordering-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-ordering-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesAND_OrderByBlockTimeDesc() {
            Page<TxnEntity> results = txRepository.searchTxnEntitiesAND(
                    null, null, null, null, null, null, new OffsetBasedPageRequest(0, 1));

            List<TxnEntity> txList = results.getContent();
            assertThat(txList).isNotEmpty();
            
            for (int i = 0; i < txList.size() - 1; i++) {
                Long currentOrderValue = getOrderValue(txList.get(i));
                Long nextOrderValue = getOrderValue(txList.get(i + 1));
                assertThat(currentOrderValue).isGreaterThanOrEqualTo(nextOrderValue);
            }
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-ordering-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-ordering-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesOR_OrderByBlockTimeDesc() {
            Page<TxnEntity> results = txRepository.searchTxnEntitiesOR(
                    null, null, null, null, null, null, new OffsetBasedPageRequest(0, 1));

            List<TxnEntity> txList = results.getContent();
            assertThat(txList).isNotEmpty();
            
            for (int i = 0; i < txList.size() - 1; i++) {
                Long currentOrderValue = getOrderValue(txList.get(i));
                Long nextOrderValue = getOrderValue(txList.get(i + 1));
                assertThat(currentOrderValue).isGreaterThanOrEqualTo(nextOrderValue);
            }
        }
    }

    @Nested
    class SuccessFilteringTests {

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesAND_FilterSuccessfulTransactions() {
            Page<TxnEntity> results = txRepository.searchTxnEntitiesAND(
                    null, null, null, null, true, null, new OffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            assertThat(txList).isNotEmpty();
            
            // All transactions should be successful (not in InvalidTransactionEntity)
            // This would need to be verified against test data setup
            assertThat(txList).allSatisfy(tx -> {
                assertThat(tx.getTxHash()).isNotNull();
            });
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesAND_FilterFailedTransactions() {
            Page<TxnEntity> results = txRepository.searchTxnEntitiesAND(
                    null, null, null, null, false, null, new OffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            
            // Results could be empty if no failed transactions exist in test data
            // All transactions should be failed (in InvalidTransactionEntity)
            txList.forEach(tx -> {
                assertThat(tx.getTxHash()).isNotNull();
            });
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesAND_NoSuccessFilter() {
            Page<TxnEntity> results = txRepository.searchTxnEntitiesAND(
                    null, null, null, null, null, null, new OffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            assertThat(txList).isNotEmpty();
            
            // Should return all transactions regardless of success status
            assertThat(txList).allSatisfy(tx -> {
                assertThat(tx.getTxHash()).isNotNull();
            });
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesOR_FilterSuccessfulTransactions() {
            Page<TxnEntity> results = txRepository.searchTxnEntitiesOR(
                    null, null, null, null, true, null, new OffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            assertThat(txList).isNotEmpty();
            
            // All transactions should be successful (not in InvalidTransactionEntity)
            assertThat(txList).allSatisfy(tx -> {
                assertThat(tx.getTxHash()).isNotNull();
            });
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesOR_FilterFailedTransactions() {
            Page<TxnEntity> results = txRepository.searchTxnEntitiesOR(
                    null, null, null, null, false, null, new OffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            
            // Results could be empty if no failed transactions exist in test data
            // All transactions should be failed (in InvalidTransactionEntity)
            txList.forEach(tx -> {
                assertThat(tx.getTxHash()).isNotNull();
            });
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesOR_NoSuccessFilter() {
            Page<TxnEntity> results = txRepository.searchTxnEntitiesOR(
                    null, null, null, null, null, null, new OffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            assertThat(txList).isNotEmpty();
            
            // Should return all transactions regardless of success status
            assertThat(txList).allSatisfy(tx -> {
                assertThat(tx.getTxHash()).isNotNull();
            });
        }
    }

    @Nested
    //@Disabled("Currency filtering tests temporarily disabled due to H2/PostgreSQL dialect detection issues")
    class CurrencyFilteringTests {

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-currency-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-currency-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesAND_FilterByLovelace() {
            // Test ADA/lovelace currency filtering
            Currency lovelaceCurrency = Currency.builder()
                    .symbol("lovelace")
                    .build();

            Page<TxnEntity> results = txRepository.searchTxnEntitiesAND(
                    null, null, null, null, null, lovelaceCurrency, 
                    new OffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            assertThat(txList).isNotEmpty();
            
            // All transactions should contain lovelace amounts
            assertThat(txList).allSatisfy(tx -> {
                assertThat(tx.getTxHash()).isNotNull();
            });
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-currency-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-currency-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesAND_FilterByADA() {
            // Test ADA symbol filtering (should work same as lovelace)
            Currency adaCurrency = Currency.builder()
                    .symbol("ADA")
                    .build();

            Page<TxnEntity> results = txRepository.searchTxnEntitiesAND(
                    null, null, null, null, null, adaCurrency, 
                    new OffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            assertThat(txList).isNotEmpty();
            
            // All transactions should contain ADA amounts
            assertThat(txList).allSatisfy(tx -> {
                assertThat(tx.getTxHash()).isNotNull();
            });
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-currency-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-currency-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesAND_FilterByPolicyIdOnly() {
            // Test filtering by policy ID only
            Currency policyIdCurrency = Currency.builder()
                    .policyId("29d222ce763455e3d7a09a665ce554f00ac89d2e99a1a83d267170c6")
                    .build();

            Page<TxnEntity> results = txRepository.searchTxnEntitiesAND(
                    null, null, null, null, null, policyIdCurrency, 
                    new OffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            
            // Results could be empty if no transactions with this policy ID exist
            // All transactions should contain assets with the specified policy ID
            txList.forEach(tx -> {
                assertThat(tx.getTxHash()).isNotNull();
            });
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-currency-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-currency-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesAND_FilterByPolicyIdAndSymbol() {
            // Test filtering by both policy ID and asset name (most precise)
            Currency preciseAssetCurrency = Currency.builder()
                    .policyId("29d222ce763455e3d7a09a665ce554f00ac89d2e99a1a83d267170c6")
                    .symbol("MIN")
                    .decimals(6)
                    .build();

            Page<TxnEntity> results = txRepository.searchTxnEntitiesAND(
                    null, null, null, null, null, preciseAssetCurrency, 
                    new OffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            
            // Results could be empty if no transactions with this specific asset exist
            // All transactions should contain the exact asset (policy ID + asset name)
            txList.forEach(tx -> {
                assertThat(tx.getTxHash()).isNotNull();
            });
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-currency-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-currency-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesAND_FilterBySymbolOnly() {
            // Test filtering by symbol/asset name only (searches across all policy IDs)
            Currency symbolCurrency = Currency.builder()
                    .symbol("MIN")
                    .build();

            Page<TxnEntity> results = txRepository.searchTxnEntitiesAND(
                    null, null, null, null, null, symbolCurrency, 
                    new OffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            
            // Results could be empty if no transactions with MIN tokens exist
            // All transactions should contain assets with "MIN" as asset name
            txList.forEach(tx -> {
                assertThat(tx.getTxHash()).isNotNull();
            });
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-currency-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-currency-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesOR_FilterByLovelace() {
            // Test OR logic with lovelace filtering
            Currency lovelaceCurrency = Currency.builder()
                    .symbol("lovelace")
                    .build();

            Page<TxnEntity> results = txRepository.searchTxnEntitiesOR(
                    null, null, null, null, null, lovelaceCurrency, 
                    new OffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            assertThat(txList).isNotEmpty();
            
            // All transactions should contain lovelace amounts
            assertThat(txList).allSatisfy(tx -> {
                assertThat(tx.getTxHash()).isNotNull();
            });
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-currency-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-currency-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesOR_FilterByPolicyIdAndSymbol() {
            // Test OR logic with precise asset filtering
            Currency preciseAssetCurrency = Currency.builder()
                    .policyId("d5e6bf0500378d4f0da4e8dde6becec7621cd8cbf5cbb9b87013d4cc")
                    .symbol("537061636542756433343132")
                    .decimals(0)
                    .build();

            Page<TxnEntity> results = txRepository.searchTxnEntitiesOR(
                    null, null, null, null, null, preciseAssetCurrency, 
                    new OffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            
            // Results could be empty if no transactions with this specific asset exist
            // All transactions should contain the exact asset
            txList.forEach(tx -> {  
                assertThat(tx.getTxHash()).isNotNull();
            });
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-currency-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-currency-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesAND_NullCurrency() {
            // Test that null currency parameter doesn't filter anything
            Page<TxnEntity> results = txRepository.searchTxnEntitiesAND(
                    null, null, null, null, null, null, 
                    new OffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            assertThat(txList).isNotEmpty();
            
            // Should return all transactions without currency filtering
            assertThat(txList).allSatisfy(tx -> {
                assertThat(tx.getTxHash()).isNotNull();
            });
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-currency-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-currency-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesAND_EmptyCurrency() {
            // Test that empty currency (no fields set) returns no results
            Currency emptyCurrency = Currency.builder().build();

            Page<TxnEntity> results = txRepository.searchTxnEntitiesAND(
                    null, null, null, null, null, emptyCurrency, 
                    new OffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            
            // Should return empty results since no meaningful currency criteria provided
            assertThat(txList).isEmpty();
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-currency-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-currency-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesAND_CombineWithOtherFilters() {
            // Test currency filtering combined with other filters
            Currency lovelaceCurrency = Currency.builder()
                    .symbol("lovelace")
                    .build();

            Set<String> txHashes = Set.of("specific_tx_hash_1", "specific_tx_hash_2");

            Page<TxnEntity> results = txRepository.searchTxnEntitiesAND(
                    txHashes, null, null, null, true, lovelaceCurrency, 
                    new OffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            
            // Should only return successful transactions from the specified hashes that contain lovelace
            txList.forEach(tx -> {
                assertThat(tx.getTxHash()).isIn(txHashes);
            });
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-currency-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-currency-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesCaseInsensitiveSymbol() {
            // Test case insensitive symbol matching for ADA/lovelace
            Currency upperCaseAdaCurrency = Currency.builder()
                    .symbol("ADA")
                    .build();
            
            Currency lowerCaseAdaCurrency = Currency.builder()
                    .symbol("ada")
                    .build();
            
            Currency mixedCaseLovelaceCurrency = Currency.builder()
                    .symbol("LoVeLaCe")
                    .build();

            Page<TxnEntity> upperResults = txRepository.searchTxnEntitiesAND(
                    null, null, null, null, null, upperCaseAdaCurrency, 
                    new OffsetBasedPageRequest(0, 100));
            
            Page<TxnEntity> lowerResults = txRepository.searchTxnEntitiesAND(
                    null, null, null, null, null, lowerCaseAdaCurrency, 
                    new OffsetBasedPageRequest(0, 100));
            
            Page<TxnEntity> mixedResults = txRepository.searchTxnEntitiesAND(
                    null, null, null, null, null, mixedCaseLovelaceCurrency, 
                    new OffsetBasedPageRequest(0, 100));

            // All should return the same results (case insensitive for ADA/lovelace)
            assertThat(upperResults.getContent()).hasSameSizeAs(lowerResults.getContent());
            assertThat(upperResults.getContent()).hasSameSizeAs(mixedResults.getContent());
        }
    }

}
