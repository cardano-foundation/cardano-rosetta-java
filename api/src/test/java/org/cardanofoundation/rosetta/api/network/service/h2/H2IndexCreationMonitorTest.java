package org.cardanofoundation.rosetta.api.network.service.h2;

import org.cardanofoundation.rosetta.api.network.service.IndexCreationMonitor.IndexCreationProgress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class H2IndexCreationMonitorTest {

    private H2IndexCreationMonitor monitor;

    @BeforeEach
    void setUp() {
        monitor = new H2IndexCreationMonitor();
    }

    @Test
    @DisplayName("Should always return false for isCreatingIndexes")
    void shouldAlwaysReturnFalse() {
        // When
        boolean creating = monitor.isCreatingIndexes();

        // Then
        assertThat(creating).isFalse();
    }

    @Test
    @DisplayName("Should always return empty list for getIndexCreationProgress")
    void shouldAlwaysReturnEmptyList() {
        // When
        List<IndexCreationProgress> progress = monitor.getIndexCreationProgress();

        // Then
        assertThat(progress).isEmpty();
    }

    @Test
    @DisplayName("Should be consistent across multiple calls")
    void shouldBeConsistentAcrossMultipleCalls() {
        // When/Then - multiple calls should all return the same values
        assertThat(monitor.isCreatingIndexes()).isFalse();
        assertThat(monitor.isCreatingIndexes()).isFalse();
        assertThat(monitor.isCreatingIndexes()).isFalse();

        assertThat(monitor.getIndexCreationProgress()).isEmpty();
        assertThat(monitor.getIndexCreationProgress()).isEmpty();
        assertThat(monitor.getIndexCreationProgress()).isEmpty();
    }
}
