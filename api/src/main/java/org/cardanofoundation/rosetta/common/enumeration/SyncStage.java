package org.cardanofoundation.rosetta.common.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents the different stages of blockchain synchronization and readiness.
 */
@Getter
@RequiredArgsConstructor
public enum SyncStage {
    /**
     * Node is syncing to the blockchain tip but has not reached it yet.
     */
    SYNCING("SYNCING"),

    /**
     * Node has reached the blockchain tip and is now applying database indexes.
     * The service is not yet ready for production traffic.
     */
    APPLYING_INDEXES("APPLYING_INDEXES"),

    /**
     * Node is fully synced, all indexes are applied, and the service is ready
     * for production traffic.
     */
    LIVE("LIVE");

    private final String value;

    @Override
    public String toString() {
        return value;
    }
}
