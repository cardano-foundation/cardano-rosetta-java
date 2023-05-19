package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.SlotLeader;
import org.cardanofoundation.rosetta.consumer.repository.SlotLeaderRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedSlotLeaderRepository;
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
public class CachedSlotLeaderRepositoryImpl implements CachedSlotLeaderRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  SlotLeaderRepository slotLeaderRepository;

  @Override
  public Optional<SlotLeader> findSlotLeaderByHash(String hash) {
    return Optional.ofNullable(inMemoryCachedEntities.getSlotLeaderMap().get(hash))
        .or(() -> slotLeaderRepository.findSlotLeaderByHash(hash));
  }


  @Override
  public SlotLeader save(SlotLeader entity) {
    inMemoryCachedEntities.getSlotLeaderMap().putIfAbsent(entity.getHash(), entity);
    return entity;
  }

  @Override
  public void flushToDb() {
    slotLeaderRepository.saveAll(inMemoryCachedEntities.getSlotLeaderMap().values());
    inMemoryCachedEntities.getSlotLeaderMap().clear();
  }
}
