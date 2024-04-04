package org.cardanofoundation.rosetta.api.network.service;


import com.bloxbean.cardano.client.common.model.Network;
import org.openapitools.client.model.*;

import java.io.IOException;

public interface NetworkService {
    NetworkListResponse getNetworkList(final MetadataRequest metadataRequest)
        throws IOException;

    NetworkOptionsResponse getNetworkOptions(final NetworkRequest networkRequest)
        throws IOException, InterruptedException;

    NetworkStatusResponse getNetworkStatus(final NetworkRequest networkRequest)
        throws IOException;

    Network getSupportedNetwork();
}
