package org.cardanofoundation.rosetta.consumer.service.impl;


import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.common.entity.Redeemer;
import org.cardanofoundation.rosetta.common.entity.StakeDeregistration.StakeDeregistrationBuilder;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.ledgersync.certs.StakeDeregistration;
import org.cardanofoundation.rosetta.common.util.AddressUtil;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedStakeAddressRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedStakeDeregistrationRepository;
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
public class StakeDeregistrationServiceImpl extends CertificateSyncService<StakeDeregistration> {

  CachedStakeAddressRepository cachedStakeAddressRepository;
  CachedStakeDeregistrationRepository cachedStakeDeregistrationRepository;

  @Override
  public void handle(AggregatedBlock aggregatedBlock,
      StakeDeregistration certificate, int certificateIdx,
      Tx tx, Redeemer redeemer) {
    StakeDeregistrationBuilder<?, ?> stakeDeregistrationBuilder =
        org.cardanofoundation.rosetta.common.entity.StakeDeregistration.builder();

    String stakeAddressHex = AddressUtil.getRewardAddressString(
        certificate.getStakeCredential(), aggregatedBlock.getNetwork());
    cachedStakeAddressRepository.findByHashRaw(stakeAddressHex)
        .ifPresentOrElse(stakeDeregistrationBuilder::addr, () -> {
          log.error("Stake address with address hex {} not found", stakeAddressHex);
          log.error(
              "Block number: {}, block hash: {}",
              aggregatedBlock.getBlockNo(),
              aggregatedBlock.getHash());
          System.exit(0);
        });

    stakeDeregistrationBuilder.certIndex(certificateIdx);

    stakeDeregistrationBuilder.epochNo(aggregatedBlock.getEpochNo());
    stakeDeregistrationBuilder.tx(tx);
    stakeDeregistrationBuilder.redeemer(redeemer);

    cachedStakeDeregistrationRepository.save(stakeDeregistrationBuilder.build());
  }
}
