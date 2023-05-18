package org.cardanofoundation.rosetta.consumer.repository.cached;

import org.cardanofoundation.rosetta.common.entity.MaTxOut;
import org.cardanofoundation.rosetta.common.entity.TxOut;
import java.util.Collection;

public interface CachedMultiAssetTxOutRepository extends BaseCachedRepository<MaTxOut> {

  Collection<MaTxOut> findAllByTxOutIn(Collection<TxOut> txOuts);
}
