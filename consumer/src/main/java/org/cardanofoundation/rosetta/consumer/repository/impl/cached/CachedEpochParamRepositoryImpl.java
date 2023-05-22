package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.EpochParam;
import org.cardanofoundation.rosetta.consumer.repository.EpochParamRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedEpochParamRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
public class CachedEpochParamRepositoryImpl implements CachedEpochParamRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  EpochParamRepository epochParamRepository;

  @Override
  public EpochParam save(EpochParam entity) {
    inMemoryCachedEntities.getEpochParams().add(entity);
    return entity;
  }

  @Override
  public List<EpochParam> saveAll(Collection<EpochParam> entities) {
    inMemoryCachedEntities.getEpochParams().addAll(entities);
    return new ArrayList<>(entities);
  }

  @Override
  public void flushToDb() {
    long startTime = System.currentTimeMillis();
    var epochParams = inMemoryCachedEntities.getEpochParams();
    var epochParamsSize = epochParams.size();
    epochParamRepository.saveAll(epochParams);
    inMemoryCachedEntities.getEpochParams().clear();
    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    log.debug("EpochParam {} elapsed: {} ms, {} second(s)", epochParamsSize, totalTime,
        totalTime / 1000f);
  }

  @Override
  public Integer findLastEpochParam() {
    List<EpochParam> epochParams = inMemoryCachedEntities.getEpochParams();
    int epochParamsSize = epochParams.size();
    return Optional.ofNullable(epochParamsSize > 0 ? epochParams.get(epochParamsSize - 1) : null)
        .or(epochParamRepository::findLastEpochParam)
        .map(EpochParam::getEpochNo)
        .orElse(0);
  }

  @Override
  public Optional<EpochParam> findPrevEpochParamByEpochNo(int epochNo) {
    List<EpochParam> epochParams = inMemoryCachedEntities.getEpochParams();
    return epochParams.stream().filter(epochParam -> epochParam.getEpochNo() == epochNo - 1)
        .findFirst()
        .or(() -> epochParamRepository.findEpochParamByEpochNo(epochNo - 1));
  }
}
