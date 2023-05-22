package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.consumer.repository.TxRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedTxRepository;
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
public class CachedTxRepositoryImpl implements CachedTxRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  TxRepository txRepository;


  @Override
  public List<Tx> saveAll(Collection<Tx> entities) {
    entities.forEach(tx -> inMemoryCachedEntities.getTxMap().put(tx.getHash(), tx));

    return new ArrayList<>(entities);
  }

  @Override
  public void flushToDb() {
    long startTime = System.currentTimeMillis();
    var txs = inMemoryCachedEntities.getTxMap().values();
    var txsSize = txs.size();
    txRepository.saveAll(txs);
    inMemoryCachedEntities.getTxMap().clear();
    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    log.debug("Tx {} elapsed: {} ms, {} second(s)", txsSize, totalTime, totalTime / 1000f);
  }
}
