package org.cardanofoundation.rosetta.yaciindexer.service;

import java.util.Optional;

import org.cardanofoundation.rosetta.yaciindexer.domain.model.StakeAccountRewardInfo;

public interface AccountService {

    Optional<StakeAccountRewardInfo> getAccountInfo(String stakeAddress);

}
