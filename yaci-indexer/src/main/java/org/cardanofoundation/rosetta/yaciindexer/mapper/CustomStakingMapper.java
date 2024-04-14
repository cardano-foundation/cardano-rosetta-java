package org.cardanofoundation.rosetta.yaciindexer.mapper;

import com.bloxbean.cardano.yaci.store.staking.domain.Delegation;
import com.bloxbean.cardano.yaci.store.staking.domain.StakeRegistrationDetail;
import com.bloxbean.cardano.yaci.store.staking.storage.impl.mapper.StakingMapperImpl;
import com.bloxbean.cardano.yaci.store.staking.storage.impl.model.DelegationEntity;
import com.bloxbean.cardano.yaci.store.staking.storage.impl.model.StakeRegistrationEntity;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class CustomStakingMapper extends StakingMapperImpl {

  @Override
  public StakeRegistrationEntity toStakeResistrationEntity(
      StakeRegistrationDetail stakeRegistrationDetail) {
    return StakeRegistrationEntity.builder()
        .txHash(stakeRegistrationDetail.getTxHash())
        .certIndex(stakeRegistrationDetail.getCertIndex())
        .type(stakeRegistrationDetail.getType())
        .address(stakeRegistrationDetail.getAddress())
        .credential(stakeRegistrationDetail.getCredential())
        .build();
  }

  @Override
  public DelegationEntity toDelegationEntity(Delegation delegation) {
    return DelegationEntity.builder()
        .txHash(delegation.getTxHash())
        .certIndex(delegation.getCertIndex())
        .credential(delegation.getCredential())
        .poolId(delegation.getPoolId())
        .address(delegation.getAddress())
        .build();
  }
}
