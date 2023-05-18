package org.cardanofoundation.rosetta.api.service.construction;

import org.cardanofoundation.rosetta.api.construction.data.NetWork;
import org.cardanofoundation.rosetta.api.model.rest.NetworkIdentifier;


public interface CheckService {
    void withNetworkValidation(NetworkIdentifier networkIdentifier);

    NetWork getSupportedNetwork(String networkId, Integer networkMagic);
}
