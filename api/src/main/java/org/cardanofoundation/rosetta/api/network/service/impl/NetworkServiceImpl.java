package org.cardanofoundation.rosetta.api.network.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.common.model.Networks;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.json.JSONObject;
import org.openapitools.client.model.*;
import org.openapitools.client.model.Error;

import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.GenesisBlock;
import org.cardanofoundation.rosetta.api.block.model.domain.NetworkStatus;
import org.cardanofoundation.rosetta.api.block.service.LedgerBlockService;
import org.cardanofoundation.rosetta.api.network.service.NetworkService;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.enumeration.OperationTypeStatus;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.exception.ServerException;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.common.model.cardano.network.Producer;
import org.cardanofoundation.rosetta.common.model.cardano.network.PublicRoot;
import org.cardanofoundation.rosetta.common.model.cardano.network.TopologyConfig;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.cardanofoundation.rosetta.common.util.FileUtils;
import org.cardanofoundation.rosetta.common.util.RosettaConstants;
import org.cardanofoundation.rosetta.config.RosettaConfig;

@Service
@Slf4j
@RequiredArgsConstructor
public class NetworkServiceImpl implements NetworkService {

  private final RosettaConfig rosettaConfig;

  private final LedgerBlockService ledgerBlockService;
  private final DataMapper datamapper;

  @Value("${cardano.rosetta.TOPOLOGY_FILEPATH}")
  private String topologyFilepath;
  @Value("${cardano.rosetta.GENESIS_SHELLEY_PATH}")
  private String genesisPath;

  @Value("${cardano.rosetta.CARDANO_NODE_VERSION}")
  private String cardanoNodeVersion;
  private final ResourceLoader resourceLoader;

  @PostConstruct
  public void filePathExistingValidator() throws ServerException {
    validator(topologyFilepath);
    validator(genesisPath);
//    validator(cardanoNodeVersion);
  }

  private void validator( String path) throws ServerException {
    if(!new File(path).exists()) {
      throw ExceptionFactory.configNotFoundException();
    }
  }



  @Override
  public NetworkListResponse getNetworkList(MetadataRequest metadataRequest) {
    log.info("[networkList] Looking for all supported networks");
    Network supportedNetwork = getSupportedNetwork();
    return datamapper.mapToNetworkListResponse(supportedNetwork);
  }

  @Override
  public NetworkOptionsResponse getNetworkOptions(NetworkRequest networkRequest)
      throws IOException {
    log.info("[networkOptions] Looking for networkOptions");
    InputStream openAPIStream = resourceLoader.getResource(
        "classpath:/rosetta-specifications-1.4.15/api.yaml").getInputStream();
    OpenAPI openAPI = new OpenAPIV3Parser().readContents(new String(openAPIStream.readAllBytes()),
            null,
            null)
        .getOpenAPI();
    String rosettaVersion = openAPI.getInfo().getVersion();
    String implementationVersion = rosettaConfig.getImplementationVersion();

    OperationStatus success = new OperationStatus().successful(true)
        .status(OperationTypeStatus.SUCCESS.getValue());
    OperationStatus invalid = new OperationStatus().successful(false)
        .status(OperationTypeStatus.INVALID.getValue());
    List<OperationStatus> operationStatuses = List.of(success, invalid);

    return NetworkOptionsResponse.builder()
        .version(new Version().nodeVersion(cardanoNodeVersion)
            .rosettaVersion(rosettaVersion)
            .middlewareVersion(implementationVersion)
            .metadata(new LinkedHashMap<>()))
        .allow(new Allow().operationStatuses(operationStatuses)
            .operationTypes(
                Arrays.stream(OperationType.values()).map(OperationType::getValue).toList())
            .errors(RosettaConstants.ROSETTA_ERRORS.stream()
                .map(error ->
                    new Error().code(error.getCode())
                        .message(error.getMessage())
                        .retriable(error.isRetriable())
                        .description(error.getDescription())
                        .code(error.getCode())
                )
                .sorted(Comparator.comparingInt(Error::getCode))
                .toList())
            .historicalBalanceLookup(true)
            .callMethods(new ArrayList<>())
//            .balanceExemptions(loadExemptionsFile()) // TODO Removed to get it working clean - add balance exemptions
            .mempoolCoins(false))
        .build();
  }

