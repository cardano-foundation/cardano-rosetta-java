package org.cardanofoundation.rosetta.common.services;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.json.JSONObject;
import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.api.block.model.entity.LocalProtocolParamsEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.ProtocolParamsEntity;
import org.cardanofoundation.rosetta.api.block.model.repository.EpochParamRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.LocalProtocolParamsRepository;
import org.cardanofoundation.rosetta.common.mapper.ProtocolParamsMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProtocolParamServiceImpl implements ProtocolParamService {

  @Value("${cardano.rosetta.OFFLINE_MODE}")
  private boolean offlineMode;
  @Value("${cardano.rosetta.GENESIS_SHELLEY_PATH}")
  private String genesisShelleyPath;
  @Value("${cardano.rosetta.GENESIS_ALONZO_PATH}")
  private String genesisAlonzoPath;
  @Value("${cardano.rosetta.GENESIS_CONWAY_PATH}")
  private String genesisConwayPath;

  private final LocalProtocolParamsRepository localProtocolParamsRepository;
  private final ProtocolParamsMapper mapperProtocolParams;
  private final EpochParamRepository epochParamRepository;

  @Override
  @Cacheable(value = "protocolParamsCache")
  @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
  public ProtocolParams findProtocolParameters() {
    if(!offlineMode) {
      log.info("Fetching protocol parameters from the indexer");
      Optional<LocalProtocolParamsEntity> protocolParams = localProtocolParamsRepository.getLocalProtocolParams();
      log.debug("Protocol parameters fetched from the indexer: {} \nand saved in cachedProtocolParams",
              protocolParams);
      if(protocolParams.isEmpty()) {
        ProtocolParamsEntity paramsEntity = epochParamRepository.findLatestProtocolParams();
        return mapperProtocolParams.mapProtocolParamsToEntity(paramsEntity);
      } else {
        return protocolParams.get().getProtocolParams();
      }
    } else {
      return getProtocolParamsFromGenesisFiles();
    }
  }

  private ProtocolParams getProtocolParamsFromGenesisFiles() {

    ProtocolParams protocolParams = new ProtocolParams();
      File genesisShelley = new File(genesisShelleyPath);
      if(genesisShelley.exists()) {
        try {
          InputStream is = new FileInputStream(genesisShelley);
          String jsonTxt = IOUtils.toString(is, StandardCharsets.UTF_8);
          JSONObject shelleyJson = new JSONObject(jsonTxt);

          JSONObject shelleyProtocolParams = (JSONObject) shelleyJson.get("protocolParams");
          protocolParams.setMinFeeA(shelleyProtocolParams.getInt("minFeeA"));
          protocolParams.setMinFeeB(shelleyProtocolParams.getInt("minFeeB"));
          protocolParams.setMaxTxSize(shelleyProtocolParams.getInt("maxTxSize"));
          protocolParams.setKeyDeposit(BigInteger.valueOf(shelleyProtocolParams.getInt("keyDeposit")));
          protocolParams.setPoolDeposit(BigInteger.valueOf(shelleyProtocolParams.getInt("poolDeposit")));
          protocolParams.setMinPoolCost(BigInteger.valueOf(shelleyProtocolParams.getInt("minPoolCost")));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      File alonzoFile = new File(genesisAlonzoPath);
      if (alonzoFile.exists()) {
        try {
          InputStream is = null;

          is = new FileInputStream(alonzoFile);

          String jsonTxt = IOUtils.toString(is, StandardCharsets.UTF_8);
          JSONObject alonzoJson = new JSONObject(jsonTxt);
          protocolParams.setAdaPerUtxoByte(BigInteger.valueOf(alonzoJson.getInt("lovelacePerUTxOWord")));
          protocolParams.setMaxValSize(alonzoJson.getLong("maxValueSize"));
          protocolParams.setMaxCollateralInputs(alonzoJson.getInt("maxCollateralInputs"));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      return protocolParams;
  }

  @Scheduled(fixedRate = 3600000) // 1 hour
  @CacheEvict(value = "protocolParamsCache", allEntries = true)
  public void evictAllCacheValues() {
    log.info("Evicting all entries from protocolParamsCache");
  }
}
