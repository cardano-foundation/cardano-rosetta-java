package org.cardanofoundation.rosetta.consumer.repository.cached;

import org.cardanofoundation.rosetta.common.entity.Epoch;
import java.util.List;
import java.util.Optional;

public interface CachedEpochRepository extends BaseCachedRepository<Epoch> {

  Optional<Epoch> findEpochByNo(Integer no);

  List<Epoch> findAll();
}
