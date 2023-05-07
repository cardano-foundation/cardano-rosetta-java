package org.cardanofoundation.rosetta.consumer.repository.cached;

import java.util.Optional;
import org.cardanofoundation.rosetta.common.entity.EpochParam;

public interface CachedEpochParamRepository extends BaseCachedRepository<EpochParam>{

  Integer findLastEpochParam();

  Optional<EpochParam> findPrevEpochParamByEpochNo(int epochNo);
}
