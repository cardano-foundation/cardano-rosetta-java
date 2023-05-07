package org.cardanofoundation.rosetta.consumer.service;

import org.cardanofoundation.rosetta.common.entity.Datum;
import org.cardanofoundation.rosetta.consumer.dto.DatumDTO;
import java.util.Map;
import java.util.Set;

public interface DatumService {

  /**
   * Handle raw CDDL "datum" data
   *
   * @param dto   dto containing all processed "datum" map, currently processing
   *              tx witness, tx entity and aggregated tx
   */
  void handleDatum(DatumDTO dto);

  /**
   * Get all "datum" data by hashes
   *
   * @param hashes  "datum" hashes
   * @return        a map with key is "datum" has and value is its respective "datum"
   */
  Map<String, Datum> getDatumsByHashes(Set<String> hashes);
}
