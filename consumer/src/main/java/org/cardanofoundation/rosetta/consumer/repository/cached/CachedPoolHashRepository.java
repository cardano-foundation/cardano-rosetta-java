package org.cardanofoundation.rosetta.consumer.repository.cached;

import org.cardanofoundation.rosetta.common.entity.PoolHash;
import java.util.Optional;

public interface CachedPoolHashRepository extends BaseCachedRepository<PoolHash> {

  Optional<PoolHash> findPoolHashByHashRaw(String hashRaw);
}
