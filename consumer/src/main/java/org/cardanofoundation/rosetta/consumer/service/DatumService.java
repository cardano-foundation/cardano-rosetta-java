package org.cardanofoundation.rosetta.consumer.service;

import org.cardanofoundation.rosetta.common.entity.Datum;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface DatumService {

  /**
   * Handle raw CDDL "datum" data
   *
   * @param aggregatedTxs aggregated tx batch
   * @param txMap         transaction entity map
   */
  void handleDatum(Collection<AggregatedTx> aggregatedTxs, Map<String, Tx> txMap);

  /**
   * Get all "datum" data by hashes
   *
   * @param hashes "datum" hashes
   * @return a map with key is "datum" has and value is its respective "datum"
   */
  Map<String, Datum> getDatumsByHashes(Set<String> hashes);
}
