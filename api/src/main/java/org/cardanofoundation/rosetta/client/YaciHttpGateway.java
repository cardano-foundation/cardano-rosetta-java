package org.cardanofoundation.rosetta.client;

import java.util.List;

import org.cardanofoundation.rosetta.client.model.domain.DiscoveredPeer;
import org.cardanofoundation.rosetta.client.model.domain.StakeAccountInfo;

public interface YaciHttpGateway {

    StakeAccountInfo getStakeAccountRewards(String stakeAddress);

    List<DiscoveredPeer> getDiscoveredPeers();

}
