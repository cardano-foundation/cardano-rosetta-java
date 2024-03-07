package org.cardanofoundation.rosetta.api.service;

import lombok.Builder;
import org.cardanofoundation.rosetta.api.model.rest.BlockIdentifier;

@Builder
public class PostgresLedgerDataProviderClient { //TODO the naming should not include specific db name. What if we decide switch to mysql..
    private final String networkId;

    //TODO EPAM why it ret null ??
    public BlockIdentifier getTip() {
        return null;
    }
}
