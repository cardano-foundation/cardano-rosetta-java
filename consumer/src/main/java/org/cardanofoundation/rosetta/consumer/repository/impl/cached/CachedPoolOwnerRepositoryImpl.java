package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.PoolOwner;
import org.cardanofoundation.rosetta.consumer.repository.PoolOwnerRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedPoolOwnerRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CachedPoolOwnerRepositoryImpl implements CachedPoolOwnerRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  PoolOwnerRepository poolOwnerRepository;

  @Override
  public List<PoolOwner> saveAll(Collection<PoolOwner> entities) {
    inMemoryCachedEntities.getPoolOwners().addAll(entities);
    return new ArrayList<>(entities);
  }

  @Override
  public void flushToDb() {
    poolOwnerRepository.saveAll(inMemoryCachedEntities.getPoolOwners());
    inMemoryCachedEntities.getPoolOwners().clear();
  }
}
