package org.cardanofoundation.rosetta.crawler.service.construction;

import org.cardanofoundation.rosetta.crawler.construction.data.NetWork;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkIdentifier;


public interface CheckService {
    void withNetworkValidation(NetworkIdentifier networkIdentifier);

    NetWork getSupportedNetwork(String networkId, Integer networkMagic);
}
