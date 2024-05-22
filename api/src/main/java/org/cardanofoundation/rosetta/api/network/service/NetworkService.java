package org.cardanofoundation.rosetta.api.network.service;


import com.bloxbean.cardano.client.common.model.Network;
import org.openapitools.client.model.MetadataRequest;
import org.openapitools.client.model.NetworkIdentifier;
import org.openapitools.client.model.NetworkListResponse;
import org.openapitools.client.model.NetworkOptionsResponse;
import org.openapitools.client.model.NetworkRequest;
import org.openapitools.client.model.NetworkStatusResponse;

public interface NetworkService {
    NetworkListResponse getNetworkList(final MetadataRequest metadataRequest);

    NetworkOptionsResponse getNetworkOptions(final NetworkRequest networkRequest);

    NetworkStatusResponse getNetworkStatus(final NetworkRequest networkRequest);

    Network getSupportedNetwork();

    void verifyNetworkRequest(final NetworkIdentifier networkRequest);

}
