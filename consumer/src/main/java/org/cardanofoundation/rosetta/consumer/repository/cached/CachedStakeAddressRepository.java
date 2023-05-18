package org.cardanofoundation.rosetta.consumer.repository.cached;

import org.cardanofoundation.rosetta.common.entity.StakeAddress;
import java.util.Collection;
import java.util.Optional;

public interface CachedStakeAddressRepository extends BaseCachedRepository<StakeAddress> {

  Optional<StakeAddress> findByHashRaw(String hashRaw);

  Collection<StakeAddress> findByHashRawIn(Collection<String> hashes);
}
