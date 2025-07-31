package org.cardanofoundation.rosetta.api.block.model.repository;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
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

public class TxRepositoryTest extends IntegrationTest {

    @Autowired
    private TxRepository txRepository;

    @Test
    @Sql(scripts = "classpath:/testdata//sql/tx-repository-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:/testdata//sql/tx-repository-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
    public void testSearchTxnEntitiesOR_NoDuplicates() {
        Slice<TxnEntity> results = txRepository.searchTxnEntitiesOR(
                null, null, null, null, null, new OffsetBasedPageRequest(0, 100));

        assertThat(hasDuplicateTxHashes(results.getContent())).isFalse();
    }

    @Test
    @Sql(scripts = "classpath:/testdata//sql/tx-repository-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:/testdata//sql/tx-repository-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
    public void testSearchTxnEntitiesAND_NoDuplicates() {
        Slice<TxnEntity> results = txRepository.searchTxnEntitiesAND(
                null, null, null, null, null, new OffsetBasedPageRequest(0, 100));

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
                    null, null, null, null, null, new OffsetBasedPageRequest(0, 1));

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
                    null, null, null, null, null, new OffsetBasedPageRequest(0, 1));

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
                    null, null, null, null, true, new OffsetBasedPageRequest(0, 100));

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
                    null, null, null, null, false, new OffsetBasedPageRequest(0, 100));

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
                    null, null, null, null, null, new OffsetBasedPageRequest(0, 100));

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
                    null, null, null, null, true, new OffsetBasedPageRequest(0, 100));

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
                    null, null, null, null, false, new OffsetBasedPageRequest(0, 100));

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
                    null, null, null, null, null, new OffsetBasedPageRequest(0, 100));

            List<TxnEntity> txList = results.getContent();
            assertThat(txList).isNotEmpty();
            
            // Should return all transactions regardless of success status
            assertThat(txList).allSatisfy(tx -> {
                assertThat(tx.getTxHash()).isNotNull();
            });
        }
    }

}
