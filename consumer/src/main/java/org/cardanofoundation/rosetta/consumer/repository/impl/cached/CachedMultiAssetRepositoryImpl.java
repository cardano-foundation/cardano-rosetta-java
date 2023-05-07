package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import static org.cardanofoundation.rosetta.consumer.constant.ConsumerConstant.BATCH_QUERY_SIZE;

import com.google.common.collect.Lists;
import org.cardanofoundation.rosetta.common.entity.MultiAsset;
import org.cardanofoundation.rosetta.consumer.repository.MultiAssetRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedMultiAssetRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CachedMultiAssetRepositoryImpl implements CachedMultiAssetRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  MultiAssetRepository multiAssetRepository;

  @Override
  public List<MultiAsset> findMultiAssetsByFingerprintIn(Set<String> fingerprints) {
    Set<String> nonExistentFingerprints = new HashSet<>();
    List<MultiAsset> multiAssets = new ArrayList<>();

    fingerprints.forEach(fingerprint -> {
      MultiAsset multiAsset = inMemoryCachedEntities.getMultiAssetMap().get(fingerprint);
      if (Objects.isNull(multiAsset)) {
        nonExistentFingerprints.add(fingerprint);
        return;
      }

      multiAssets.add(multiAsset);
    });

    int cacheHit = multiAssets.size();
    long startTime = System.currentTimeMillis();

    var queryBatches = Lists.partition(new ArrayList<>(nonExistentFingerprints), BATCH_QUERY_SIZE);
    queryBatches.forEach(batch -> {
      List<MultiAsset> multiAssetList = multiAssetRepository.findMultiAssetsByFingerprintIn(batch);
      multiAssets.addAll(multiAssetList);
    });

    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    log.trace("Find multi asset by fingerprint: total {}, cache hit {}, query {}, time {} second(s)", fingerprints.size(),
        cacheHit, nonExistentFingerprints.size(), totalTime / 1000f);

    return multiAssets;
  }

  @Override
  public List<MultiAsset> saveAll(Collection<MultiAsset> entities) {
    entities.parallelStream().forEach(multiAsset ->
        inMemoryCachedEntities.getMultiAssetMap().put(multiAsset.getFingerprint(), multiAsset));
    return new ArrayList<>(entities);
  }

  @Override
  public void flushToDb() {
    long startTime = System.currentTimeMillis();
    var multiAssets = inMemoryCachedEntities.getMultiAssetMap().values();
    var multiAssetsSize = multiAssets.size();
    multiAssetRepository.saveAll(multiAssets);
    inMemoryCachedEntities.getMultiAssetMap().clear();
    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    log.debug("MultiAsset {} elapsed: {} ms, {} second(s)", multiAssetsSize, totalTime,
        totalTime / 1000f);
  }
}
