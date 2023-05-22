package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.Treasury;
import org.cardanofoundation.rosetta.consumer.repository.TreasuryRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedTreasuryRepository;
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
public class CachedTreasuryRepositoryImpl implements CachedTreasuryRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  TreasuryRepository treasuryRepository;

  @Override
  public List<Treasury> saveAll(Collection<Treasury> entities) {
    inMemoryCachedEntities.getTreasuries().addAll(entities);
    return new ArrayList<>(entities);
  }

  @Override
  public void flushToDb() {
    treasuryRepository.saveAll(inMemoryCachedEntities.getTreasuries());
    inMemoryCachedEntities.getTreasuries().clear();
  }
}
