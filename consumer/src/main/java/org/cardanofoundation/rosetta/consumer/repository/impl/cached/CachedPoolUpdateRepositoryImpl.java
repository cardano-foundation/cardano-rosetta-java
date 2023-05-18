package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.PoolHash;
import org.cardanofoundation.rosetta.common.entity.PoolUpdate;
import org.cardanofoundation.rosetta.consumer.repository.PoolUpdateRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedPoolUpdateRepository;
import java.util.Collection;
import java.util.List;
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
public class CachedPoolUpdateRepositoryImpl implements CachedPoolUpdateRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  PoolUpdateRepository poolUpdateRepository;



  @Override
  public Boolean existsByPoolHash(PoolHash poolHash) {
    List<PoolUpdate> poolUpdates = inMemoryCachedEntities.getPoolUpdateMap().get(poolHash);
    if (!CollectionUtils.isEmpty(poolUpdates)) {
      return true;
    }

    return !isNew(poolHash) && poolUpdateRepository.existsByPoolHash(poolHash);
  }

  @Override
  public PoolUpdate save(PoolUpdate entity) {
    inMemoryCachedEntities.getPoolUpdateMap().add(entity.getPoolHash(), entity);
    return entity;
  }

  @Override
  public void flushToDb() {
    poolUpdateRepository.saveAll(inMemoryCachedEntities.getPoolUpdateMap()
        .values()
        .stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toSet()));
    inMemoryCachedEntities.getPoolUpdateMap().clear();
  }
}
