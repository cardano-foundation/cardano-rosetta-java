package org.cardanofoundation.rosetta.api.network.service;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.SyncStatus;

import org.cardanofoundation.rosetta.api.block.model.domain.BlockIdentifierExtended;
import org.cardanofoundation.rosetta.api.block.service.LedgerBlockService;
import org.cardanofoundation.rosetta.common.exception.ApiException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetworkServiceSyncStatusTest {

    @Mock
    private LedgerBlockService ledgerBlockService;

    @Mock
    private SyncStatusService syncStatusService;

    @InjectMocks
    private NetworkServiceImpl networkService;

    private BlockIdentifierExtended latestBlock;

    @BeforeEach
    void setUp() {
        latestBlock = BlockIdentifierExtended.builder()
                .hash("hash")
                .number(100L)
                .slot(2000L)
                .build();
    }

    @Test
    void verifySyncStatus_Live_DoesNotThrow() {
        SyncStatus syncStatus = SyncStatus.builder()
                .stage("LIVE")
                .synced(true)
                .build();
        when(syncStatusService.getSyncStatus()).thenReturn(Optional.of(syncStatus));

        assertDoesNotThrow(() -> networkService.verifySyncStatus());
    }

    @Test
    void verifySyncStatus_Syncing_ThrowsIndexerNotReady() {
        SyncStatus syncStatus = SyncStatus.builder()
                .stage("SYNCING")
                .synced(false)
                .build();
        when(syncStatusService.getSyncStatus()).thenReturn(Optional.of(syncStatus));

        assertThrows(ApiException.class, () -> networkService.verifySyncStatus());
    }

    @Test
    void verifySyncStatus_ApplyingIndexes_ThrowsIndexerNotReady() {
        SyncStatus syncStatus = SyncStatus.builder()
                .stage("APPLYING_INDEXES")
                .synced(false)
                .build();
        when(syncStatusService.getSyncStatus()).thenReturn(Optional.of(syncStatus));

        assertThrows(ApiException.class, () -> networkService.verifySyncStatus());
    }

    @Test
    void verifySyncStatus_EmptySyncStatus_ThrowsIndexerNotReady() {
        when(syncStatusService.getSyncStatus()).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> networkService.verifySyncStatus());
    }
}
