package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import static org.cardanofoundation.rosetta.consumer.constant.ConsumerConstant.BATCH_QUERY_SIZE;

import com.google.common.collect.Lists;
import org.cardanofoundation.rosetta.common.entity.StakeAddress;
import org.cardanofoundation.rosetta.consumer.repository.StakeAddressRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedStakeAddressRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Slf4j
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CachedStakeAddressRepositoryImpl implements CachedStakeAddressRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  StakeAddressRepository stakeAddressRepository;


  @Override
  public Optional<StakeAddress> findByHashRaw(String hashRaw) {
    if (!StringUtils.hasText(hashRaw)) {
      return Optional.empty();
    }

    return findByHashRawCacheOnly(hashRaw)
        .or(() -> stakeAddressRepository.findByHashRaw(hashRaw));
  }

  private Optional<StakeAddress> findByHashRawCacheOnly(String hashRaw) {
    return Optional.ofNullable(inMemoryCachedEntities.getStakeAddressMap().get(hashRaw));
  }

  @Override
  public Collection<StakeAddress> findByHashRawIn(Collection<String> hashes) {
    Collection<StakeAddress> stakeAddressCollection = new ConcurrentLinkedQueue<>();
    Set<String> nonExistentHash = new ConcurrentSkipListSet<>();

    hashes.parallelStream().forEach(stakeAddressHex -> {
      Optional<StakeAddress> stakeAddress = findByHashRawCacheOnly(stakeAddressHex);
      if (stakeAddress.isEmpty()) {
        nonExistentHash.add(stakeAddressHex);
        return;
      }

      stakeAddressCollection.add(stakeAddress.get());
    });

    int cacheHit = stakeAddressCollection.size();
    long startTime = System.currentTimeMillis();

    var queryBatches = Lists.partition(new ArrayList<>(nonExistentHash), BATCH_QUERY_SIZE);
    queryBatches.parallelStream().forEach(batch -> {
      List<StakeAddress> stakeAddresses = stakeAddressRepository.findByHashRawIn(batch);
      stakeAddresses.parallelStream().forEach(stakeAddressCollection::add);
    });

    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    log.trace("Find stake address: total {}, cache hit {}, query {}, time {} second(s)", hashes.size(),
        cacheHit, nonExistentHash.size(), totalTime / 1000f);

    return stakeAddressCollection;
  }

  @Override
  public List<StakeAddress> saveAll(Collection<StakeAddress> entities) {
    entities.forEach(stakeAddress -> inMemoryCachedEntities.getStakeAddressMap()
        .putIfAbsent(stakeAddress.getHashRaw(), stakeAddress));

    return new ArrayList<>(entities);
  }

  @Override
  public void flushToDb() {
    stakeAddressRepository.saveAll(inMemoryCachedEntities.getStakeAddressMap().values());
    inMemoryCachedEntities.getStakeAddressMap().clear();
  }
}
