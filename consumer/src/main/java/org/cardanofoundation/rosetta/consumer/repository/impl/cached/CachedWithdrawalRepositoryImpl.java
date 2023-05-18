package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.Withdrawal;
import org.cardanofoundation.rosetta.consumer.repository.WithdrawalRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedWithdrawalRepository;
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
public class CachedWithdrawalRepositoryImpl implements CachedWithdrawalRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  WithdrawalRepository withdrawalRepository;

  @Override
  public List<Withdrawal> saveAll(Collection<Withdrawal> entities) {
    inMemoryCachedEntities.getWithdrawals().addAll(entities);
    return new ArrayList<>(entities);
  }

  @Override
  public void flushToDb() {
    withdrawalRepository.saveAll(inMemoryCachedEntities.getWithdrawals());
    inMemoryCachedEntities.getWithdrawals().clear();
  }
}
