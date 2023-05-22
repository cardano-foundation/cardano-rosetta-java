package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.StakeRegistration;
import org.cardanofoundation.rosetta.consumer.repository.StakeRegistrationRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedStakeRegistrationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CachedStakeRegistrationRepositoryImpl implements CachedStakeRegistrationRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  StakeRegistrationRepository stakeRegistrationRepository;

  @Override
  public StakeRegistration save(StakeRegistration entity) {
    inMemoryCachedEntities.getStakeRegistrations().add(entity);
    return entity;
  }

  @Override
  public void flushToDb() {
    stakeRegistrationRepository.saveAll(inMemoryCachedEntities.getStakeRegistrations());
    inMemoryCachedEntities.getStakeRegistrations().clear();
  }
}
