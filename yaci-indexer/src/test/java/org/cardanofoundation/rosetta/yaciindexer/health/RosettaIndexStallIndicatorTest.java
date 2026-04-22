package org.cardanofoundation.rosetta.yaciindexer.health;

import org.cardanofoundation.rosetta.yaciindexer.indexmanagement.IndexLifecycleState;
import org.cardanofoundation.rosetta.yaciindexer.indexmanagement.RosettaIndexLifecycleService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RosettaIndexStallIndicatorTest {

    private final RosettaIndexLifecycleService lifecycleService = mock(RosettaIndexLifecycleService.class);

    @Nested
    class WhenStateIsNotApplying {

        @Test
        void shouldReturnUpWhenPending() {
            when(lifecycleService.getState()).thenReturn(IndexLifecycleState.PENDING);
            RosettaIndexStallIndicator indicator = new RosettaIndexStallIndicator(lifecycleService, 15);

            Health health = indicator.health();

            assertEquals(Status.UP, health.getStatus());
            assertEquals("PENDING", health.getDetails().get("indexLifecycleState"));
        }

        @Test
        void shouldReturnUpWhenReady() {
            when(lifecycleService.getState()).thenReturn(IndexLifecycleState.READY);
            RosettaIndexStallIndicator indicator = new RosettaIndexStallIndicator(lifecycleService, 15);

            Health health = indicator.health();

            assertEquals(Status.UP, health.getStatus());
        }

        @Test
        void shouldReturnUpWhenFailed() {
            when(lifecycleService.getState()).thenReturn(IndexLifecycleState.FAILED);
            RosettaIndexStallIndicator indicator = new RosettaIndexStallIndicator(lifecycleService, 15);

            Health health = indicator.health();

            assertEquals(Status.UP, health.getStatus());
        }
    }

    @Nested
    class WhenStateIsApplying {

        @Test
        void shouldReturnUpWhenLastProgressIsNull() {
            when(lifecycleService.getState()).thenReturn(IndexLifecycleState.APPLYING);
            when(lifecycleService.getLastProgressAt()).thenReturn(null);
            RosettaIndexStallIndicator indicator = new RosettaIndexStallIndicator(lifecycleService, 15);

            Health health = indicator.health();

            assertEquals(Status.UP, health.getStatus());
            assertEquals("Starting", health.getDetails().get("status"));
        }

        @Test
        void shouldReturnUpWhenProgressIsRecent() {
            when(lifecycleService.getState()).thenReturn(IndexLifecycleState.APPLYING);
            when(lifecycleService.getLastProgressAt()).thenReturn(Instant.now().minus(5, ChronoUnit.MINUTES));
            RosettaIndexStallIndicator indicator = new RosettaIndexStallIndicator(lifecycleService, 15);

            Health health = indicator.health();

            assertEquals(Status.UP, health.getStatus());
        }

        @Test
        void shouldReturnDownWhenStalled() {
            when(lifecycleService.getState()).thenReturn(IndexLifecycleState.APPLYING);
            when(lifecycleService.getLastProgressAt()).thenReturn(Instant.now().minus(20, ChronoUnit.MINUTES));
            RosettaIndexStallIndicator indicator = new RosettaIndexStallIndicator(lifecycleService, 15);

            Health health = indicator.health();

            assertEquals(Status.DOWN, health.getStatus());
            assertEquals("Index creation stalled", health.getDetails().get("error"));
            assertEquals(15, health.getDetails().get("stallTimeoutMinutes"));
        }

        @Test
        void shouldReturnDownWhenExactlyAtTimeout() {
            when(lifecycleService.getState()).thenReturn(IndexLifecycleState.APPLYING);
            when(lifecycleService.getLastProgressAt()).thenReturn(Instant.now().minus(15, ChronoUnit.MINUTES));
            RosettaIndexStallIndicator indicator = new RosettaIndexStallIndicator(lifecycleService, 15);

            Health health = indicator.health();

            assertEquals(Status.DOWN, health.getStatus());
        }
    }
}
