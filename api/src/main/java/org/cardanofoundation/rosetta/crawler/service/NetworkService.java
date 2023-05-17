package org.cardanofoundation.rosetta.crawler.service;

import java.io.IOException;
import org.cardanofoundation.rosetta.crawler.exception.ServerException;
import org.cardanofoundation.rosetta.crawler.model.Network;
import org.cardanofoundation.rosetta.crawler.model.rest.MetadataRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkListResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkOptionsResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkStatusResponse;

public interface NetworkService {
    NetworkListResponse getNetworkList(final MetadataRequest metadataRequest);

    NetworkOptionsResponse getNetworkOptions(final NetworkRequest networkRequest);

    NetworkStatusResponse getNetworkStatus(final NetworkRequest networkRequest)
        throws IOException, ServerException;

    Network getSupportedNetwork();
}
