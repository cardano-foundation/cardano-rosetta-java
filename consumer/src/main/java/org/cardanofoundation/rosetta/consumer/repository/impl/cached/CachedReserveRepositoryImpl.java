package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.Reserve;
import org.cardanofoundation.rosetta.consumer.repository.ReserveRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedReserveRepository;
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
public class CachedReserveRepositoryImpl implements CachedReserveRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  ReserveRepository reserveRepository;

  @Override
  public List<Reserve> saveAll(Collection<Reserve> entities) {
    inMemoryCachedEntities.getReserves().addAll(entities);
    return new ArrayList<>(entities);
  }

  @Override
  public void flushToDb() {
    reserveRepository.saveAll(inMemoryCachedEntities.getReserves());
    inMemoryCachedEntities.getReserves().clear();
  }
}
