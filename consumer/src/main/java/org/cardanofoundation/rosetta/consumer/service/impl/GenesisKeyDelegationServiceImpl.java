package org.cardanofoundation.rosetta.consumer.service.impl;

import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.common.entity.Redeemer;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.ledgersync.certs.GenesisKeyDelegation;
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
public class GenesisKeyDelegationServiceImpl extends CertificateSyncService<GenesisKeyDelegation> {

  @Override
  public void handle(AggregatedBlock aggregatedBlock,
      GenesisKeyDelegation certificate, int certificateIdx, Tx tx, Redeemer redeemer) {
    // No-op
  }
}
