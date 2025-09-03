package org.cardanofoundation.rosetta.api.network.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.network.model.BootstrapPeer;
import org.cardanofoundation.rosetta.api.network.model.TopologyConfig;
import org.cardanofoundation.rosetta.client.YaciHttpGateway;
import org.cardanofoundation.rosetta.client.model.domain.DiscoveredPeer;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.util.FileUtils;
import org.openapitools.client.model.Peer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TopologyConfigServiceImpl implements TopologyConfigService {

  private final YaciHttpGateway yaciHttpGateway;

  @Value("${cardano.rosetta.TOPOLOGY_FILEPATH}")
  private String topologyFilepath;

  private List<Peer> cachedPeers;

  @PostConstruct
  public void init() {
    TopologyConfig topologyConfig = loadTopologyConfig();
    cachedPeers = getPeerFromConfig(topologyConfig);
    log.info("Peer loaded from topology config json, cachedPeers_size:" + cachedPeers.size());
  }

  @Override
  public List<Peer> getPeers() {
    List<Peer> discoveredPeers = getDiscoveredPeers();

    if (discoveredPeers.isEmpty()) {
      return getStaticPeers();
    }

    return discoveredPeers;
  }

  @Override
  public List<Peer> getStaticPeers() {
    return cachedPeers;
  }

  public List<Peer> getPeerFromConfig(TopologyConfig topologyConfig) {
    log.info("[getPeersFromConfig] Looking for bootstrap peers from topologyFile");

    if (topologyConfig == null) {
      log.warn("Topology config is null");
      return new ArrayList<>();
    }

    // Only read bootstrap peers now
    List<BootstrapPeer> bootstrapPeers = topologyConfig.getBootstrapPeers();
    
    if (bootstrapPeers == null || bootstrapPeers.isEmpty()) {
      log.warn("No bootstrap peers found in topology config");
      return new ArrayList<>();
    }

    log.debug("[getPeersFromConfig] Found {} bootstrap peers", bootstrapPeers.size());

    return bootstrapPeers.stream()
        .map(this::mapBootstrapPeerToPeer)
        .toList();
  }

  private Peer mapBootstrapPeerToPeer(BootstrapPeer bootstrapPeer) {
    String peerAddress = bootstrapPeer.getAddress();
    peerAddress = "%s:%d".formatted(peerAddress, bootstrapPeer.getPort());

    return new Peer(peerAddress, Map.of("type", "IPv4"));
  }

  @Override
  public List<Peer> getDiscoveredPeers() {
    log.info("[getDiscoveredPeers] Fetching discovered peers from yaci-indexer");

    try {
      List<DiscoveredPeer> discoveredPeers = yaciHttpGateway.getDiscoveredPeers();
      log.debug("[getDiscoveredPeers] Found {} discovered peers", discoveredPeers.size());
      
      return discoveredPeers.stream()
              .map(this::mapDiscoveredPeerToPeer)
              .toList();

    } catch (Exception e) {
      log.warn("[getDiscoveredPeers] Failed to fetch discovered peers!", e);

      return List.of();
    }
  }

  private Peer mapDiscoveredPeerToPeer(DiscoveredPeer discoveredPeer) {
    String peerAddress = discoveredPeer.getAddress();
    peerAddress = "%s:%d".formatted(peerAddress, discoveredPeer.getPort());

    String peerType = discoveredPeer.getType();

    return new Peer(peerAddress, Map.of("type", peerType));
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