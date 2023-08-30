package org.cardanofoundation.rosetta.api.service;

import org.cardanofoundation.rosetta.api.model.Network;
import org.cardanofoundation.rosetta.api.model.rest.*;

import java.io.IOException;

public interface NetworkService {
    NetworkListResponse getNetworkList(final MetadataRequest metadataRequest)
        throws IOException;

    NetworkOptionsResponse getNetworkOptions(final NetworkRequest networkRequest)
        throws IOException, InterruptedException;

    NetworkStatusResponse getNetworkStatus(final NetworkRequest networkRequest)
        throws IOException;

    Network getSupportedNetwork() throws IOException;
}