  @Override
  public NetworkStatusResponse getNetworkStatus(NetworkRequest networkRequest)
      throws  IOException {
    log.debug("[networkStatus] Request received:" + networkRequest.toString());
    log.info("[networkStatus] Looking for latest block");
    NetworkStatus networkStatus = networkStatus();
    return datamapper.mapToNetworkStatusResponse(networkStatus);
  }

  @Override
  public Network getSupportedNetwork() {

    String content = null;
    try {
      content = FileUtils.fileReader(genesisPath);
    } catch (IOException e) {
      throw ExceptionFactory.configNotFoundException();
    }
    JSONObject object = new JSONObject(content);
    Integer networkMagic = (Integer) object.get(Constants.NETWORK_MAGIC_NAME);
    return switch (networkMagic) {
      case Constants.MAINNET_NETWORK_MAGIC -> Networks.mainnet();
      case Constants.PREPROD_NETWORK_MAGIC -> Networks.preprod();
      case Constants.TESTNET_NETWORK_MAGIC -> Networks.testnet();
      case Constants.DEVNET_NETWORK_MAGIC -> new Network(0b0000, Constants.DEVKIT_PROTOCOL_MAGIC);
      default -> throw ExceptionFactory.invalidNetworkError();
    };
  }

  private NetworkStatus networkStatus() throws  IOException {
    log.info("[networkStatus] Looking for latest block");
    Block latestBlock = ledgerBlockService.findLatestBlock();
    log.debug("[networkStatus] Latest block found " + latestBlock);
    log.debug("[networkStatus] Looking for genesis block");
    GenesisBlock genesisBlock = ledgerBlockService.findGenesisBlock();
    log.debug("[networkStatus] Genesis block found " + genesisBlock);

    ObjectMapper mapper = new ObjectMapper();
    String content = FileUtils.fileReader(topologyFilepath);
    TopologyConfig topologyConfig = mapper.readValue(content, TopologyConfig.class);

    return NetworkStatus.builder()
        .latestBlock(latestBlock)
        .genesisBlock(genesisBlock)
        .peers(getPeerFromConfig(topologyConfig))
        .build();
  }

  private List<Peer> getPeerFromConfig(TopologyConfig topologyFile) {
    log.info("[getPeersFromConfig] Looking for peers from topologyFile");
    List<Producer> producers = Optional.ofNullable(topologyFile).map(
            TopologyConfig::getProducers)
        .orElseGet(() -> {
            assert topologyFile != null;
            return getPublicRoots(topologyFile.getPublicRoots());
        });
    log.debug("[getPeersFromConfig] Found " + producers.size() + " peers");
    return producers.stream().map(producer -> new Peer(producer.getAddr(), null)).toList();
  }

  private List<Producer> getPublicRoots(List<PublicRoot> publicRoots) {
    if (publicRoots == null) {
      return new ArrayList<>();
    }
    return publicRoots.stream().flatMap(pr -> pr.getAccessPoints().stream())
        .map(ap -> Producer.builder().addr(ap.getAddress()).build())
        .toList();

  }

  @Override
  public void verifyNetworkRequest(final NetworkIdentifier networkIdentifier) {
    if(!verifyBlockchain(networkIdentifier.getBlockchain())) {
      throw ExceptionFactory.invalidBlockchainError();
    }
    if(!verifyNetwork(networkIdentifier.getNetwork())) {
      throw ExceptionFactory.networkNotFoundError();
    }
  }

  private boolean verifyBlockchain(String blockchain) {
    return blockchain.equals(Constants.CARDANO_BLOCKCHAIN);
  }

  private boolean verifyNetwork(String network) {
    Network supportedNetwork = getSupportedNetwork();

    switch ((int) supportedNetwork.getProtocolMagic()) {
      case Constants.MAINNET_PROTOCOL_MAGIC -> {
        return network.equalsIgnoreCase(Constants.MAINNET);
      }
      case Constants.TESTNET_PROTOCOL_MAGIC -> {
        return network.equalsIgnoreCase(Constants.TESTNET);
      }
      case Constants.PREPROD_PROTOCOL_MAGIC -> {
        return network.equals(Constants.PREPROD);
      }
      case Constants.PREVIEW_PROTOCOL_MAGIC -> {
        return network.equals(Constants.PREVIEW);
      }
      case Constants.DEVKIT_PROTOCOL_MAGIC -> {
        return network.equals(Constants.DEVKIT);
      }
      default -> {
        throw ExceptionFactory.networkNotFoundError();
      }
    }
  }

}
