package org.cardanofoundation.rosetta.api.account.mapper;

import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.client.model.domain.StakeAccountInfo;

public interface AddressBalanceMapper {

    AddressBalance convertToAdaAddressBalance(StakeAccountInfo stakeAccountInfo, Long number);

}
