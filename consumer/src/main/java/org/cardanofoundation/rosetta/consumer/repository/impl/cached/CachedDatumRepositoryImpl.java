package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.Datum;
import org.cardanofoundation.rosetta.consumer.projection.DatumProjection;
import org.cardanofoundation.rosetta.consumer.repository.DatumRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedDatumRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
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
public class CachedDatumRepositoryImpl implements CachedDatumRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  DatumRepository datumRepository;

  @Override
  public Set<String> getExistHashByHashIn(Set<String> datumsHash) {
    Set<String> nonExistentDatumHashes = new HashSet<>();
    Set<String> existingDatumHashes = new HashSet<>();

    datumsHash.forEach(datumHash -> {
      if (inMemoryCachedEntities.getDatumMap().containsKey(datumHash)) {
        existingDatumHashes.add(datumHash);
      } else {
        nonExistentDatumHashes.add(datumHash);
      }
    });

    if (!CollectionUtils.isEmpty(nonExistentDatumHashes)) {
      existingDatumHashes.addAll(datumRepository.getExistHashByHashIn(nonExistentDatumHashes));
    }
    return existingDatumHashes;
  }

  @Override
  public Map<String, Datum> getDatumByHashes(Set<String> hashes) {
    Set<String> nonExistentDatumHash = new HashSet<>();
    Map<String, Datum> datumMap = new ConcurrentHashMap<>();

    hashes.forEach(datumHash -> {
      Datum datum = inMemoryCachedEntities.getDatumMap().get(datumHash);
      if (Objects.isNull(datum)) {
        nonExistentDatumHash.add(datumHash);
        return;
      }

      datumMap.put(datumHash, datum);
    });

    if (!CollectionUtils.isEmpty(nonExistentDatumHash)) {
      datumMap.putAll(datumRepository.getDatumByHashes(nonExistentDatumHash)
          .stream()
          .collect(Collectors.toMap(
              DatumProjection::getHash,
              datumProjection -> Datum.builder()
                  .id(datumProjection.getId())
                  .hash(datumProjection.getHash())
                  .build())
          )
      );
    }

    return datumMap;
  }

  @Override
  public Datum save(Datum entity) {
    inMemoryCachedEntities.getDatumMap().putIfAbsent(entity.getHash(), entity);
    return entity;
  }

  @Override
  public List<Datum> saveAll(Collection<Datum> entities) {
    entities.forEach(
        datum -> inMemoryCachedEntities.getDatumMap().putIfAbsent(datum.getHash(), datum));
    return new ArrayList<>(entities);
  }

  @Override
  public void flushToDb() {
    long startTime = System.currentTimeMillis();
    var datums = inMemoryCachedEntities.getDatumMap().values();
    var datumsSize = datums.size();
    datumRepository.saveAll(datums);
    inMemoryCachedEntities.getDatumMap().clear();
    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    log.debug("Datum {} elapsed: {} ms, {} second(s)", datumsSize, totalTime, totalTime / 1000f);
  }
}
