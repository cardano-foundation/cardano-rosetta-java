package org.cardanofoundation.rosetta.common.services;

import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

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

  private ProtocolParams cachedProtocolParams;

  @PostConstruct
  public void init() {
    cachedProtocolParams = findProtocolParametersFromIndexer();
  }

  @Override
  public ProtocolParams getProtocolParameters() {
    return cachedProtocolParams;
  }

  @Override
  public ProtocolParams findProtocolParametersFromIndexer() {
    ProtocolParamsEntity paramsEntity = epochParamRepository.findLatestProtocolParams();
    return mapperProtocolParams.fromEntity(paramsEntity);
  }
}
