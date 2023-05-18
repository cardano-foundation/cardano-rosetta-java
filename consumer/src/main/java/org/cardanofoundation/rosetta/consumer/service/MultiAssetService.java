package org.cardanofoundation.rosetta.consumer.service;

import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTxOut;
import org.cardanofoundation.rosetta.common.entity.MaTxOut;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.entity.TxOut;
import java.util.Collection;
import java.util.Map;
import org.springframework.util.MultiValueMap;

public interface MultiAssetService {

  /**
   * Handle all asset mints within a collection of success txs
   *
   * @param successTxs success txs collection
   * @param txMap a map with key is tx hash and value is the respective tx entity
   */
  void handleMultiAssetMint(Collection<AggregatedTx> successTxs, Map<byte[], Tx> txMap);

  /**
   * Update multi asset tx outs' idents (ident is the associated multi asset entity)
   * The returning MaTxOut collection may have lesser records than all original lists
   * combined, because some MaTxOuts do not have their assets minted before
   *
   * @param maTxOuts  a multivalued map with key is asset fingerprints, and value is a
   *                  list of associated asset tx outputs
   * @return          collection of asset tx outputs with updated idents
   */
  Collection<MaTxOut> updateIdentMaTxOuts(MultiValueMap<String, MaTxOut> maTxOuts);

  /**
   * Build multivalued map of asset outputs
   *
   * @param txOutput  aggregated tx out containing all asset outputs
   * @param txOut     target tx out entity associates with the asset outputs
   * @return          a multivalued map with key is asset fingerprints, and value is a
   *                  list of associated asset tx outputs
   */
  MultiValueMap<String, MaTxOut> buildMaTxOut(AggregatedTxOut txOutput, TxOut txOut);

}
