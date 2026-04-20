package org.cardanofoundation.rosetta.yaciindexer.indexmanagement;

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
class RosettaIndexesEndpointTest {

    @Mock
    private RosettaIndexLifecycleService lifecycleService;

    @InjectMocks
    private RosettaIndexesEndpoint endpoint;

    @Nested
    class GetRosettaIndexProgress {

        @Test
        void shouldReturnCorrectProgressSnapshot() {
            // Arrange
            Instant lastProgress = Instant.now();
            when(lifecycleService.getState()).thenReturn(IndexLifecycleState.APPLYING);
            when(lifecycleService.getLastProgressAt()).thenReturn(lastProgress);

            List<IndexItemStatus> statuses = List.of(
                    new IndexItemStatus("idx_1", IndexItemState.READY, null),
                    new IndexItemStatus("idx_2", IndexItemState.MISSING, null),
                    new IndexItemStatus("idx_3", IndexItemState.FAILED, "Error"),
                    new IndexItemStatus("idx_4", IndexItemState.BUILDING, null),
                    new IndexItemStatus("idx_5", IndexItemState.INVALID, null)
            );
            when(lifecycleService.getIndexStatus()).thenReturn(statuses);

            // Act
            RosettaIndexProgressSnapshot snapshot = endpoint.getRosettaIndexProgress();

            // Assert
            assertEquals(IndexLifecycleState.APPLYING, snapshot.overallState());
            assertEquals(statuses, snapshot.indexes());
            assertEquals(lastProgress, snapshot.lastProgressAt());
            assertEquals(5, snapshot.totalRequired());
            assertEquals(1, snapshot.totalReady());
            assertEquals(1, snapshot.totalMissing());
            assertEquals(1, snapshot.totalFailed());
        }
    }
}
