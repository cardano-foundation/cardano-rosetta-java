package org.cardanofoundation.rosetta.api.service;

import org.cardanofoundation.rosetta.api.model.rest.*;

public interface NetworkService {
    NetworkListResponse getNetworkList(final MetadataRequest metadataRequest);

    NetworkOptionsResponse getNetworkOptions(final NetworkRequest networkRequest);

    NetworkStatusResponse getNetworkStatus(final NetworkRequest networkRequest);
}
