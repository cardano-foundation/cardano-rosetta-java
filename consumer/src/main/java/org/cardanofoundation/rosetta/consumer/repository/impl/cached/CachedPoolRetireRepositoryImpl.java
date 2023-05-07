package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.PoolRetire;
import org.cardanofoundation.rosetta.consumer.repository.PoolRetireRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedPoolRetireRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CachedPoolRetireRepositoryImpl implements CachedPoolRetireRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  PoolRetireRepository poolRetireRepository;

  @Override
  public PoolRetire save(PoolRetire entity) {
    inMemoryCachedEntities.getPoolRetires().add(entity);
    return entity;
  }

  @Override
  public void flushToDb() {
    poolRetireRepository.saveAll(inMemoryCachedEntities.getPoolRetires());
    inMemoryCachedEntities.getPoolRetires().clear();
  }
}
