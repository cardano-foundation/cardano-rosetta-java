package org.cardanofoundation.rosetta.api.service;

import lombok.Builder;
import org.cardanofoundation.rosetta.api.model.rest.BlockIdentifier;

@Builder
public class PostgresLedgerDataProviderClient {
    private final String networkId;

    public BlockIdentifier getTip() {
        return null;
    }
}
