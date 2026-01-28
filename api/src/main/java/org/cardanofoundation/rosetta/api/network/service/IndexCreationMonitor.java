package org.cardanofoundation.rosetta.api.network.service;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Interface for monitoring database index readiness status.
 * Implementations check if required indexes exist and are valid and ready,
 * which affects sync status reporting.
 */
public interface IndexCreationMonitor {

    /**
     * Checks if required indexes are missing, not valid, or not ready in the database.
     * Returns true if any required index is missing or not fully ready (indisvalid=false or indisready=false).
     *
     * @return true if indexes are not ready (missing, not valid, or not ready), false if all are ready
     */
    boolean isCreatingIndexes();

    /**
     * Retrieves detailed information about required indexes and their status.
     * Returns an empty list if database doesn't support index status monitoring.
     * For PostgreSQL, returns status information from pg_index system catalog.
     *
     * @return list of index status information
     */
    List<IndexCreationProgress> getIndexCreationProgress();

    /**
     * Represents the status of an index.
     * The phase field contains a description of the index status.
     * Numeric fields (blocks*, tuples*) are optional and may be null depending on the implementation.
     */
    record IndexCreationProgress(
        @Nullable String phase,
        @Nullable Long blocksTotal,
        @Nullable Long blocksDone,
        @Nullable Long tuplesTotal,
        @Nullable Long tuplesDone
    ) {
        /**
         * Calculates the completion percentage based on tuples processed.
         *
         * @return percentage complete (0-100), or null if cannot be calculated
         */
        @Nullable
        public Double getCompletionPercentage() {
            if (tuplesTotal == null || tuplesDone == null || tuplesTotal == 0) {
                return null;
            }
            return (tuplesDone.doubleValue() / tuplesTotal.doubleValue()) * 100.0;
        }
    }
}
