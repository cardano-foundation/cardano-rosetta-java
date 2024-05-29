package org.cardanofoundation.rosetta.yaciindexer.mapper;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import com.bloxbean.cardano.yaci.store.staking.domain.PoolRegistration;
import com.bloxbean.cardano.yaci.store.staking.domain.PoolRetirement;
import com.bloxbean.cardano.yaci.store.staking.storage.impl.mapper.PoolMapperImpl;
import com.bloxbean.cardano.yaci.store.staking.storage.impl.model.PoolRegistrationEnity;
import com.bloxbean.cardano.yaci.store.staking.storage.impl.model.PoolRetirementEntity;

@Component
@Primary
public class CustomPoolMapper extends PoolMapperImpl {

  @Override
  public PoolRegistrationEnity toPoolRegistrationEntity(PoolRegistration poolRegistrationDetail) {
    return PoolRegistrationEnity.builder()
        .txHash(poolRegistrationDetail.getTxHash())
        .certIndex(poolRegistrationDetail.getCertIndex())
        .poolId(poolRegistrationDetail.getPoolId())
        .vrfKeyHash(poolRegistrationDetail.getVrfKeyHash())
        .pledge(poolRegistrationDetail.getPledge())
        .cost(poolRegistrationDetail.getCost())
        .margin(poolRegistrationDetail.getMargin())
        .rewardAccount(poolRegistrationDetail.getRewardAccount())
        .poolOwners(poolRegistrationDetail.getPoolOwners())
        .relays(poolRegistrationDetail.getRelays())
        .updateDateTime(null)
        .build();
  }

  @Override
  public PoolRetirementEntity toPoolRetirementEntity(PoolRetirement poolRetirement) {
    return PoolRetirementEntity.builder()
        .txHash(poolRetirement.getTxHash())
        .certIndex(poolRetirement.getCertIndex())
        .poolId(poolRetirement.getPoolId())
        .epoch(poolRetirement.getEpoch())
        .updateDateTime(null)
        .build();
  }
}
