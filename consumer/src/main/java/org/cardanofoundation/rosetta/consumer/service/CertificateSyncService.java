package org.cardanofoundation.rosetta.consumer.service;

import org.cardanofoundation.rosetta.common.entity.Redeemer;
import org.cardanofoundation.rosetta.common.entity.StakeAddress;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.ledgersync.certs.Certificate;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;

import java.util.Map;

public abstract class CertificateSyncService<T extends Certificate> implements // NOSONAR
    SyncServiceInstance<T> {

  /**
   * Handle raw CDDL certificate data
   *
   * @param aggregatedBlock aggregated block where the certificate associates with
   * @param certificate     raw CDDL certificate data
   * @param certificateIdx  certificate idx within a tx
   * @param tx              target tx entity where the certificate associates with
   * @param redeemer        redeemer entity associates with the certificate
   * @param stakeAddressMap a map of stake address entities associated with all entities inside the
   *                        current block batch
   */
  public abstract void handle(AggregatedBlock aggregatedBlock, T certificate, int certificateIdx,
      Tx tx, Redeemer redeemer, Map<String, StakeAddress> stakeAddressMap);
}
