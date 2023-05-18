package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.RedeemerData;
import org.cardanofoundation.rosetta.consumer.repository.RedeemerDataRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedRedeemerDataRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

@Slf4j
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CachedRedeemerDataRepositoryImpl implements CachedRedeemerDataRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  RedeemerDataRepository redeemerDataRepository;

  @Override
  public Map<String, RedeemerData> findAllByHashIn(Collection<String> hashes) {
    Map<String, RedeemerData> redeemerDataMap = new LinkedHashMap<>();
    Set<String> nonExistentHash = new HashSet<>();

    hashes.forEach(redeemerDataHash -> {
      RedeemerData redeemerData = inMemoryCachedEntities.getRedeemerDataMap()
          .get(redeemerDataHash);
      if (Objects.isNull(redeemerData)) {
        nonExistentHash.add(redeemerDataHash);
        return;
      }

      redeemerDataMap.put(redeemerDataHash, redeemerData);
    });

    if (!CollectionUtils.isEmpty(nonExistentHash)) {
      List<RedeemerData> redeemerDataList = redeemerDataRepository.findAllByHashIn(nonExistentHash);
      redeemerDataList.forEach(redeemerData -> {
        redeemerDataMap.put(redeemerData.getHash(), redeemerData);
      });
    }

    return redeemerDataMap;
  }

  @Override
  public List<RedeemerData> saveAll(Collection<RedeemerData> entities) {
    entities.forEach(data ->
        inMemoryCachedEntities.getRedeemerDataMap().putIfAbsent(data.getHash(), data));
    return new ArrayList<>(entities);
  }

  @Override
  public void flushToDb() {
    redeemerDataRepository.saveAll(inMemoryCachedEntities.getRedeemerDataMap().values());
    inMemoryCachedEntities.getRedeemerDataMap().clear();
  }
}
