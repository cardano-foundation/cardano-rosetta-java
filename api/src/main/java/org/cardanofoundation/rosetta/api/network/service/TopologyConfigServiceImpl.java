package org.cardanofoundation.rosetta.api.network.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openapitools.client.model.Peer;

import org.cardanofoundation.rosetta.api.network.model.Producer;
import org.cardanofoundation.rosetta.api.network.model.PublicRoot;
import org.cardanofoundation.rosetta.api.network.model.TopologyConfig;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.util.FileUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class TopologyConfigServiceImpl implements TopologyConfigService {

  @Value("${cardano.rosetta.TOPOLOGY_FILEPATH}")
  private String topologyFilepath;
  private List<Peer> cachedPeers;

  @PostConstruct
  public void init() {
    TopologyConfig topologyConfig = loadTopologyConfig();
    cachedPeers = getPeerFromConfig(topologyConfig);
    log.info("Peer loaded from topology config json");
  }

  @Override
  public List<Peer> getPeers() {
    return cachedPeers;
  }

  public List<Peer> getPeerFromConfig(TopologyConfig topologyConfig) {
    log.info("[getPeersFromConfig] Looking for peers from topologyFile");

    List<Producer> producers = Optional.ofNullable(topologyConfig)
        .map(TopologyConfig::getProducers)
        .orElseGet(() -> {
          assert topologyConfig != null;
          return getPublicRoots(topologyConfig.getPublicRoots());
        });
    log.debug("[getPeersFromConfig] Found {} peers", producers.size());
    return producers.stream().map(producer -> new Peer(producer.getAddr(), null)).toList();
  }

  private List<Producer> getPublicRoots(List<PublicRoot> publicRoots) {
    if (publicRoots == null) {
      return new ArrayList<>();
    }
    return publicRoots.stream()
        .flatMap(pr -> pr.getAccessPoints().stream())
        .map(ap -> Producer.builder().addr(ap.getAddress()).build())
        .toList();
  }

  private TopologyConfig loadTopologyConfig() {
    try {
      ObjectMapper mapper = new ObjectMapper();
      String content = FileUtils.fileReader(topologyFilepath);
      return mapper.readValue(content, TopologyConfig.class);
    } catch (IOException e) {
      throw ExceptionFactory.configNotFoundException(topologyFilepath);
    }
  }

}
