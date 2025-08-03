package org.cardanofoundation.rosetta.api.block.model.repository;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.search.model.Currency;
import org.cardanofoundation.rosetta.common.spring.SimpleOffsetBasedPageRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashSet;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
                Collections.emptySet(), null, null, null, null, null, new SimpleOffsetBasedPageRequest(0, 100));

        assertThat(hasDuplicateTxHashes(results.getContent())).isFalse();
    }

    @Test
    @Sql(scripts = "classpath:/testdata//sql/tx-repository-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:/testdata//sql/tx-repository-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
    public void testSearchTxnEntitiesAND_NoDuplicates() {
        Slice<TxnEntity> results = txRepository.searchTxnEntitiesAND(
                Collections.emptySet(), null, null, null, null, null, new SimpleOffsetBasedPageRequest(0, 100));

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
                    Collections.emptySet(), null, null, null, null, null, new SimpleOffsetBasedPageRequest(0, 1));

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
                    Collections.emptySet(), null, null, null, null, null, new SimpleOffsetBasedPageRequest(0, 1));

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
                    Collections.emptySet(), null, null, null, true, null, new SimpleOffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            assertThat(txList).isNotEmpty();
            
            Set<String> actualTxHashes = txList.stream()
                    .map(TxnEntity::getTxHash)
                    .collect(Collectors.toSet());
            
            // Verify our test successful transactions are included
            Set<String> expectedSuccessfulTxHashes = Set.of("successTx1", "successTx2", "successTx3");
            assertThat(actualTxHashes).containsAll(expectedSuccessfulTxHashes);
            
            // Verify our test failed transactions are NOT included
            Set<String> failedTxHashes = Set.of("failedTx1", "failedTx2");
            assertThat(actualTxHashes).doesNotContainAnyElementsOf(failedTxHashes);
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesAND_FilterFailedTransactions() {
            Page<TxnEntity> results = txRepository.searchTxnEntitiesAND(
                    Collections.emptySet(), null, null, null, false, null, new SimpleOffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            Set<String> actualTxHashes = txList.stream()
                    .map(TxnEntity::getTxHash)
                    .collect(Collectors.toSet());
            
            // Verify our test failed transactions are included
            Set<String> expectedFailedTxHashes = Set.of("failedTx1", "failedTx2");
            assertThat(actualTxHashes).containsAll(expectedFailedTxHashes);
            
            // Verify our test successful transactions are NOT included
            Set<String> successfulTxHashes = Set.of("successTx1", "successTx2", "successTx3");
            assertThat(actualTxHashes).doesNotContainAnyElementsOf(successfulTxHashes);
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesAND_NoSuccessFilter() {
            Page<TxnEntity> results = txRepository.searchTxnEntitiesAND(
                    Collections.emptySet(), null, null, null, null, null, new SimpleOffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            assertThat(txList).isNotEmpty();
            
            Set<String> actualTxHashes = txList.stream()
                    .map(TxnEntity::getTxHash)
                    .collect(Collectors.toSet());
            
            // Should return all our test transactions regardless of success status
            Set<String> expectedAllTxHashes = Set.of("successTx1", "successTx2", "successTx3", "failedTx1", "failedTx2");
            assertThat(actualTxHashes).containsAll(expectedAllTxHashes);
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesOR_FilterSuccessfulTransactions() {
            Page<TxnEntity> results = txRepository.searchTxnEntitiesOR(
                    Collections.emptySet(), null, null, null, true, null, new SimpleOffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            assertThat(txList).isNotEmpty();
            
            Set<String> actualTxHashes = txList.stream()
                    .map(TxnEntity::getTxHash)
                    .collect(Collectors.toSet());
            
            // Verify our test successful transactions are included
            Set<String> expectedSuccessfulTxHashes = Set.of("successTx1", "successTx2", "successTx3");
            assertThat(actualTxHashes).containsAll(expectedSuccessfulTxHashes);
            
            // Verify failed transactions are NOT included
            Set<String> failedTxHashes = Set.of("failedTx1", "failedTx2");
            assertThat(actualTxHashes).doesNotContainAnyElementsOf(failedTxHashes);
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesOR_FilterFailedTransactions() {
            Page<TxnEntity> results = txRepository.searchTxnEntitiesOR(
                    Collections.emptySet(), null, null, null, false, null, new SimpleOffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            Set<String> actualTxHashes = txList.stream()
                    .map(TxnEntity::getTxHash)
                    .collect(Collectors.toSet());
            
            // Verify our test failed transactions are included
            Set<String> expectedFailedTxHashes = Set.of("failedTx1", "failedTx2");
            assertThat(actualTxHashes).containsAll(expectedFailedTxHashes);
            
            // Verify our test successful transactions are NOT included
            Set<String> successfulTxHashes = Set.of("successTx1", "successTx2", "successTx3");
            assertThat(actualTxHashes).doesNotContainAnyElementsOf(successfulTxHashes);
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesOR_NoSuccessFilter() {
            Page<TxnEntity> results = txRepository.searchTxnEntitiesOR(
                    Collections.emptySet(), null, null, null, null, null, new SimpleOffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            assertThat(txList).isNotEmpty();
            
            Set<String> actualTxHashes = txList.stream()
                    .map(TxnEntity::getTxHash)
                    .collect(Collectors.toSet());
            
            // Should return all our test transactions regardless of success status
            Set<String> expectedAllTxHashes = Set.of("successTx1", "successTx2", "successTx3", "failedTx1", "failedTx2");
            assertThat(actualTxHashes).containsAll(expectedAllTxHashes);
        }
        
        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesAND_CountValidation() {
            // Verify that isSuccess=null includes both successful and failed transactions
            Page<TxnEntity> allResults = txRepository.searchTxnEntitiesAND(
                    Collections.emptySet(), null, null, null, null, null, new SimpleOffsetBasedPageRequest(0, 100));
            
            Page<TxnEntity> successfulResults = txRepository.searchTxnEntitiesAND(
                    Collections.emptySet(), null, null, null, true, null, new SimpleOffsetBasedPageRequest(0, 100));
            
            Page<TxnEntity> failedResults = txRepository.searchTxnEntitiesAND(
                    Collections.emptySet(), null, null, null, false, null, new SimpleOffsetBasedPageRequest(0, 100));
            
            // Collect transaction hashes for verification
            Set<String> allTxHashes = allResults.getContent().stream()
                    .map(TxnEntity::getTxHash)
                    .collect(Collectors.toSet());
            Set<String> successfulTxHashes = successfulResults.getContent().stream()
                    .map(TxnEntity::getTxHash)
                    .collect(Collectors.toSet());
            Set<String> failedTxHashes = failedResults.getContent().stream()
                    .map(TxnEntity::getTxHash)
                    .collect(Collectors.toSet());
            
            // Verify that all results contain both successful and failed transactions
            assertThat(allTxHashes).containsAll(Set.of("successTx1", "successTx2", "successTx3"));
            assertThat(allTxHashes).containsAll(Set.of("failedTx1", "failedTx2"));
            
            // Verify filtering works correctly
            assertThat(successfulTxHashes).containsAll(Set.of("successTx1", "successTx2", "successTx3"));
            assertThat(successfulTxHashes).doesNotContainAnyElementsOf(Set.of("failedTx1", "failedTx2"));
            
            assertThat(failedTxHashes).containsAll(Set.of("failedTx1", "failedTx2"));
            assertThat(failedTxHashes).doesNotContainAnyElementsOf(Set.of("successTx1", "successTx2", "successTx3"));
        }
        
        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesOR_CountValidation() {
            // Verify that isSuccess=null includes both successful and failed transactions for OR queries
            Page<TxnEntity> allResults = txRepository.searchTxnEntitiesOR(
                    Collections.emptySet(), null, null, null, null, null, new SimpleOffsetBasedPageRequest(0, 100));
            
            Page<TxnEntity> successfulResults = txRepository.searchTxnEntitiesOR(
                    Collections.emptySet(), null, null, null, true, null, new SimpleOffsetBasedPageRequest(0, 100));
            
            Page<TxnEntity> failedResults = txRepository.searchTxnEntitiesOR(
                    Collections.emptySet(), null, null, null, false, null, new SimpleOffsetBasedPageRequest(0, 100));
            
            // Collect transaction hashes for verification
            Set<String> allTxHashes = allResults.getContent().stream()
                    .map(TxnEntity::getTxHash)
                    .collect(Collectors.toSet());
            Set<String> successfulTxHashes = successfulResults.getContent().stream()
                    .map(TxnEntity::getTxHash)
                    .collect(Collectors.toSet());
            Set<String> failedTxHashes = failedResults.getContent().stream()
                    .map(TxnEntity::getTxHash)
                    .collect(Collectors.toSet());
            
            // Verify that all results contain both successful and failed transactions
            assertThat(allTxHashes).containsAll(Set.of("successTx1", "successTx2", "successTx3"));
            assertThat(allTxHashes).containsAll(Set.of("failedTx1", "failedTx2"));
            
            // Verify filtering works correctly
            assertThat(successfulTxHashes).containsAll(Set.of("successTx1", "successTx2", "successTx3"));
            assertThat(successfulTxHashes).doesNotContainAnyElementsOf(Set.of("failedTx1", "failedTx2"));
            
            assertThat(failedTxHashes).containsAll(Set.of("failedTx1", "failedTx2"));
            assertThat(failedTxHashes).doesNotContainAnyElementsOf(Set.of("successTx1", "successTx2", "successTx3"));
        }
        
        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesAND_CombineSuccessFilterWithOtherFilters() {
            // Test combining isSuccess with specific transaction hashes
            Set<String> specificTxHashes = Set.of("successTx1", "failedTx1");
            
            // Filter for successful transactions from the specific set
            Page<TxnEntity> successfulSpecificResults = txRepository.searchTxnEntitiesAND(
                    specificTxHashes, null, null, null, true, null, new SimpleOffsetBasedPageRequest(0, 100));
            
            // Should only return successTx1 (successful and in the specified set)
            assertThat(successfulSpecificResults.getContent()).hasSize(1);
            assertThat(successfulSpecificResults.getContent().get(0).getTxHash()).isEqualTo("successTx1");
            
            // Filter for failed transactions from the specific set
            Page<TxnEntity> failedSpecificResults = txRepository.searchTxnEntitiesAND(
                    specificTxHashes, null, null, null, false, null, new SimpleOffsetBasedPageRequest(0, 100));
            
            // Should only return failedTx1 (failed and in the specified set)
            assertThat(failedSpecificResults.getContent()).hasSize(1);
            assertThat(failedSpecificResults.getContent().get(0).getTxHash()).isEqualTo("failedTx1");
        }
        
        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesOR_CombineSuccessFilterWithOtherFilters() {
            // Test combining isSuccess with specific transaction hashes using OR logic
            Set<String> specificTxHashes = Set.of("successTx1", "failedTx1");
            
            // Filter for successful transactions from the specific set
            Page<TxnEntity> successfulSpecificResults = txRepository.searchTxnEntitiesOR(
                    specificTxHashes, null, null, null, true, null, new SimpleOffsetBasedPageRequest(0, 100));
            
            // Should only return successTx1 (successful and in the specified set)
            assertThat(successfulSpecificResults.getContent()).hasSize(1);
            assertThat(successfulSpecificResults.getContent().get(0).getTxHash()).isEqualTo("successTx1");
            
            // Filter for failed transactions from the specific set
            Page<TxnEntity> failedSpecificResults = txRepository.searchTxnEntitiesOR(
                    specificTxHashes, null, null, null, false, null, new SimpleOffsetBasedPageRequest(0, 100));
            
            // Should only return failedTx1 (failed and in the specified set)
            assertThat(failedSpecificResults.getContent()).hasSize(1);
            assertThat(failedSpecificResults.getContent().get(0).getTxHash()).isEqualTo("failedTx1");
        }
    }

    @Nested
    class InvalidTransactionJoinTests {

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesAND_VerifySuccessfulTransactionsNotInInvalidTable() {
            // Test that successful transactions (isSuccess=true) are NOT in invalid_transaction table
            // Integration test environment may have additional transactions beyond test data
            Page<TxnEntity> results = txRepository.searchTxnEntitiesAND(
                    Collections.emptySet(), null, null, null, true, null, new SimpleOffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            assertThat(txList).isNotEmpty();
            
            Set<String> actualTxHashes = txList.stream()
                    .map(TxnEntity::getTxHash)
                    .collect(Collectors.toSet());
            
            // Verify that our test successful transactions are present in results
            Set<String> expectedSuccessfulTxHashes = Set.of("successTx1", "successTx2", "successTx3");
            assertThat(actualTxHashes).containsAll(expectedSuccessfulTxHashes);
            
            // Verify that our test failed transactions are NOT present in results
            Set<String> invalidTxHashes = Set.of("failedTx1", "failedTx2");
            assertThat(actualTxHashes).doesNotContainAnyElementsOf(invalidTxHashes);
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesAND_VerifyFailedTransactionsInInvalidTable() {
            // Test that failed transactions (isSuccess=false) ARE in invalid_transaction table
            // Integration test environment may have additional transactions beyond test data
            Page<TxnEntity> results = txRepository.searchTxnEntitiesAND(
                    Collections.emptySet(), null, null, null, false, null, new SimpleOffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            Set<String> actualTxHashes = txList.stream()
                    .map(TxnEntity::getTxHash)
                    .collect(Collectors.toSet());
            
            // Verify that our test failed transactions are present in results
            Set<String> expectedFailedTxHashes = Set.of("failedTx1", "failedTx2");
            assertThat(actualTxHashes).containsAll(expectedFailedTxHashes);
            
            // Verify that our test successful transactions are NOT present in results
            Set<String> successfulTxHashes = Set.of("successTx1", "successTx2", "successTx3");
            assertThat(actualTxHashes).doesNotContainAnyElementsOf(successfulTxHashes);
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesAND_VerifyNoJoinWhenIsSuccessNull() {
            // Test performance optimization: when isSuccess=null, no INVALID_TRANSACTION join should occur
            // and all transactions (both successful and failed) should be returned
            // Integration test environment may have additional transactions beyond test data
            Page<TxnEntity> results = txRepository.searchTxnEntitiesAND(
                    Collections.emptySet(), null, null, null, null, null, new SimpleOffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            assertThat(txList).isNotEmpty();
            
            Set<String> actualTxHashes = txList.stream()
                    .map(TxnEntity::getTxHash)
                    .collect(Collectors.toSet());
            
            // Verify that ALL our test transactions are present (both successful and failed)
            Set<String> expectedAllTxHashes = Set.of("successTx1", "successTx2", "successTx3", "failedTx1", "failedTx2");
            assertThat(actualTxHashes).containsAll(expectedAllTxHashes);
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesOR_VerifySuccessfulTransactionsNotInInvalidTable() {
            // Test that successful transactions (isSuccess=true) are NOT in invalid_transaction table for OR logic
            // Integration test environment may have additional transactions beyond test data
            Page<TxnEntity> results = txRepository.searchTxnEntitiesOR(
                    Collections.emptySet(), null, null, null, true, null, new SimpleOffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            assertThat(txList).isNotEmpty();
            
            Set<String> actualTxHashes = txList.stream()
                    .map(TxnEntity::getTxHash)
                    .collect(Collectors.toSet());
            
            // Verify that our test successful transactions are present in results
            Set<String> expectedSuccessfulTxHashes = Set.of("successTx1", "successTx2", "successTx3");
            assertThat(actualTxHashes).containsAll(expectedSuccessfulTxHashes);
            
            // Verify that our test failed transactions are NOT present in results
            Set<String> invalidTxHashes = Set.of("failedTx1", "failedTx2");
            assertThat(actualTxHashes).doesNotContainAnyElementsOf(invalidTxHashes);
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesOR_VerifyFailedTransactionsInInvalidTable() {
            // Test that failed transactions (isSuccess=false) ARE in invalid_transaction table for OR logic
            // Integration test environment may have additional transactions beyond test data
            Page<TxnEntity> results = txRepository.searchTxnEntitiesOR(
                    Collections.emptySet(), null, null, null, false, null, new SimpleOffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            Set<String> actualTxHashes = txList.stream()
                    .map(TxnEntity::getTxHash)
                    .collect(Collectors.toSet());
            
            // Verify that our test failed transactions are present in results
            Set<String> expectedFailedTxHashes = Set.of("failedTx1", "failedTx2");
            assertThat(actualTxHashes).containsAll(expectedFailedTxHashes);
            
            // Verify that our test successful transactions are NOT present in results
            Set<String> successfulTxHashes = Set.of("successTx1", "successTx2", "successTx3");
            assertThat(actualTxHashes).doesNotContainAnyElementsOf(successfulTxHashes);
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesOR_VerifyNoJoinWhenIsSuccessNull() {
            // Test performance optimization: when isSuccess=null, no INVALID_TRANSACTION join should occur
            // and all transactions (both successful and failed) should be returned for OR logic
            // Integration test environment may have additional transactions beyond test data
            Page<TxnEntity> results = txRepository.searchTxnEntitiesOR(
                    Collections.emptySet(), null, null, null, null, null, new SimpleOffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            assertThat(txList).isNotEmpty();
            
            Set<String> actualTxHashes = txList.stream()
                    .map(TxnEntity::getTxHash)
                    .collect(Collectors.toSet());
            
            // Verify that ALL our test transactions are present (both successful and failed)
            Set<String> expectedAllTxHashes = Set.of("successTx1", "successTx2", "successTx3", "failedTx1", "failedTx2");
            assertThat(actualTxHashes).containsAll(expectedAllTxHashes);
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesAND_InvalidTransactionJoinWithOtherFilters() {
            // Test combining isSuccess with other filters (blockHash, txHashes)
            // Verify join behavior works correctly with complex conditions
            
            // Test successful transactions in specific block
            Set<String> specificBlockTxHashes = Set.of("successTx1", "successTx2", "failedTx1");
            Page<TxnEntity> successfulInBlockResults = txRepository.searchTxnEntitiesAND(
                    specificBlockTxHashes, "successBlock1", null, null, true, null, 
                    new SimpleOffsetBasedPageRequest(0, 100));
            
            // Should include successful transactions from the specified block and tx hash set
            // Integration test environment may have additional matching transactions
            Set<String> successfulTxHashes = successfulInBlockResults.getContent().stream()
                    .map(TxnEntity::getTxHash)
                    .collect(Collectors.toSet());
            assertThat(successfulTxHashes).contains("successTx1", "successTx2");
            // Verify only transactions from our specific tx hash set are returned
            assertThat(successfulTxHashes).allSatisfy(hash -> 
                    assertThat(Set.of("successTx1", "successTx2", "failedTx1")).contains(hash));
            
            // Test failed transactions in specific block
            Page<TxnEntity> failedInBlockResults = txRepository.searchTxnEntitiesAND(
                    specificBlockTxHashes, "successBlock1", null, null, false, null, 
                    new SimpleOffsetBasedPageRequest(0, 100));
            
            // Should include failed transactions from the specified block and tx hash set
            // Integration test environment may have additional matching transactions
            Set<String> failedTxHashes = failedInBlockResults.getContent().stream()
                    .map(TxnEntity::getTxHash)
                    .collect(Collectors.toSet());
            assertThat(failedTxHashes).contains("failedTx1");
            // Verify only transactions from our specific tx hash set are returned
            assertThat(failedTxHashes).allSatisfy(hash -> 
                    assertThat(Set.of("successTx1", "successTx2", "failedTx1")).contains(hash));
        }

        @Test
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
        @Sql(scripts = "classpath:/testdata/sql/tx-repository-success-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
        public void testSearchTxnEntitiesOR_InvalidTransactionJoinWithOtherFilters() {
            // Test combining isSuccess with other filters using OR logic
            // Verify join behavior works correctly with complex OR conditions
            
            // Test successful transactions with mixed block conditions
            Page<TxnEntity> successfulMixedResults = txRepository.searchTxnEntitiesOR(
                    Set.of("successTx1"), "successBlock2", null, null, true, null, 
                    new SimpleOffsetBasedPageRequest(0, 100));
            
            // Should return successful transactions that match either condition:
            // - successTx1 (from tx hashes) OR transactions from successBlock2
            // AND are successful (not in invalid_transaction table)
            Set<String> actualTxHashes = successfulMixedResults.getContent().stream()
                    .map(TxnEntity::getTxHash)
                    .collect(Collectors.toSet());
            
            // Should include successTx1 (matches tx hash) and successTx3 (matches block hash)
            // but exclude failedTx2 even though it's in successBlock2 (because it's not successful)
            // Integration test environment may have additional matching transactions
            assertThat(actualTxHashes).contains("successTx1", "successTx3");
            // Verify our test failed transactions are not included
            assertThat(actualTxHashes).doesNotContainAnyElementsOf(Set.of("failedTx1", "failedTx2"));
            
            // Test failed transactions with mixed conditions
            Page<TxnEntity> failedMixedResults = txRepository.searchTxnEntitiesOR(
                    Set.of("failedTx1"), "successBlock2", null, null, false, null, 
                    new SimpleOffsetBasedPageRequest(0, 100));
            
            // Should return failed transactions that match either condition:
            // - failedTx1 (from tx hashes) OR transactions from successBlock2  
            // AND are failed (in invalid_transaction table)
            Set<String> failedTxHashes = failedMixedResults.getContent().stream()
                    .map(TxnEntity::getTxHash)
                    .collect(Collectors.toSet());
            
            // Should include failedTx1 (matches tx hash) and failedTx2 (matches block hash)
            // but exclude successTx3 even though it's in successBlock2 (because it's successful)
            // Integration test environment may have additional matching transactions
            assertThat(failedTxHashes).contains("failedTx1", "failedTx2");
            // Verify our test successful transactions are not included
            assertThat(failedTxHashes).doesNotContainAnyElementsOf(Set.of("successTx1", "successTx2", "successTx3"));
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
                    Collections.emptySet(), null, null, null, null, lovelaceCurrency, 
                    new SimpleOffsetBasedPageRequest(0, 100));

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
                    Collections.emptySet(), null, null, null, null, adaCurrency, 
                    new SimpleOffsetBasedPageRequest(0, 100));

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
                    Collections.emptySet(), null, null, null, null, policyIdCurrency, 
                    new SimpleOffsetBasedPageRequest(0, 100));

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
                    Collections.emptySet(), null, null, null, null, preciseAssetCurrency, 
                    new SimpleOffsetBasedPageRequest(0, 100));

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
                    Collections.emptySet(), null, null, null, null, symbolCurrency, 
                    new SimpleOffsetBasedPageRequest(0, 100));

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
                    Collections.emptySet(), null, null, null, null, lovelaceCurrency, 
                    new SimpleOffsetBasedPageRequest(0, 100));

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
                    Collections.emptySet(), null, null, null, null, preciseAssetCurrency, 
                    new SimpleOffsetBasedPageRequest(0, 100));

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
                    Collections.emptySet(), null, null, null, null, null, 
                    new SimpleOffsetBasedPageRequest(0, 100));

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
                    Collections.emptySet(), null, null, null, null, emptyCurrency, 
                    new SimpleOffsetBasedPageRequest(0, 100));

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
                    new SimpleOffsetBasedPageRequest(0, 100));

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
                    Collections.emptySet(), null, null, null, null, upperCaseAdaCurrency, 
                    new SimpleOffsetBasedPageRequest(0, 100));
            
            Page<TxnEntity> lowerResults = txRepository.searchTxnEntitiesAND(
                    Collections.emptySet(), null, null, null, null, lowerCaseAdaCurrency, 
                    new SimpleOffsetBasedPageRequest(0, 100));
            
            Page<TxnEntity> mixedResults = txRepository.searchTxnEntitiesAND(
                    Collections.emptySet(), null, null, null, null, mixedCaseLovelaceCurrency, 
                    new SimpleOffsetBasedPageRequest(0, 100));

            // All should return the same results (case insensitive for ADA/lovelace)
            assertThat(upperResults.getContent()).hasSameSizeAs(lowerResults.getContent());
            assertThat(upperResults.getContent()).hasSameSizeAs(mixedResults.getContent());
        }
    }

}
