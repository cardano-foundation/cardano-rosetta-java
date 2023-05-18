package org.cardanofoundation.rosetta.consumer.repository.cached;

import org.cardanofoundation.rosetta.common.entity.RedeemerData;
import java.util.Collection;
import java.util.Map;

public interface CachedRedeemerDataRepository extends BaseCachedRepository<RedeemerData> {

  Map<String, RedeemerData> findAllByHashIn(Collection<String> hashes);
}
