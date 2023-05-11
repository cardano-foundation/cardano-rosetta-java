package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import static org.cardanofoundation.rosetta.consumer.constant.ConsumerConstant.BATCH_QUERY_SIZE;

import com.google.common.collect.Lists;
import org.cardanofoundation.rosetta.common.entity.ExtraKeyWitness;
import org.cardanofoundation.rosetta.consumer.repository.ExtraKeyWitnessRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedExtraKeyWitnessRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CachedExtraKeyWitnessRepositoryImpl implements CachedExtraKeyWitnessRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  ExtraKeyWitnessRepository extraKeyWitnessRepository;

  @Override
  public Set<String> findByHashIn(Set<String> hashes) {
    Set<String> nonExistentHash = new HashSet<>();
    Set<String> existingHash = new HashSet<>();

    hashes.forEach(extraKeyWitnessHash -> {
      if (inMemoryCachedEntities.getExtraKeyWitnessMap().containsKey(extraKeyWitnessHash)) {
        existingHash.add(extraKeyWitnessHash);
      } else {
        nonExistentHash.add(extraKeyWitnessHash);
      }
    });

    int cacheHit = existingHash.size();
    long startTime = System.currentTimeMillis();

    var queryBatches = Lists.partition(new ArrayList<>(nonExistentHash), BATCH_QUERY_SIZE);
    queryBatches.forEach(batch ->
        existingHash.addAll(extraKeyWitnessRepository.findByHashIn(batch)));

    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    log.trace("Find extra key witness: total {}, cache hit {}, query {}, time {} second(s)", hashes.size(),
        cacheHit, nonExistentHash.size(), totalTime / 1000f);
    return existingHash;
  }

  @Override
  public ExtraKeyWitness save(ExtraKeyWitness entity) {
    inMemoryCachedEntities.getExtraKeyWitnessMap().putIfAbsent(entity.getHash(), entity);
    return entity;
  }

  @Override
  public List<ExtraKeyWitness> saveAll(Collection<ExtraKeyWitness> entities) {
    entities.forEach(extraKeyWitness ->
        inMemoryCachedEntities.getExtraKeyWitnessMap()
            .putIfAbsent(extraKeyWitness.getHash(), extraKeyWitness));
    return new ArrayList<>(entities);
  }

  @Override
  public void flushToDb() {
    extraKeyWitnessRepository.saveAll(inMemoryCachedEntities.getExtraKeyWitnessMap().values());
    inMemoryCachedEntities.getExtraKeyWitnessMap().clear();
  }
}
