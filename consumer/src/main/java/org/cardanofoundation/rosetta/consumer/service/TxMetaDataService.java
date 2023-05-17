package org.cardanofoundation.rosetta.consumer.service;

import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.entity.TxMetadata;
import java.util.List;
import java.util.Map;

public interface TxMetaDataService {

  /**
   * Handle CDDL block auxiliary data, convert them into tx metadata
   * and save them
   *
   * @param txMap a map with key is tx hash and value is the respective tx entity
   * @return      list of handled tx metadata entities
   */
  List<TxMetadata> handleAuxiliaryDataMaps(Map<String, Tx> txMap);
}
