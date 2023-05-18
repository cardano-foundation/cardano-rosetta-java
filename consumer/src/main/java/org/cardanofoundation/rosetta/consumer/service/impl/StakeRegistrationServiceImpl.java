package org.cardanofoundation.rosetta.consumer.service.impl;

import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.common.entity.Redeemer;
import org.cardanofoundation.rosetta.common.entity.StakeAddress;
import org.cardanofoundation.rosetta.common.entity.StakeRegistration.StakeRegistrationBuilder;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.ledgersync.certs.StakeRegistration;
import org.cardanofoundation.rosetta.common.util.AddressUtil;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedStakeRegistrationRepository;
import org.cardanofoundation.rosetta.consumer.service.CertificateSyncService;
import org.cardanofoundation.rosetta.consumer.service.StakeAddressService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StakeRegistrationServiceImpl extends CertificateSyncService<StakeRegistration> {

  CachedStakeRegistrationRepository cachedStakeRegistrationRepository;

  StakeAddressService stakeAddressService;

  @Override
  public void handle(AggregatedBlock aggregatedBlock,
      StakeRegistration certificate, int certificateIdx,
      Tx tx, Redeemer redeemer) {
    StakeRegistrationBuilder<?, ?> stakeRegistrationBuilder =
        org.cardanofoundation.rosetta.common.entity.StakeRegistration.builder();

    String stakeAddressHex = AddressUtil.getRewardAddressString(
        certificate.getStakeCredential(), aggregatedBlock.getNetwork());
    StakeAddress stakeAddress = stakeAddressService.getStakeAddress(stakeAddressHex);
    stakeRegistrationBuilder.addr(stakeAddress);
    stakeRegistrationBuilder.certIndex(certificateIdx);
    stakeRegistrationBuilder.epochNo(aggregatedBlock.getEpochNo());
    stakeRegistrationBuilder.tx(tx);

    cachedStakeRegistrationRepository.save(stakeRegistrationBuilder.build());
  }
}
