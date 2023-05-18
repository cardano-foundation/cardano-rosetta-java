package org.cardanofoundation.rosetta.consumer.repository.cached;

import org.cardanofoundation.rosetta.common.entity.Datum;
import java.util.Map;
import java.util.Set;

public interface CachedDatumRepository extends BaseCachedRepository<Datum>{

  Set<String> getExistHashByHashIn(Set<String> datumsHash);

  Map<String, Datum> getDatumByHashes(Set<String> hashes);
}
