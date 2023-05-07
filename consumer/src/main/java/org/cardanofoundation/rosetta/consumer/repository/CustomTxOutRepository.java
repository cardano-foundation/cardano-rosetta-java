package org.cardanofoundation.rosetta.consumer.repository;

import org.cardanofoundation.rosetta.consumer.projection.TxOutProjection;
import java.util.List;
import java.util.Set;
import org.springframework.data.util.Pair;

public interface CustomTxOutRepository {

  Set<TxOutProjection> findTxOutsByTxHashInAndTxIndexIn(
      List<Pair<String, Short>> txHashIndexPairs);
}
