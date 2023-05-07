package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.PotTransfer;
import org.cardanofoundation.rosetta.consumer.repository.PotTransferRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedPotTransferRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CachedPotTransferRepositoryImpl implements CachedPotTransferRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  PotTransferRepository potTransferRepository;

  @Override
  public PotTransfer save(PotTransfer entity) {
    inMemoryCachedEntities.getPotTransfers().add(entity);
    return entity;
  }

  @Override
  public void flushToDb() {
    potTransferRepository.saveAll(inMemoryCachedEntities.getPotTransfers());
    inMemoryCachedEntities.getPotTransfers().clear();
  }
}
