package org.cardanofoundation.rosetta.consumer.repository.cached;

import org.cardanofoundation.rosetta.common.entity.ExtraKeyWitness;
import java.util.Set;

public interface CachedExtraKeyWitnessRepository extends BaseCachedRepository<ExtraKeyWitness> {

  Set<String> findByHashIn(Set<String> hashes);
}
