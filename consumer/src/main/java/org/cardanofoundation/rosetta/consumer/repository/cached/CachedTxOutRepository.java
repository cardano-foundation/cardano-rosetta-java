package org.cardanofoundation.rosetta.consumer.repository.cached;

import org.cardanofoundation.rosetta.common.entity.TxOut;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.util.Pair;

public interface CachedTxOutRepository extends BaseCachedRepository<TxOut> {

  Collection<TxOut> findTxOutsByTxHashInAndTxIndexIn(Collection<Pair<String, Short>> txHashIndexPairs);

  Optional<TxOut> findTxOutByTxHashAndTxOutIndex(String txHash, Short index);
}
