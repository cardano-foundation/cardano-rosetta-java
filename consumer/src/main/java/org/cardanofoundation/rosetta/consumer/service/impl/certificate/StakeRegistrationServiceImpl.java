package org.cardanofoundation.rosetta.consumer.service.impl.certificate;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.common.entity.Redeemer;
import org.cardanofoundation.rosetta.common.entity.StakeAddress;
import org.cardanofoundation.rosetta.common.entity.StakeRegistration.StakeRegistrationBuilder;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.ledgersync.certs.StakeRegistration;
import org.cardanofoundation.rosetta.common.util.AddressUtil;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.consumer.service.BatchCertificateDataService;
import org.cardanofoundation.rosetta.consumer.service.CertificateSyncService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StakeRegistrationServiceImpl extends CertificateSyncService<StakeRegistration> {

  BatchCertificateDataService batchCertificateDataService;

  @Override
  public void handle(AggregatedBlock aggregatedBlock,
                     StakeRegistration certificate, int certificateIdx,
                     Tx tx, Redeemer redeemer, Map<String, StakeAddress> stakeAddressMap) {
    StakeRegistrationBuilder<?, ?> stakeRegistrationBuilder =
        org.cardanofoundation.rosetta.common.entity.StakeRegistration.builder();

    String stakeAddressHex = AddressUtil.getRewardAddressString(
        certificate.getStakeCredential(), aggregatedBlock.getNetwork());
    StakeAddress stakeAddress = stakeAddressMap.get(stakeAddressHex);
    stakeRegistrationBuilder.addr(stakeAddress);
    stakeRegistrationBuilder.certIndex(certificateIdx);
    stakeRegistrationBuilder.epochNo(aggregatedBlock.getEpochNo());
    stakeRegistrationBuilder.tx(tx);

    batchCertificateDataService.saveStakeRegistration(stakeRegistrationBuilder.build());
  }
}
