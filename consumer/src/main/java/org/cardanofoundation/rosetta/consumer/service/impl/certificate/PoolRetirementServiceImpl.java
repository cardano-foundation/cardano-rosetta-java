package org.cardanofoundation.rosetta.consumer.service.impl.certificate;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.common.entity.PoolRetire;
import org.cardanofoundation.rosetta.common.entity.PoolRetire.PoolRetireBuilder;
import org.cardanofoundation.rosetta.common.entity.Redeemer;
import org.cardanofoundation.rosetta.common.entity.StakeAddress;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.ledgersync.certs.PoolRetirement;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.consumer.service.BatchCertificateDataService;
import org.cardanofoundation.rosetta.consumer.service.CertificateSyncService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PoolRetirementServiceImpl extends CertificateSyncService<PoolRetirement> {

  BatchCertificateDataService batchCertificateDataService;

  @Override
  public void handle(AggregatedBlock aggregatedBlock,
                     PoolRetirement certificate, int certificateIdx, Tx tx, Redeemer redeemer,
                     Map<String, StakeAddress> stakeAddressMap) {
    PoolRetireBuilder<?, ?> poolRetireBuilder = PoolRetire.builder();

    batchCertificateDataService.findPoolHashByHashRaw(certificate.getPoolKeyHash())
        .ifPresentOrElse(poolHash -> {
          poolRetireBuilder.poolHash(poolHash);

          // This part will only save this entity to cache layer for fast future use
          batchCertificateDataService.savePoolHash(poolHash);
        }, () -> {
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

    batchCertificateDataService.savePoolRetire(poolRetireBuilder.build());
  }
}
