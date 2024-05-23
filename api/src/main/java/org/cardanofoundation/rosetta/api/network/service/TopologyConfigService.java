package org.cardanofoundation.rosetta.api.network.service;

import java.util.List;

import org.openapitools.client.model.Peer;

public interface TopologyConfigService {

  List<Peer> getPeers();
}
