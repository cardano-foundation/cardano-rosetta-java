package org.cardanofoundation.rosetta.consumer.service.impl.certificate;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.common.entity.Delegation;
import org.cardanofoundation.rosetta.common.entity.Delegation.DelegationBuilder;
import org.cardanofoundation.rosetta.common.entity.Redeemer;
import org.cardanofoundation.rosetta.common.entity.StakeAddress;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.ledgersync.certs.StakeDelegation;
import org.cardanofoundation.rosetta.common.util.AddressUtil;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.consumer.service.BatchCertificateDataService;
import org.cardanofoundation.rosetta.consumer.service.CertificateSyncService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StakeDelegationServiceImpl extends CertificateSyncService<StakeDelegation> {

  BatchCertificateDataService batchCertificateDataService;

  @Override
  public void handle(AggregatedBlock aggregatedBlock,
                     StakeDelegation certificate, int certificateIdx,
                     Tx tx, Redeemer redeemer, Map<String, StakeAddress> stakeAddressMap) {
    DelegationBuilder<?, ?> delegationBuilder = Delegation.builder();

    String stakeAddressHex = AddressUtil.getRewardAddressString(
        certificate.getStakeCredential(), aggregatedBlock.getNetwork());
    Optional.ofNullable(stakeAddressMap.get(stakeAddressHex))
        .ifPresentOrElse(delegationBuilder::address, () -> {
          log.error("Stake address with address hex {} not found", stakeAddressHex);
          log.error(
              "Block number: {}, block hash: {}",
              aggregatedBlock.getBlockNo(),
              aggregatedBlock.getHash());
          throw new IllegalStateException();
        });

    delegationBuilder.certIndex(certificateIdx);

    String poolHashHex = certificate.getStakePoolId().getPoolKeyHash();
    batchCertificateDataService.findPoolHashByHashRaw(poolHashHex)
        .ifPresentOrElse(poolHash -> {
          delegationBuilder.poolHash(poolHash);

          // This part will only save this entity to cache layer for fast future use
          batchCertificateDataService.savePoolHash(poolHash);
        }, () -> {
          log.error("Pool hash with hash {} not found", poolHashHex);
          log.error(
              "Block number: {}, block hash: {}",
              aggregatedBlock.getBlockNo(),
              aggregatedBlock.getPrevBlockHash());
          throw new IllegalStateException();
        });

    int epochNo = aggregatedBlock.getEpochNo();
    // The first epoch where this delegation is valid
    delegationBuilder.activeEpochNo(epochNo + 2);

    delegationBuilder.slotNo(aggregatedBlock.getSlotNo());
    delegationBuilder.tx(tx);
    delegationBuilder.redeemer(redeemer);
    batchCertificateDataService.saveDelegation(delegationBuilder.build());
  }
}
