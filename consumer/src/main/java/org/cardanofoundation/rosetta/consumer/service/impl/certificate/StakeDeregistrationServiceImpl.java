package org.cardanofoundation.rosetta.consumer.service.impl.certificate;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.common.entity.Redeemer;
import org.cardanofoundation.rosetta.common.entity.StakeAddress;
import org.cardanofoundation.rosetta.common.entity.StakeDeregistration.StakeDeregistrationBuilder;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.ledgersync.certs.StakeDeregistration;
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
public class StakeDeregistrationServiceImpl extends CertificateSyncService<StakeDeregistration> {

  BatchCertificateDataService batchCertificateDataService;

  @Override
  public void handle(AggregatedBlock aggregatedBlock,
                     StakeDeregistration certificate, int certificateIdx,
                     Tx tx, Redeemer redeemer, Map<String, StakeAddress> stakeAddressMap) {
    StakeDeregistrationBuilder<?, ?> stakeDeregistrationBuilder =
        org.cardanofoundation.rosetta.common.entity.StakeDeregistration.builder();

    String stakeAddressHex = AddressUtil.getRewardAddressString(
        certificate.getStakeCredential(), aggregatedBlock.getNetwork());
    Optional.ofNullable(stakeAddressMap.get(stakeAddressHex))
        .ifPresentOrElse(stakeDeregistrationBuilder::addr, () -> {
          log.error("Stake address with address hex {} not found", stakeAddressHex);
          log.error(
              "Block number: {}, block hash: {}",
              aggregatedBlock.getBlockNo(),
              aggregatedBlock.getHash());
          throw new IllegalStateException();
        });

    stakeDeregistrationBuilder.certIndex(certificateIdx);

    stakeDeregistrationBuilder.epochNo(aggregatedBlock.getEpochNo());
    stakeDeregistrationBuilder.tx(tx);
    stakeDeregistrationBuilder.redeemer(redeemer);

    batchCertificateDataService.saveStakeDeregistration(stakeDeregistrationBuilder.build());
  }
}
