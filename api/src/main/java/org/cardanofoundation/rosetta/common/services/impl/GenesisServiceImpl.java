package org.cardanofoundation.rosetta.common.services.impl;

import java.io.IOException;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.services.GenesisService;
import org.cardanofoundation.rosetta.common.util.FileUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenesisServiceImpl implements GenesisService {

  @Value("${cardano.rosetta.GENESIS_SHELLEY_PATH}")
  private String genesisShelleyPath;

  private final ObjectMapper objectMapper;


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

  private ProtocolParams fromJSONObject(JSONObject shelleyJsonObject) {

    var params = shelleyJsonObject.getJSONObject("protocolParams");

    try {
      String s = Optional.ofNullable(params).map(JSONObject::toString).orElse("{}");
      return objectMapper.readValue(s, ProtocolParams.class);
    } catch (Exception e) {
      throw new ApiException("Error parsing protocol parameters", e);
    }

  }

}
