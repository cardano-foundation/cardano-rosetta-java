package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.TxIn;
import org.cardanofoundation.rosetta.consumer.repository.TxInRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedTxInRepository;
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
public class CachedTxInRepositoryImpl implements CachedTxInRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  TxInRepository txInRepository;

  @Override
  public List<TxIn> saveAll(Collection<TxIn> entities) {
    inMemoryCachedEntities.getTxIns().addAll(entities);
    return new ArrayList<>(entities);
  }

  @Override
  public void flushToDb() {
    long startTime = System.currentTimeMillis();
    var txIns = inMemoryCachedEntities.getTxIns();
    var txInsSize = txIns.size();
    txInRepository.saveAll(txIns);
    inMemoryCachedEntities.getTxIns().clear();
    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    log.debug("TxIn {} elapsed: {} ms, {} second(s)", txInsSize, totalTime, totalTime / 1000f);
  }
}
