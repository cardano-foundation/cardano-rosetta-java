package org.cardanofoundation.rosetta.api.network.service;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Interface for monitoring database index creation progress.
 * Implementations can check if any indexes are currently being created,
 * which affects sync status reporting.
 */
public interface IndexCreationMonitor {

    /**
     * Checks if any indexes are currently being created in the database.
     *
     * @return true if indexes are being created, false otherwise
     */
    boolean isCreatingIndexes();

    /**
     * Retrieves detailed information about indexes currently being created.
     * Returns an empty list if no indexes are being created or if the database
     * doesn't support detailed index creation monitoring.
     *
     * @return list of index creation progress information
     */
    List<IndexCreationProgress> getIndexCreationProgress();

    /**
     * Represents the progress of an index being created.
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
