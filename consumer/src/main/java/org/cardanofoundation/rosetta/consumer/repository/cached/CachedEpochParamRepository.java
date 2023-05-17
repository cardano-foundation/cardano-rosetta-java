package org.cardanofoundation.rosetta.consumer.repository.cached;

import org.cardanofoundation.rosetta.common.entity.EpochParam;
import java.util.Optional;

public interface CachedEpochParamRepository extends BaseCachedRepository<EpochParam>{

  Integer findLastEpochParam();

  Optional<EpochParam> findPrevEpochParamByEpochNo(int epochNo);
}
