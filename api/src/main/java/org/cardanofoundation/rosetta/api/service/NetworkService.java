package org.cardanofoundation.rosetta.api.service;

import java.io.IOException;
import org.cardanofoundation.rosetta.api.exception.ServerException;
import org.cardanofoundation.rosetta.api.model.Network;
import org.cardanofoundation.rosetta.api.model.rest.MetadataRequest;
import org.cardanofoundation.rosetta.api.model.rest.NetworkListResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkOptionsResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkRequest;
import org.cardanofoundation.rosetta.api.model.rest.NetworkStatusResponse;

public interface NetworkService {
    NetworkListResponse getNetworkList(final MetadataRequest metadataRequest);

    NetworkOptionsResponse getNetworkOptions(final NetworkRequest networkRequest)
        throws IOException, InterruptedException;

    NetworkStatusResponse getNetworkStatus(final NetworkRequest networkRequest)
        throws IOException, ServerException;

    Network getSupportedNetwork();
}
