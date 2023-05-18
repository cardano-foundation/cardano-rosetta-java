package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.Epoch;
import org.cardanofoundation.rosetta.consumer.repository.EpochRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedEpochRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
public class CachedEpochRepositoryImpl implements CachedEpochRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  EpochRepository epochRepository;


  @Override
  public Optional<Epoch> findEpochByNo(Integer no) {
    return Optional.ofNullable(inMemoryCachedEntities.getEpochMap().get(no))
        .or(() -> epochRepository.findEpochByNo(no));
  }

  @Override
  public List<Epoch> findAll() {
    List<Epoch> epochs = epochRepository.findAll();
    Map<Integer, Epoch> epochMap = inMemoryCachedEntities.getEpochMap();
    epochs.forEach(epoch -> epochMap.put(epoch.getNo(), epoch));
    return new ArrayList<>(epochMap.values());
  }

  @Override
  public Epoch save(Epoch entity) {
    inMemoryCachedEntities.getEpochMap().put(entity.getNo(), entity);
    return entity;
  }

  @Override
  public List<Epoch> saveAll(Collection<Epoch> entities) {
    entities.forEach(epoch -> inMemoryCachedEntities.getEpochMap().put(epoch.getNo(), epoch));
    return new ArrayList<>(entities);
  }

  @Override
  public void flushToDb() {

  }
}
