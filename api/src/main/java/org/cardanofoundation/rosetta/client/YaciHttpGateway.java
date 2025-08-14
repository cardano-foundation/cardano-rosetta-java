package org.cardanofoundation.rosetta.client;

import org.cardanofoundation.rosetta.client.model.domain.DiscoveredPeer;
import org.cardanofoundation.rosetta.client.model.domain.StakeAccountInfo;

import java.util.List;

public interface YaciHttpGateway {

    StakeAccountInfo getStakeAccountRewards(String stakeAddress);

    List<DiscoveredPeer> getDiscoveredPeers();

}
