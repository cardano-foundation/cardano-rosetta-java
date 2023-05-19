package org.cardanofoundation.rosetta.crawler.service;

import org.cardanofoundation.rosetta.crawler.model.rest.MetadataRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkListResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkOptionsResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkStatusResponse;

public interface NetworkService {
    NetworkListResponse getNetworkList(final MetadataRequest metadataRequest);

    NetworkOptionsResponse getNetworkOptions(final NetworkRequest networkRequest);

    NetworkStatusResponse getNetworkStatus(final NetworkRequest networkRequest);
}
