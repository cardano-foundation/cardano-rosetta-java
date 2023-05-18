package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.StakeDeregistration;
import org.cardanofoundation.rosetta.consumer.repository.StakeDeregistrationRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedStakeDeregistrationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CachedStakeDeregistrationRepositoryImpl implements
    CachedStakeDeregistrationRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  StakeDeregistrationRepository stakeDeregistrationRepository;

  @Override
  public StakeDeregistration save(StakeDeregistration entity) {
    inMemoryCachedEntities.getStakeDeregistrations().add(entity);
    return entity;
  }

  @Override
  public void flushToDb() {
    stakeDeregistrationRepository.saveAll(inMemoryCachedEntities.getStakeDeregistrations());
    inMemoryCachedEntities.getStakeDeregistrations().clear();
  }
}
