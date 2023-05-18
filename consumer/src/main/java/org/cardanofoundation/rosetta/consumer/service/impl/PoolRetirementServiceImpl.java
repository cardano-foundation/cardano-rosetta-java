package org.cardanofoundation.rosetta.consumer.service.impl;

import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.common.entity.PoolRetire;
import org.cardanofoundation.rosetta.common.entity.PoolRetire.PoolRetireBuilder;
import org.cardanofoundation.rosetta.common.entity.Redeemer;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.ledgersync.certs.PoolRetirement;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedPoolHashRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedPoolRetireRepository;
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
public class PoolRetirementServiceImpl extends CertificateSyncService<PoolRetirement> {

  CachedPoolHashRepository cachedPoolHashRepository;
  CachedPoolRetireRepository cachedPoolRetireRepository;

  @Override
  public void handle(AggregatedBlock aggregatedBlock,
      PoolRetirement certificate, int certificateIdx, Tx tx, Redeemer redeemer) {
    PoolRetireBuilder<?, ?> poolRetireBuilder = PoolRetire.builder();

    cachedPoolHashRepository.findPoolHashByHashRaw(certificate.getPoolKeyHash())
        .ifPresentOrElse(poolRetireBuilder::poolHash, () -> {
          log.error("Pool hash with hash {} not found", certificate.getPoolKeyHash());
          log.error(
              "Block number: {}, block hash: {}",
              aggregatedBlock.getBlockNo(),
              aggregatedBlock.getHash());
          throw new IllegalStateException();
        });

    poolRetireBuilder.certIndex(certificateIdx);
    poolRetireBuilder.announcedTx(tx);
    poolRetireBuilder.retiringEpoch((int) certificate.getEpoch());

    cachedPoolRetireRepository.save(poolRetireBuilder.build());
  }
}
