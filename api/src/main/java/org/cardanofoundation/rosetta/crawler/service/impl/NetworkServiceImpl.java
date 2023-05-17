package org.cardanofoundation.rosetta.crawler.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.swagger.v3.core.util.Json;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.crawler.common.constants.Constants;
import org.cardanofoundation.rosetta.crawler.config.NetworkConfig;
import org.cardanofoundation.rosetta.crawler.config.RosettaConfig;
import org.cardanofoundation.rosetta.crawler.event.AppEvent;
import org.cardanofoundation.rosetta.crawler.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.crawler.exception.ServerException;
import org.cardanofoundation.rosetta.crawler.mapper.DataMapper;
import org.cardanofoundation.rosetta.crawler.model.Network;
import org.cardanofoundation.rosetta.crawler.model.NetworkStatus;
import org.cardanofoundation.rosetta.crawler.model.Peer;
import org.cardanofoundation.rosetta.crawler.model.Producer;
import org.cardanofoundation.rosetta.crawler.model.PublicRoot;
import org.cardanofoundation.rosetta.crawler.model.TopologyConfig;
import org.cardanofoundation.rosetta.crawler.model.Version;
import org.cardanofoundation.rosetta.crawler.model.rest.BalanceExemption;
import org.cardanofoundation.rosetta.crawler.model.rest.MetadataRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkIdentifier;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkListResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkOptionsResponse;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.NetworkStatusResponse;
import org.cardanofoundation.rosetta.crawler.projection.BlockDto;
import org.cardanofoundation.rosetta.crawler.projection.GenesisBlockDto;
import org.cardanofoundation.rosetta.crawler.service.BlockService;
import org.cardanofoundation.rosetta.crawler.service.NetworkService;
import org.cardanofoundation.rosetta.crawler.util.RosettaConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NetworkServiceImpl implements NetworkService {
  @Autowired
  private RosettaConfig rosettaConfig;

  @Autowired
  private BlockService blockService;

  @Value("${cardano.rosetta.TOPOLOGY_FILEPATH}")
  private String topologyFilepath;


  private List<BalanceExemption> balanceExemptions;

  @PostConstruct
  void loadExemptionsFile() throws IOException {
    if (rosettaConfig.getExemptionsFile() != null) {
      final ObjectMapper objectMapper = new ObjectMapper();
      balanceExemptions = objectMapper.readValue(
          new File(rosettaConfig.getExemptionsFile()),
          new TypeReference<List<BalanceExemption>>() {
          });
    } else {
      balanceExemptions = List.of();
    }
  }

  @Override
  public NetworkListResponse getNetworkList(MetadataRequest metadataRequest) {
    log.info("[networkList] Looking for all supported networks");
    NetworkListResponse networkListResponse = new NetworkListResponse();
    rosettaConfig.getNetworks().forEach((networkConfig -> {
      NetworkIdentifier identifier = new NetworkIdentifier();
      identifier.setBlockchain(RosettaConstants.BLOCKCHAIN_NAME);
      identifier.setNetwork(networkConfig.getSanitizedNetworkId());
      networkListResponse.addNetworkIdentifiersItem(identifier);
    }));
    return networkListResponse;
  }

  @Override
  public NetworkOptionsResponse getNetworkOptions(NetworkRequest networkRequest) {
    NetworkConfig requestedNetworkConfig = rosettaConfig.networkConfigFromNetworkRequest(networkRequest).orElseThrow();
    NetworkOptionsResponse networkOptionsResponse = new NetworkOptionsResponse();
    log.info("[networkOptions] Looking for networkOptions");


    Version version = new Version();
    version.setRosettaVersion(rosettaConfig.getVersion());
    version.setMiddlewareVersion(rosettaConfig.getImplementationVersion());
    version.setNodeVersion(requestedNetworkConfig.getNodeVersion());
//    networkOptionsResponse.setVersion(version);
//
//        final Allow allow = new Allow();
//        allow.setOperationStatuses(RosettaConstants.ROSETTA_OPERATION_STATUSES);
//        allow.setOperationTypes(RosettaConstants.ROSETTA_OPERATION_TYPES);
//        allow.setErrors(RosettaConstants.ROSETTA_ERRORS);
//        allow.setHistoricalBalanceLookup(true);
//        allow.setCallMethods(List.of());
//        allow.setMempoolCoins(false);
//        allow.setBalanceExemptions(balanceExemptions);
//        networkOptionsResponse.setAllow(allow);
    return networkOptionsResponse;
  }

  @Override
  public NetworkStatusResponse getNetworkStatus(NetworkRequest networkRequest)
      throws ServerException {
    log.debug("[networkStatus] Request received:" + networkRequest.toString());
    log.info("[networkStatus] Looking for latest block");
    NetworkStatus networkStatus = networkStatus();
    return DataMapper.mapToNetworkStatusResponse(networkStatus);
  }

  @Override
  public Network getSupportedNetwork() {
    if(AppEvent.networkId.equals("mainnet")){
      return Network.builder().networkId(AppEvent.networkId).build();
    } else if (AppEvent.networkMagic.equals(BigInteger.valueOf(Constants.PREPROD_NETWORK_MAGIC))) {
      return Network.builder().networkId("preprod").build();
    } else if (AppEvent.networkMagic.equals(BigInteger.valueOf(Constants.PREVIEW_NETWORK_MAGIC))) {
      return Network.builder().networkId("preprod").build();
    }
    return null;
  }

  private NetworkStatus networkStatus() throws ServerException {
    log.info("[networkStatus] Looking for latest block");
    BlockDto latestBlock = blockService.getLatestBlock();
    log.debug("[networkStatus] Latest block found " + latestBlock);
    log.debug("[networkStatus] Looking for genesis block");
    GenesisBlockDto genesisBlock = blockService.getGenesisBlock();
    log.debug("[networkStatus] Genesis block found " + genesisBlock);
    return NetworkStatus.builder()
        .latestBlock(latestBlock)
        .genesisBlock(genesisBlock)
        .peers(getPeerFromConfig(readFromFileConfig(topologyFilepath)))
        .build();
  }

  private List<Peer> getPeerFromConfig(TopologyConfig topologyFile) {
    log.info("[getPeersFromConfig] Looking for peers from topologyFile");
    List<Producer> producers = Optional.ofNullable(topologyFile).map(
            TopologyConfig::getProducers)
        .orElseGet(() -> getPublicRoots(topologyFile.getPublicRoots()));
    log.debug("[getPeersFromConfig] Found " + producers.size() + " peers");
    return producers.stream().map(producer -> new Peer(producer.getAddr())).collect(Collectors.toList());
  }

  private List<Producer> getPublicRoots(List<PublicRoot> publicRoots) {
    if (publicRoots == null) {
      return new ArrayList<>();
    }
    return publicRoots.stream().flatMap(pr -> pr.getAccessPoints().stream())
        .map(ap -> Producer.builder().addr(ap.getAddress()).build())
        .collect(Collectors.toList());

  }

  private TopologyConfig readFromFileConfig(String urlPath) throws ServerException {
    try {
      ObjectMapper  mapper = new ObjectMapper();
      File topologyFile = new File(urlPath);
      return mapper.readValue(topologyFile,TopologyConfig.class);

    } catch (IOException e) {
      throw ExceptionFactory.configNotFoundException();
    }
  }
}
