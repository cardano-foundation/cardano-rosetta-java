package org.cardanofoundation.rosetta.api.network.service.postgresql;

import org.cardanofoundation.rosetta.api.network.service.IndexCreationMonitor.IndexCreationProgress;
import org.cardanofoundation.rosetta.api.network.service.RosettaIndexConfig;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostgreSQLIndexCreationMonitorTest {

    @Mock
    private DSLContext dslContext;

    @Mock
    private RosettaIndexConfig rosettaIndexConfig;

    private PostgreSQLIndexCreationMonitor monitor;

    private static final List<String> REQUIRED_INDEXES = Arrays.asList(
        "idx_test_1",
        "idx_test_2",
        "idx_test_3"
    );

    @BeforeEach
    void setUp() {
        monitor = new PostgreSQLIndexCreationMonitor(dslContext, rosettaIndexConfig);
    }

    @Nested
    @DisplayName("isCreatingIndexes - null/empty configuration tests")
    class NullAndEmptyConfigTests {

        @Test
        @DisplayName("Should return false when no indexes are configured")
        void shouldReturnFalseWhenNoIndexesConfigured() {
            // Given
            when(rosettaIndexConfig.getDbIndexes()).thenReturn(Collections.emptyList());

            // When
            boolean creating = monitor.isCreatingIndexes();

            // Then
            assertThat(creating).isFalse();
            verify(dslContext, never()).select(any(), any(), any());
        }

        @Test
        @DisplayName("Should return false when configuration is null")
        void shouldReturnFalseWhenConfigurationIsNull() {
            // Given
            when(rosettaIndexConfig.getDbIndexes()).thenReturn(null);

            // When
            boolean creating = monitor.isCreatingIndexes();

            // Then
            assertThat(creating).isFalse();
            verify(dslContext, never()).select(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("isCreatingIndexes - error handling tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should return true on database query error to stay in APPLYING_INDEXES state")
        void shouldReturnTrueOnDatabaseError() {
            // Given
            when(rosettaIndexConfig.getDbIndexes()).thenReturn(REQUIRED_INDEXES);
            when(dslContext.select(any(), any(), any())).thenThrow(new RuntimeException("Database error"));

            // When
            boolean creating = monitor.isCreatingIndexes();

            // Then - safer to assume indexes not ready on error
            assertThat(creating).isTrue();
        }
    }

    @Nested
    @DisplayName("getIndexCreationProgress - configuration tests")
    class GetIndexCreationProgressConfigTests {

        @Test
        @DisplayName("Should return empty list when no indexes configured")
        void shouldReturnEmptyListWhenNoIndexesConfigured() {
            // Given
            when(rosettaIndexConfig.getDbIndexes()).thenReturn(Collections.emptyList());

            // When
            List<IndexCreationProgress> progress = monitor.getIndexCreationProgress();

            // Then
            assertThat(progress).isEmpty();
            verify(dslContext, never()).select(any(), any(), any());
        }

        @Test
        @DisplayName("Should return empty list when configuration is null")
        void shouldReturnEmptyListWhenConfigurationIsNull() {
            // Given
            when(rosettaIndexConfig.getDbIndexes()).thenReturn(null);

            // When
            List<IndexCreationProgress> progress = monitor.getIndexCreationProgress();

            // Then
            assertThat(progress).isEmpty();
            verify(dslContext, never()).select(any(), any(), any());
        }

        @Test
        @DisplayName("Should return empty list on database error")
        void shouldReturnEmptyListOnDatabaseError() {
            // Given
            when(rosettaIndexConfig.getDbIndexes()).thenReturn(REQUIRED_INDEXES);
            when(dslContext.select(any(), any(), any())).thenThrow(new RuntimeException("Database error"));

            // When
            List<IndexCreationProgress> progress = monitor.getIndexCreationProgress();

            // Then
            assertThat(progress).isEmpty();
        }
    }

    @Nested
    @DisplayName("Invariant tests")
    class InvariantTests {

        @Test
        @DisplayName("Invariant: When config is null or empty, no database queries should be made")
        void invariantNoQueriesWhenNoConfig() {
            // Test with null
            when(rosettaIndexConfig.getDbIndexes()).thenReturn(null);
            monitor.isCreatingIndexes();
            monitor.getIndexCreationProgress();

            // Test with empty list
            when(rosettaIndexConfig.getDbIndexes()).thenReturn(Collections.emptyList());
            monitor.isCreatingIndexes();
            monitor.getIndexCreationProgress();

            // Verify no database interactions
            verifyNoInteractions(dslContext);
        }

        @Test
        @DisplayName("Invariant: getDbIndexes() should be called for every operation")
        void invariantConfigAccessedEveryTime() {
            // Given
            when(rosettaIndexConfig.getDbIndexes()).thenReturn(null);

            // When
            monitor.isCreatingIndexes();
            monitor.getIndexCreationProgress();

            // Then - should be called twice (once per method call)
            verify(rosettaIndexConfig, times(2)).getDbIndexes();
        }

        @Test
        @DisplayName("Invariant: On any exception, isCreatingIndexes returns true (safe default)")
        void invariantErrorStateReturnsTrueForIsCreating() {
            // Given
            when(rosettaIndexConfig.getDbIndexes()).thenReturn(REQUIRED_INDEXES);
            when(dslContext.select(any(), any(), any()))
                .thenThrow(new RuntimeException("DB error"))
                .thenThrow(new NullPointerException("NPE"))
                .thenThrow(new IllegalStateException("Illegal state"));

            // When/Then - all errors should return true (stay in APPLYING_INDEXES)
            assertThat(monitor.isCreatingIndexes()).isTrue();
            assertThat(monitor.isCreatingIndexes()).isTrue();
            assertThat(monitor.isCreatingIndexes()).isTrue();
        }

        @Test
        @DisplayName("Invariant: On any exception, getIndexCreationProgress returns empty list")
        void invariantErrorStateReturnsEmptyListForProgress() {
            // Given
            when(rosettaIndexConfig.getDbIndexes()).thenReturn(REQUIRED_INDEXES);
            when(dslContext.select(any(), any(), any()))
                .thenThrow(new RuntimeException("DB error"))
                .thenThrow(new NullPointerException("NPE"));

            // When/Then - all errors should return empty list
            assertThat(monitor.getIndexCreationProgress()).isEmpty();
        }

        @Test
        @DisplayName("Invariant: Required indexes list should not be modified")
        void invariantRequiredIndexesNotModified() {
            // Given
            List<String> originalList = Arrays.asList("idx_1", "idx_2");
            when(rosettaIndexConfig.getDbIndexes()).thenReturn(originalList);

            // When
            monitor.isCreatingIndexes();
            monitor.getIndexCreationProgress();

            // Then - verify the list wasn't modified
            assertThat(originalList).hasSize(2).containsExactly("idx_1", "idx_2");
        }
    }

    @Nested
    @DisplayName("Integration contract tests")
    class IntegrationContractTests {

        @Test
        @DisplayName("Contract: When called with valid config, should query pg_index and pg_class")
        void contractShouldQueryCorrectTables() {
            // Given
            when(rosettaIndexConfig.getDbIndexes()).thenReturn(REQUIRED_INDEXES);
            when(dslContext.select(any(), any(), any())).thenThrow(new RuntimeException("Expected query attempt"));

            // When
            try {
                monitor.isCreatingIndexes();
            } catch (Exception ignored) {
                // Expected
            }

            // Then - verify that a SELECT query was attempted
            verify(dslContext).select(any(), any(), any());
        }

        @Test
        @DisplayName("Contract: Both methods should use same configuration source")
        void contractShouldUseSameConfigSource() {
            // Given
            when(rosettaIndexConfig.getDbIndexes())
                .thenReturn(REQUIRED_INDEXES)
                .thenReturn(Collections.emptyList());

            // When
            boolean first = monitor.isCreatingIndexes();
            List<IndexCreationProgress> second = monitor.getIndexCreationProgress();

            // Then - both should respect their respective config values
            // First call had indexes, so it tried to query (would fail but that's OK for this test)
            // Second call had no indexes, so should return empty without query
            assertThat(second).isEmpty();
        }
    }
}
