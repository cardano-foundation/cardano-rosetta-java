package org.cardanofoundation.rosetta.yaciindexer.mapper;

import com.bloxbean.cardano.yaci.store.account.domain.AddressBalance;
import com.bloxbean.cardano.yaci.store.account.domain.StakeAddressBalance;
import com.bloxbean.cardano.yaci.store.account.storage.impl.mapper.AccountMapperImpl_;
import com.bloxbean.cardano.yaci.store.account.storage.impl.model.AddressBalanceEntity;
import com.bloxbean.cardano.yaci.store.account.storage.impl.model.StakeAddressBalanceEntity;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class CustomAccountMapper extends AccountMapperImpl_ {

  @Override
  public AddressBalanceEntity toAddressBalanceEntity(AddressBalance addressBalance) {
    return AddressBalanceEntity.builder()
        .address(addressBalance.getAddress())
        .unit(addressBalance.getUnit())
        .slot(addressBalance.getSlot())
        .quantity(addressBalance.getQuantity())
        .paymentCredential(null)
        .updateDateTime(null)
        .blockTime(null)
        .build();
  }

  @Override
  public StakeAddressBalanceEntity toStakeBalanceEntity(StakeAddressBalance stakeBalance) {
    return StakeAddressBalanceEntity.builder()
        .address(stakeBalance.getAddress())
        .slot(stakeBalance.getSlot())
        .quantity(stakeBalance.getQuantity())
        .updateDateTime(null)
        .build();
  }
}
