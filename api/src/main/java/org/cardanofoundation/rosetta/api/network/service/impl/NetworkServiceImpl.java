package org.cardanofoundation.rosetta.api.network.service.impl;

import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.common.model.Networks;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.common.model.cardano.Producer;
import org.cardanofoundation.rosetta.common.model.cardano.PublicRoot;
import org.cardanofoundation.rosetta.common.model.cardano.TopologyConfig;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.enumeration.OperationTypeStatus;
import org.cardanofoundation.rosetta.config.RosettaConfig;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.exception.ServerException;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.api.block.model.domain.NetworkStatus;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.GenesisBlock;
import org.cardanofoundation.rosetta.common.services.LedgerDataProviderService;
import org.cardanofoundation.rosetta.api.network.service.NetworkService;
import org.cardanofoundation.rosetta.common.util.FileUtils;
import org.cardanofoundation.rosetta.common.util.RosettaConstants;
import org.json.JSONObject;
import org.openapitools.client.model.*;
import org.openapitools.client.model.Error;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NetworkServiceImpl implements NetworkService {

  private final RosettaConfig rosettaConfig;

  private final LedgerDataProviderService ledgerDataProviderService;

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
  public NetworkListResponse getNetworkList(MetadataRequest metadataRequest)
      throws IOException {
    log.info("[networkList] Looking for all supported networks");
    Network supportedNetwork = getSupportedNetwork();
    return DataMapper.mapToNetworkListResponse(supportedNetwork);
  }

  @Override
  public NetworkOptionsResponse getNetworkOptions(NetworkRequest networkRequest)
      throws IOException, InterruptedException {
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
    return DataMapper.mapToNetworkStatusResponse(networkStatus);
  }

  @Override
  public Network getSupportedNetwork() throws IOException {

    String content = FileUtils.fileReader(genesisPath);
    JSONObject object = new JSONObject(content);
      String networkId = ((String) object.get("networkId")).toLowerCase();
      Integer networkMagic = (Integer) object.get("networkMagic");

    if (networkId.equals("mainnet")) {
      return Networks.mainnet();
    } else if (Objects.equals(networkMagic, Constants.PREPROD_NETWORK_MAGIC)) {
      return Networks.preprod();
    } else if (Objects.equals(networkMagic, Constants.TESTNET_NETWORK_MAGIC)) {
      return Networks.testnet();
    } else if(Objects.equals(networkMagic, Constants.DEVNET_NETWORK_MAGIC)) {
      return new Network(0b0000, 42);
    } else {
      throw ExceptionFactory.invalidNetworkError();
    }
  }

  private NetworkStatus networkStatus() throws  IOException {
    log.info("[networkStatus] Looking for latest block");
    Block latestBlock = ledgerDataProviderService.findLatestBlock();
    log.debug("[networkStatus] Latest block found " + latestBlock);
    log.debug("[networkStatus] Looking for genesis block");
    GenesisBlock genesisBlock = ledgerDataProviderService.findGenesisBlock();
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

}
