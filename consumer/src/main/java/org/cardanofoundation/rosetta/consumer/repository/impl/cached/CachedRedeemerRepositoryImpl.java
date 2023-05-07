package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.Redeemer;
import org.cardanofoundation.rosetta.consumer.repository.RedeemerRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedRedeemerRepository;
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
public class CachedRedeemerRepositoryImpl implements CachedRedeemerRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  RedeemerRepository redeemerRepository;

  @Override
  public List<Redeemer> saveAll(Collection<Redeemer> entities) {
    inMemoryCachedEntities.getRedeemers().addAll(entities);
    return new ArrayList<>(entities);
  }

  @Override
  public void flushToDb() {
    redeemerRepository.saveAll(inMemoryCachedEntities.getRedeemers());
    inMemoryCachedEntities.getRedeemers().clear();
  }
}
