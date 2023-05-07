package org.cardanofoundation.rosetta.consumer.repository.cached;

import org.cardanofoundation.rosetta.common.entity.MultiAsset;
import java.util.List;
import java.util.Set;

public interface CachedMultiAssetRepository extends BaseCachedRepository<MultiAsset> {

  List<MultiAsset> findMultiAssetsByFingerprintIn(Set<String> fingerprints);
}
