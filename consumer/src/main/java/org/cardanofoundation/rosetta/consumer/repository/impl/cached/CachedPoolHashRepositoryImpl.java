package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.PoolHash;
import org.cardanofoundation.rosetta.consumer.repository.PoolHashRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedPoolHashRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CachedPoolHashRepositoryImpl implements CachedPoolHashRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  PoolHashRepository poolHashRepository;


  @Override
  public Optional<PoolHash> findPoolHashByHashRaw(String hashRaw) {
    return Optional.ofNullable(inMemoryCachedEntities.getPoolHashMap().get(hashRaw))
        .or(() -> poolHashRepository.findPoolHashByHashRaw(hashRaw));
  }

  @Override
  public PoolHash save(PoolHash entity) {
    inMemoryCachedEntities.getPoolHashMap().putIfAbsent(entity.getHashRaw(), entity);
    return entity;
  }

  @Override
  public List<PoolHash> saveAll(Collection<PoolHash> entities) {
    entities.forEach(poolHash ->
        inMemoryCachedEntities.getPoolHashMap().putIfAbsent(poolHash.getHashRaw(), poolHash));
    return new ArrayList<>(entities);
  }

  @Override
  public void flushToDb() {
    poolHashRepository.saveAll(inMemoryCachedEntities.getPoolHashMap().values());
    inMemoryCachedEntities.getPoolHashMap().clear();
  }
}
