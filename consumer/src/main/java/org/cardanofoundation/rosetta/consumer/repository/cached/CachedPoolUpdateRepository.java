package org.cardanofoundation.rosetta.consumer.repository.cached;

import org.cardanofoundation.rosetta.common.entity.PoolHash;
import org.cardanofoundation.rosetta.common.entity.PoolUpdate;

public interface CachedPoolUpdateRepository extends BaseCachedRepository<PoolUpdate> {

  Boolean existsByPoolHash(PoolHash poolHash);
}
