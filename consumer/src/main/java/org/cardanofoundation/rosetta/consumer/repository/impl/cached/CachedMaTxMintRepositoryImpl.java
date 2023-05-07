package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.MaTxMint;
import org.cardanofoundation.rosetta.consumer.repository.MaTxMintRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedMaTxMintRepository;
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
public class CachedMaTxMintRepositoryImpl implements CachedMaTxMintRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  MaTxMintRepository maTxMintRepository;

  @Override
  public List<MaTxMint> saveAll(Collection<MaTxMint> entities) {
    inMemoryCachedEntities.getMaTxMints().addAll(entities);
    return new ArrayList<>(entities);
  }

  @Override
  public void flushToDb() {
    long startTime = System.currentTimeMillis();
    var maTxMints = inMemoryCachedEntities.getMaTxMints();
    var maTxMintsSize = maTxMints.size();
    maTxMintRepository.saveAll(maTxMints);
    inMemoryCachedEntities.getMaTxMints().clear();
    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    log.debug("MaTxMint {} elapsed: {} ms, {} second(s)", maTxMintsSize, totalTime, totalTime / 1000f);
  }
}
