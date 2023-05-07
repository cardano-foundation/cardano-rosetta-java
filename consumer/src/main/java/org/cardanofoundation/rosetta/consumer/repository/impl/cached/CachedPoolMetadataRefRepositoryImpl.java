package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.PoolHash;
import org.cardanofoundation.rosetta.common.entity.PoolMetadataRef;
import org.cardanofoundation.rosetta.consumer.repository.PoolMetadataRefRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedPoolMetadataRefRepository;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CachedPoolMetadataRefRepositoryImpl implements CachedPoolMetadataRefRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  PoolMetadataRefRepository poolMetadataRefRepository;



  @Override
  public Optional<PoolMetadataRef> findPoolMetadataRefByPoolHashAndUrlAndHash(
      PoolHash poolHash, String url, String hash) {
    return inMemoryCachedEntities.getPoolMetadataRefs().stream()
        .filter(poolMetadataRef -> {
          PoolHash ph = poolMetadataRef.getPoolHash();
          String poolMetadataRefUrl = poolMetadataRef.getUrl();
          String poolMetadataRefHash = poolMetadataRef.getHash();
          return ph.equals(poolHash)
              && poolMetadataRefUrl.equals(url)
              && poolMetadataRefHash.equals(hash);
        })
        .findFirst()
        .or(() -> {
          if (isNew(poolHash)) {
            return Optional.empty();
          }

          return poolMetadataRefRepository
              .findPoolMetadataRefByPoolHashAndUrlAndHash(poolHash, url, hash);
        });
  }

  @Override
  public PoolMetadataRef save(PoolMetadataRef entity) {
    inMemoryCachedEntities.getPoolMetadataRefs().add(entity);
    return entity;
  }

  @Override
  public void flushToDb() {
    poolMetadataRefRepository.saveAll(inMemoryCachedEntities.getPoolMetadataRefs());
    inMemoryCachedEntities.getPoolMetadataRefs().clear();
  }
}
