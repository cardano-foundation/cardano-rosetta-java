package org.cardanofoundation.rosetta.consumer.repository;

import org.cardanofoundation.rosetta.consumer.projection.TxOutProjection;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Optional;

public interface CustomTxOutRepository {

  List<TxOutProjection> findTxOutsByTxHashInAndTxIndexIn(
      List<Pair<String, Short>> txHashIndexPairs);

  Optional<TxOutProjection> findTxOutByTxHashAndTxOutIndex(String txHash, Short index);
}
