package org.cardanofoundation.rosetta.common.services.impl;

import java.io.IOException;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.api.block.model.entity.ProtocolParamsEntity;
import org.cardanofoundation.rosetta.api.block.model.repository.EpochParamRepository;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.mapper.ProtocolParamsToEntity;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;
import org.cardanofoundation.rosetta.common.util.FileUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProtocolParamServiceImpl implements ProtocolParamService {

  @Value("${cardano.rosetta.GENESIS_SHELLEY_PATH}")
  private String genesisShelleyPath;

  private final ObjectMapper objectMapper;
  private final EpochParamRepository epochParamRepository;
  private final ProtocolParamsToEntity mapperProtocolParams;


  @Override
  public ProtocolParams getProtocolParameters() {
    try {
      String shelleyContent = FileUtils.fileReader(genesisShelleyPath);
      JSONObject shelleyJsonObject = new JSONObject(shelleyContent);
      return fromJSONObject(shelleyJsonObject);
    } catch (IOException e) {
      log.error("Error reading genesis shelley file: {}", genesisShelleyPath);
      throw ExceptionFactory.configNotFoundException();
    }
  }

  private ProtocolParams fromJSONObject(JSONObject shelleyJsonObject)
      throws JsonProcessingException {
    var params = shelleyJsonObject.getJSONObject("protocolParams");
    String s = Optional.ofNullable(params).map(JSONObject::toString).orElse("{}");
    return objectMapper.readValue(s, ProtocolParams.class);
  }

  @Override
  public ProtocolParams findProtocolParametersFromIndexerAndConfig() {
    ProtocolParamsEntity paramsEntity = epochParamRepository.findLatestProtocolParams();
    ProtocolParams protocolParams = mapperProtocolParams.fromEntity(paramsEntity);
    ProtocolParams protocolParametersFromConfigFile = getProtocolParameters();
    return mapperProtocolParams.merge(protocolParams, protocolParametersFromConfigFile);
  }
}
