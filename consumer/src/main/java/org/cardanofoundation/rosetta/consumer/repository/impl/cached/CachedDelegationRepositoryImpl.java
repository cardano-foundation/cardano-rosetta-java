package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.Delegation;
import org.cardanofoundation.rosetta.consumer.repository.DelegationRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedDelegationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CachedDelegationRepositoryImpl implements CachedDelegationRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  DelegationRepository delegationRepository;

  @Override
  public Delegation save(Delegation entity) {
    inMemoryCachedEntities.getDelegations().add(entity);
    return entity;
  }

  @Override
  public void flushToDb() {
    delegationRepository.saveAll(inMemoryCachedEntities.getDelegations());
    inMemoryCachedEntities.getDelegations().clear();
  }
}
