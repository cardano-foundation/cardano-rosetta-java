package org.cardanofoundation.rosetta.yaciindexer.indexes;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexesEndpointTest {

    @Mock
    private IndexService indexService;

    @InjectMocks
    private IndexesEndpoint endpoint;

    @Nested
    class GetRosettaIndexProgress {

        @Test
        void shouldReturnCorrectProgressSnapshot() {
            // Arrange
            Instant lastProgress = Instant.now();
            when(indexService.getState()).thenReturn(IndexLifecycleState.APPLYING);
            when(indexService.getLastProgressAt()).thenReturn(lastProgress);

            List<IndexItemStatus> statuses = List.of(
                    new IndexItemStatus("idx_1", IndexItemState.READY, null),
                    new IndexItemStatus("idx_2", IndexItemState.MISSING, null),
                    new IndexItemStatus("idx_3", IndexItemState.FAILED, "Error"),
                    new IndexItemStatus("idx_4", IndexItemState.BUILDING, null),
                    new IndexItemStatus("idx_5", IndexItemState.INVALID, null)
            );
            when(indexService.getIndexStatus()).thenReturn(statuses);

            // Act
            IndexProgress snapshot = endpoint.getIndexProgress();

            // Assert
            assertEquals(IndexLifecycleState.APPLYING, snapshot.overallState());
            assertEquals(statuses, snapshot.indexes());
            assertEquals(lastProgress, snapshot.lastProgressAt());
            assertEquals(5, snapshot.totalRequired());
            assertEquals(1, snapshot.totalReady());
            assertEquals(1, snapshot.totalMissing());
            assertEquals(1, snapshot.totalFailed());
        }

        @Test
        void shouldReturnCorrectProgressSnapshotWhenAllReady() {
            // Arrange — SC-004: all indexes pre-exist
            Instant lastProgress = Instant.now();
            when(indexService.getState()).thenReturn(IndexLifecycleState.READY);
            when(indexService.getLastProgressAt()).thenReturn(lastProgress);

            List<IndexItemStatus> statuses = List.of(
                    new IndexItemStatus("idx_1", IndexItemState.READY, null),
                    new IndexItemStatus("idx_2", IndexItemState.READY, null),
                    new IndexItemStatus("idx_3", IndexItemState.READY, null)
            );
            when(indexService.getIndexStatus()).thenReturn(statuses);

            // Act
            IndexProgress snapshot = endpoint.getIndexProgress();

            // Assert
            assertEquals(IndexLifecycleState.READY, snapshot.overallState());
            assertEquals(statuses, snapshot.indexes());
            assertEquals(lastProgress, snapshot.lastProgressAt());
            assertEquals(3, snapshot.totalRequired());
            assertEquals(3, snapshot.totalReady());
            assertEquals(0, snapshot.totalMissing());
            assertEquals(0, snapshot.totalFailed());
        }
    }
}
