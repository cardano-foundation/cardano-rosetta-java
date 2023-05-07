package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.PoolRelay;
import org.cardanofoundation.rosetta.consumer.repository.PoolRelayRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedPoolRelayRepository;
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
public class CachedPoolRelayRepositoryImpl implements CachedPoolRelayRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  PoolRelayRepository poolRelayRepository;

  @Override
  public List<PoolRelay> saveAll(Collection<PoolRelay> entities) {
    inMemoryCachedEntities.getPoolRelays().addAll(entities);
    return new ArrayList<>(entities);
  }

  @Override
  public void flushToDb() {
    poolRelayRepository.saveAll(inMemoryCachedEntities.getPoolRelays());
    inMemoryCachedEntities.getPoolRelays().clear();
  }
}
