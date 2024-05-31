package org.cardanofoundation.rosetta.common.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.api.block.model.entity.ProtocolParamsEntity;
import org.cardanofoundation.rosetta.api.block.model.repository.EpochParamRepository;
import org.cardanofoundation.rosetta.common.mapper.ProtocolParamsToEntity;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProtocolParamServiceImpl implements ProtocolParamService {

  private final EpochParamRepository epochParamRepository;
  private final ProtocolParamsToEntity mapperProtocolParams;

  @Override
  @Cacheable(value = "protocolParamsCache")
  @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
  public ProtocolParams findProtocolParametersFromIndexer() {
    log.info("Fetching protocol parameters from the indexer");
    ProtocolParamsEntity paramsEntity = epochParamRepository.findLatestProtocolParams();
    ProtocolParams protocolParams = mapperProtocolParams.fromEntity(paramsEntity);
    log.debug("Protocol parameters fetched from the indexer: {} \nand saved in cachedProtocolParams",
        paramsEntity);
    return protocolParams;
  }

  @Scheduled(fixedRate = 3600000) // 1 hour
  @CacheEvict(value = "protocolParamsCache", allEntries = true)
  public void evictAllCacheValues() {
    log.info("Evicting all entries from protocolParamsCache");
  }
}
