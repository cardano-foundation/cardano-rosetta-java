package org.cardanofoundation.rosetta.api.network.service;


import java.io.IOException;

import com.bloxbean.cardano.client.common.model.Network;
import org.openapitools.client.model.*;

public interface NetworkService {
    NetworkListResponse getNetworkList(final MetadataRequest metadataRequest)
        throws IOException;

    NetworkOptionsResponse getNetworkOptions(final NetworkRequest networkRequest)
        throws IOException, InterruptedException;

    NetworkStatusResponse getNetworkStatus(final NetworkRequest networkRequest)
        throws IOException;

    Network getSupportedNetwork();
}
