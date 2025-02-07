package org.cardanofoundation.rosetta.api.account.mapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.client.model.domain.StakeAccountInfo;

import static org.cardanofoundation.rosetta.common.util.Constants.LOVELACE;

@Service
@Slf4j
public class AddressBalanceMapperImpl implements AddressBalanceMapper {

    @Override
    public AddressBalance convertToAdaAddressBalance(StakeAccountInfo stakeAccountInfo, Long number) {
        return AddressBalance.builder()
                .address(stakeAccountInfo.getStakeAddress())
                .unit(LOVELACE)
                .quantity(stakeAccountInfo.getWithdrawableAmount())
                .number(number)
                .build();
    }

}
