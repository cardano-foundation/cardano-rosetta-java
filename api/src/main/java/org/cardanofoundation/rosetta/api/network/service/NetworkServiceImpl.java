package org.cardanofoundation.rosetta.api.network.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.common.model.Networks;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.json.JSONObject;
import org.openapitools.client.model.Allow;
import org.openapitools.client.model.Error;
import org.openapitools.client.model.MetadataRequest;
import org.openapitools.client.model.NetworkIdentifier;
import org.openapitools.client.model.NetworkListResponse;
import org.openapitools.client.model.NetworkOptionsResponse;
import org.openapitools.client.model.NetworkRequest;
import org.openapitools.client.model.NetworkStatusResponse;
import org.openapitools.client.model.OperationStatus;
import org.openapitools.client.model.Peer;
import org.openapitools.client.model.Version;

import org.cardanofoundation.rosetta.api.block.model.domain.BlockIdentifierExtended;
import org.cardanofoundation.rosetta.api.block.model.domain.NetworkStatus;
import org.cardanofoundation.rosetta.api.block.service.LedgerBlockService;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.enumeration.OperationTypeStatus;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
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
  private final TopologyConfigService topologyConfigService;
  private final ResourceLoader resourceLoader;

  private final Object lock = new Object();
  private Integer cachedMagicNumber;

  @Value("${cardano.rosetta.GENESIS_SHELLEY_PATH}")
  private String genesisShelleyPath;
  @Value("${cardano.rosetta.CARDANO_NODE_VERSION}")
  private String cardanoNodeVersion;

  @Override
  public NetworkListResponse getNetworkList(MetadataRequest metadataRequest) {
    log.info("[networkList] Looking for all supported networks");
    Network supportedNetwork = getSupportedNetwork();
    return datamapper.mapToNetworkListResponse(supportedNetwork);
  }

  @Override
  public NetworkOptionsResponse getNetworkOptions(NetworkRequest networkRequest) {
    log.info("[networkOptions] Looking for networkOptions");

    String rosettaVersion;
    String implementationVersion;
    try {
      InputStream openAPIStream = resourceLoader.getResource(
          "classpath:/rosetta-specifications-1.4.15/api.yaml").getInputStream();
      OpenAPI openAPI = new OpenAPIV3Parser().readContents(new String(openAPIStream.readAllBytes()),
              null,
              null)
          .getOpenAPI();
      rosettaVersion = openAPI.getInfo().getVersion();
      implementationVersion = rosettaConfig.getImplementationVersion();
    } catch (IOException e) {
      throw ExceptionFactory.configNotFoundException();
    }

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
            .mempoolCoins(false))
        .build();
  }

  @Override
  public NetworkStatusResponse getNetworkStatus(NetworkRequest networkRequest) {
    log.debug("[networkStatus] Request received:" + networkRequest.toString());
    log.info("[networkStatus] Looking for latest block");
    NetworkStatus networkStatus = networkStatus();
    return datamapper.mapToNetworkStatusResponse(networkStatus);
  }

  @Override
  public Network getSupportedNetwork() {
    Integer networkMagic = getNetworkMagic();
    return switch (networkMagic) {
      case Constants.MAINNET_NETWORK_MAGIC -> Networks.mainnet();
      case Constants.PREPROD_NETWORK_MAGIC -> Networks.preprod();
      case Constants.TESTNET_NETWORK_MAGIC -> Networks.testnet();
      case Constants.DEVNET_NETWORK_MAGIC -> new Network(0b0000, Constants.DEVKIT_PROTOCOL_MAGIC);
      default -> throw ExceptionFactory.invalidNetworkError();
    };
  }

  private NetworkStatus networkStatus() {
    log.info("[networkStatus] Looking for latest block");
    BlockIdentifierExtended latestBlock = ledgerBlockService.findLatestBlockIdentifier();
    log.debug("[networkStatus] Latest block found " + latestBlock);

    log.debug("[networkStatus] Looking for genesis block");
    BlockIdentifierExtended genesisBlock = ledgerBlockService.findGenesisBlockIdentifier();
    log.debug("[networkStatus] Genesis block found " + genesisBlock);

    List<Peer> peers = topologyConfigService.getPeers();

    return NetworkStatus.builder()
        .latestBlock(latestBlock)
        .genesisBlock(genesisBlock)
        .peers(peers)
        .build();
  }

  @Override
  public void verifyNetworkRequest(final NetworkIdentifier networkIdentifier) {
    if (networkIdentifier != null) {
      if (!verifyBlockchain(networkIdentifier.getBlockchain())) {
        throw ExceptionFactory.invalidBlockchainError();
      }
      if (!verifyNetwork(networkIdentifier.getNetwork())) {
        throw ExceptionFactory.networkNotFoundError();
      }
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
        return false;
      }
    }
  }

  public Integer getNetworkMagic() {
    Integer magicNumber = cachedMagicNumber;
    if (magicNumber == null) {
      synchronized (lock) {
        magicNumber = cachedMagicNumber;
        if (magicNumber == null) {
          JSONObject json = loadGenesisShelleyConfig();
          cachedMagicNumber = magicNumber = (Integer) json.get(Constants.NETWORK_MAGIC_NAME);
        }
      }
    }
    return magicNumber;
  }

  private JSONObject loadGenesisShelleyConfig() {
    try {
      String content = FileUtils.fileReader(genesisShelleyPath);
      return new JSONObject(content);
    } catch (IOException e) {
      throw ExceptionFactory.configNotFoundException();
    }
  }

}
