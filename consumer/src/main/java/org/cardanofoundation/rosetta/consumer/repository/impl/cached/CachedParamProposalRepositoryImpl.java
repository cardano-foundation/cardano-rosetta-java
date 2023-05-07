package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import org.cardanofoundation.rosetta.common.entity.ParamProposal;
import org.cardanofoundation.rosetta.consumer.repository.ParamProposalRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedParamProposalRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CachedParamProposalRepositoryImpl implements CachedParamProposalRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  ParamProposalRepository paramProposalRepository;

  @Override
  public List<ParamProposal> saveAll(Collection<ParamProposal> entities) {
    inMemoryCachedEntities.getParamProposals().addAll(entities);
    return new ArrayList<>(entities);
  }

  @Override
  public void flushToDb() {
    paramProposalRepository.saveAll(inMemoryCachedEntities.getParamProposals());
    inMemoryCachedEntities.getParamProposals().clear();
  }

  @Override
  public List<ParamProposal> findParamProposalEpochNo(int epochNo) {
    List<ParamProposal> paramProposalsCache = inMemoryCachedEntities.getParamProposals().stream()
        .filter(paramProposal -> paramProposal.getEpochNo() == epochNo)
        .collect(Collectors.toList());

    if(paramProposalsCache.isEmpty()) {
      paramProposalsCache =  paramProposalRepository.findParamProposalsByEpochNo(epochNo);
    }

    return paramProposalsCache;
  }
}
