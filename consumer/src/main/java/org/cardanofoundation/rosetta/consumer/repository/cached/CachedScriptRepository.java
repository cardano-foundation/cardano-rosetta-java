package org.cardanofoundation.rosetta.consumer.repository.cached;

import org.cardanofoundation.rosetta.common.entity.Script;
import java.util.Map;
import java.util.Set;

public interface CachedScriptRepository extends BaseCachedRepository<Script> {

  Map<String, Script> getScriptByHashes(Set<String> hashes);
}
