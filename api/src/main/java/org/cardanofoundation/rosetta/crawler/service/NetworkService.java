package org.cardanofoundation.rosetta.crawler.service;

import org.cardanofoundation.rosetta.crawler.model.rest.*;

public interface NetworkService {
    NetworkListResponse getNetworkList(final MetadataRequest metadataRequest);

    NetworkOptionsResponse getNetworkOptions(final NetworkRequest networkRequest);

    NetworkStatusResponse getNetworkStatus(final NetworkRequest networkRequest);
}
