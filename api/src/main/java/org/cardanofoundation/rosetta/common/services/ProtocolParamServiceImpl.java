package org.cardanofoundation.rosetta.common.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.cardanofoundation.rosetta.api.block.model.entity.LocalProtocolParamsEntity;
import org.cardanofoundation.rosetta.api.block.model.repository.LocalProtocolParamsRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.api.block.model.entity.ProtocolParamsEntity;
import org.cardanofoundation.rosetta.api.block.model.repository.EpochParamRepository;
import org.cardanofoundation.rosetta.common.mapper.ProtocolParamsMapper;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProtocolParamServiceImpl implements ProtocolParamService {

  private final LocalProtocolParamsRepository localProtocolParamsRepository;
  private final ProtocolParamsMapper mapperProtocolParams;

  @Override
  @Cacheable(value = "protocolParamsCache")
  @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
  public ProtocolParams findProtocolParametersFromIndexer() {
    log.info("Fetching protocol parameters from the indexer");
    Optional<LocalProtocolParamsEntity> protocolParams = localProtocolParamsRepository.getLocalProtocolParams();
    log.debug("Protocol parameters fetched from the indexer: {} \nand saved in cachedProtocolParams",
            protocolParams);

    return protocolParams.orElse(new LocalProtocolParamsEntity()).getProtocolParams();
  }

  @Scheduled(fixedRate = 3600000) // 1 hour
  @CacheEvict(value = "protocolParamsCache", allEntries = true)
  public void evictAllCacheValues() {
    log.info("Evicting all entries from protocolParamsCache");
  }
}
