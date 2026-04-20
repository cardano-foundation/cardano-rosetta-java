package org.cardanofoundation.rosetta.yaciindexer.indexmanagement;

public enum IndexItemState {
    MISSING,      // Not found in pg_index
    INVALID,      // Found in pg_index but indisvalid=false (was dropped and not yet rebuilt)
    BUILDING,     // CREATE INDEX is running (indisready=false, indisvalid=false)
    READY,        // indisvalid=true and indisready=true
    FAILED        // Creation attempt threw a non-recoverable exception
}
