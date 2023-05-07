package org.cardanofoundation.rosetta.consumer.service.impl;

import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.common.entity.Delegation;
import org.cardanofoundation.rosetta.common.entity.Delegation.DelegationBuilder;
import org.cardanofoundation.rosetta.common.entity.Redeemer;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.ledgersync.certs.StakeDelegation;
import org.cardanofoundation.rosetta.common.util.AddressUtil;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedDelegationRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedPoolHashRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedStakeAddressRepository;
import org.cardanofoundation.rosetta.consumer.service.CertificateSyncService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StakeDelegationServiceImpl extends CertificateSyncService<StakeDelegation> {

  CachedStakeAddressRepository cachedStakeAddressRepository;
  CachedPoolHashRepository cachedPoolHashRepository;
  CachedDelegationRepository cachedDelegationRepository;

  @Override
  public void handle(AggregatedBlock aggregatedBlock,
      StakeDelegation certificate, int certificateIdx,
      Tx tx, Redeemer redeemer) {
    DelegationBuilder<?, ?> delegationBuilder = Delegation.builder();

    String stakeAddressHex = AddressUtil.getRewardAddressString(
        certificate.getStakeCredential(), aggregatedBlock.getNetwork());
    cachedStakeAddressRepository.findByHashRaw(stakeAddressHex)
        .ifPresentOrElse(delegationBuilder::address, () -> {
          log.error("Stake address with address hex {} not found", stakeAddressHex);
          log.error(
              "Block number: {}, block hash: {}",
              aggregatedBlock.getBlockNo(),
              aggregatedBlock.getHash());
          System.exit(0);
        });

    delegationBuilder.certIndex(certificateIdx);

    String poolHashHex = certificate.getStakePoolId().getPoolKeyHash();
    cachedPoolHashRepository.findPoolHashByHashRaw(poolHashHex)
        .ifPresentOrElse(delegationBuilder::poolHash, () -> {
          log.error("Pool hash with hash {} not found", poolHashHex);
          log.error(
              "Block number: {}, block hash: {}",
              aggregatedBlock.getBlockNo(),
              aggregatedBlock.getPrevBlockHash());
          throw new IllegalStateException();
        });

    long epochNo = aggregatedBlock.getEpochNo();
    // The first epoch where this delegation is valid
    delegationBuilder.activeEpochNo(epochNo + 2);

    delegationBuilder.slotNo(aggregatedBlock.getSlotNo());
    delegationBuilder.tx(tx);
    delegationBuilder.redeemer(redeemer);
    cachedDelegationRepository.save(delegationBuilder.build());
  }
}
