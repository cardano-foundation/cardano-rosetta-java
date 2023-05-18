package org.cardanofoundation.rosetta.consumer.repository.cached;

import java.util.List;
import java.util.Optional;
import org.cardanofoundation.rosetta.common.entity.Epoch;

public interface CachedEpochRepository extends BaseCachedRepository<Epoch> {

  Optional<Epoch> findEpochByNo(Integer no);

  List<Epoch> findAll();
}
