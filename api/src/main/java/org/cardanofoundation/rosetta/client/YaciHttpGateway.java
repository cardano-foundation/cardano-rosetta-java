package org.cardanofoundation.rosetta.client;

import org.cardanofoundation.rosetta.client.model.domain.StakeAccountInfo;

public interface YaciHttpGateway {

    StakeAccountInfo getStakeAccountRewards(String stakeAddress);

}
