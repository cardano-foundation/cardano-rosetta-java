package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.jdbc.Sql;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

public class TxRepositoryTest extends IntegrationTest {

    @Autowired
    private TxRepository txRepository;

    @Test
    @Sql(scripts = "classpath:/testData//sql/tx-repository-test-init.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:/testData//sql/tx-repository-test-cleanup.sql", executionPhase = AFTER_TEST_METHOD)
    public void testSearchTxnEntitiesOR_NoDuplicates() {
        Slice<TxnEntity> results = txRepository.searchTxnEntitiesOR(
                null, null, null, null, Pageable.unpaged());

        assertThat(hasDuplicateTxHashes(results.getContent())).isFalse();
    }

    public static boolean hasDuplicateTxHashes(List<TxnEntity> results) {
        Set<String> seen = new HashSet<>();

        return results.stream()
                .map(TxnEntity::getTxHash)
                .anyMatch(txHash -> !seen.add(txHash));
    }

}
