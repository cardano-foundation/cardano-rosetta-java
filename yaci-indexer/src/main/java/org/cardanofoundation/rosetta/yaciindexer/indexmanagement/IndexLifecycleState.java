package org.cardanofoundation.rosetta.yaciindexer.indexmanagement;

public enum IndexLifecycleState {
    PENDING,    // Sync not yet complete; index work not started
    APPLYING,   // Sync reached tip; index creation in progress
    READY,      // All required indexes valid, ready, query-safe
    FAILED      // Persistent non-recoverable error; operator action required
}
