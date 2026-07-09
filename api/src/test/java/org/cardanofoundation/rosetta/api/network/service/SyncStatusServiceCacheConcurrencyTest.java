package org.cardanofoundation.rosetta.api.network.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.block.model.domain.BlockIdentifierExtended;
import org.cardanofoundation.rosetta.api.block.service.LedgerBlockService;
import org.cardanofoundation.rosetta.common.time.OfflineSlotService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Regression test for a cache-stampede bug: under concurrent requests hitting
 * {@link SyncStatusService#getSyncStatus()} at the same time (e.g. right after the 5s cache
 * eviction, or under load-test level concurrency), every thread independently missed the cache
 * and queried the DB, instead of exactly one thread computing the value while the rest waited
 * for and reused the cached result.
 * <p>
 * Fixed by adding {@code sync = true} to the {@code @Cacheable} annotation.
 */
@SpringBootTest
@EnableCaching
class SyncStatusServiceCacheConcurrencyTest {

    private static final String SYNC_STATUS_CACHE = "syncStatusCache";
    private static final int CONCURRENT_REQUESTS = 50;

    @MockitoBean
    private LedgerBlockService ledgerBlockService;

    @MockitoBean
    private OfflineSlotService offlineSlotService;

    @Autowired
    private SyncStatusService syncStatusService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        Cache cache = cacheManager.getCache(SYNC_STATUS_CACHE);
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    void concurrentCacheMissesShouldOnlyQueryDbOnce() throws Exception {
        long currentSlot = 1000L;
        long latestBlockSlot = 990L;
        BlockIdentifierExtended latestBlock = BlockIdentifierExtended.builder()
            .slot(latestBlockSlot)
            .build();

        when(ledgerBlockService.findLatestBlockIdentifier()).thenReturn(latestBlock);
        when(offlineSlotService.getCurrentSlotBasedOnTime()).thenReturn(Optional.of(currentSlot));

        // A latch ensures all threads are alive and waiting before any of them calls
        // getSyncStatus(), maximising the probability of a simultaneous cache miss.
        CountDownLatch startGate = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);
        try {
            List<Future<Optional<org.openapitools.client.model.SyncStatus>>> futures =
                IntStream.range(0, CONCURRENT_REQUESTS)
                    .mapToObj(i -> executor.submit(() -> {
                        startGate.await();
                        return syncStatusService.getSyncStatus();
                    }))
                    .toList();

            startGate.countDown(); // release all threads simultaneously

            for (Future<Optional<org.openapitools.client.model.SyncStatus>> future : futures) {
                // Timeout guards against a deadlock hanging the CI pipeline indefinitely.
                assertThat(future.get(5, TimeUnit.SECONDS))
                    .isPresent()
                    .hasValueSatisfying(status -> {
                        // Assert content consistency: every thread must see the same
                        // correct result, not a stale or partially-computed value.
                        assertThat(status.getSynced()).isTrue();
                        assertThat(status.getCurrentIndex()).isEqualTo(latestBlockSlot);
                        assertThat(status.getTargetIndex()).isEqualTo(currentSlot);
                    });
            }
        } finally {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }

        // With sync = true, only one of the CONCURRENT_REQUESTS threads should have missed
        // the cache and queried the DB; the rest must have waited and reused that result.
        verify(ledgerBlockService, times(1)).findLatestBlockIdentifier();
    }
}
