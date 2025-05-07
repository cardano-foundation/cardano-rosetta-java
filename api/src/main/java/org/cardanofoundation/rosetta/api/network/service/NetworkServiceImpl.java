package org.cardanofoundation.rosetta.api.network.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.common.model.Networks;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.openapitools.client.model.*;
import org.openapitools.client.model.Error;

import org.cardanofoundation.rosetta.api.block.model.domain.BlockIdentifierExtended;
import org.cardanofoundation.rosetta.api.block.model.domain.NetworkStatus;
import org.cardanofoundation.rosetta.api.block.service.LedgerBlockService;
import org.cardanofoundation.rosetta.api.network.mapper.NetworkMapper;
import org.cardanofoundation.rosetta.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.common.enumeration.OperationTypeStatus;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.time.OfflineSlotService;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.cardanofoundation.rosetta.common.util.RosettaConstants;

import static org.cardanofoundation.rosetta.common.util.Constants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class NetworkServiceImpl implements NetworkService {

  private final LedgerBlockService ledgerBlockService;
  private final OfflineSlotService offlineSlotService;
  private final NetworkMapper networkMapper;
  private final TopologyConfigService topologyConfigService;
  private final ResourceLoader resourceLoader;
  private final GenesisDataProvider genesisDataProvider;
  private final SlotRangeChecker slotRangeChecker;

  @Value("${cardano.rosetta.GENESIS_SHELLEY_PATH}")
  private String genesisShelleyPath;

  @Value("${cardano.rosetta.CARDANO_NODE_VERSION}")
  private String cardanoNodeVersion;

  @Value("${cardano.rosetta.middleware-version}")
  private String revision;

  @Value("${cardano.rosetta.ALLOWED_SLOTS_DELTA:100}")
  private int allowedSlotsDelta;

  @Override
  public NetworkListResponse getNetworkList(MetadataRequest metadataRequest) {
    log.info("[networkList] Looking for all supported networks");
    Network supportedNetwork = getSupportedNetwork();

    return networkMapper.toNetworkListResponse(supportedNetwork);
  }

  @Override
  public NetworkOptionsResponse getNetworkOptions(NetworkRequest networkRequest) {
    log.info("[networkOptions] Looking for networkOptions");

    String rosettaVersion = getRosettaVersion();
    OperationStatus success = new OperationStatus().successful(true)
            .status(OperationTypeStatus.SUCCESS.getValue());
    OperationStatus invalid = new OperationStatus().successful(false)
            .status(OperationTypeStatus.INVALID.getValue());
    List<OperationStatus> operationStatuses = List.of(success, invalid);

    return NetworkOptionsResponse.builder()
            .version(new Version().nodeVersion(cardanoNodeVersion)
                    .rosettaVersion(rosettaVersion)
                    .middlewareVersion(revision)
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

  private String getRosettaVersion() {
    try {
      InputStream openAPIStream = resourceLoader.getResource(
              Constants.ROSETTA_API_PATH).getInputStream();
      OpenAPI openAPI = new OpenAPIV3Parser().readContents(new String(openAPIStream.readAllBytes()),
                      null, null)
              .getOpenAPI();
      return openAPI.getInfo().getVersion();
    } catch (IOException e) {
      throw ExceptionFactory.configNotFoundException(Constants.ROSETTA_API_PATH);
    }
  }

  @Override
  public NetworkStatusResponse getNetworkStatus(NetworkRequest networkRequest) {
    log.debug("[networkStatus] Request received: {}", networkRequest.toString());
    log.info("[networkStatus] Looking for latest block");
    NetworkStatus networkStatus = networkStatus();

    return networkMapper.toNetworkStatusResponse(networkStatus);
  }

  @Override
  public Network getSupportedNetwork() {
    int networkMagic = genesisDataProvider.getProtocolMagic();

    return switch (networkMagic) {
      case MAINNET_NETWORK_MAGIC -> Networks.mainnet();
      case PREPROD_NETWORK_MAGIC -> Networks.preprod();
      case PREVIEW_NETWORK_MAGIC -> Networks.preview();
      case Constants.SANCHONET_NETWORK_MAGIC -> new Network(0b0000, Constants.SANCHONET_NETWORK_MAGIC);
      case DEVKIT_NETWORK_MAGIC -> new Network(0b0000, DEVKIT_NETWORK_MAGIC);

      default -> throw ExceptionFactory.invalidNetworkError();
    };
  }

  private NetworkStatus networkStatus() {
    log.info("[networkStatus] Looking for latest block");

    BlockIdentifierExtended latestBlock = ledgerBlockService.findLatestBlockIdentifier();
    log.debug("[networkStatus] Latest block found {}", latestBlock);

    log.debug("[networkStatus] Looking for genesis block");
    BlockIdentifierExtended genesisBlock = ledgerBlockService.findGenesisBlockIdentifier();
    log.debug("[networkStatus] Genesis block found {}", genesisBlock);

    List<Peer> peers = topologyConfigService.getPeers();

    val networkStatusBuilder = NetworkStatus.builder()
            .latestBlock(latestBlock)
            .genesisBlock(genesisBlock)
            .peers(peers);

    calculateSyncStatus(latestBlock).ifPresent(networkStatusBuilder::syncStatus);

    return networkStatusBuilder.build();
  }

  private Optional<SyncStatus> calculateSyncStatus(BlockIdentifierExtended latestBlock) {
    return offlineSlotService.getCurrentSlotBasedOnTime().map(slotBasedOnTime -> {
      long slotBasedOnLatestBlock = latestBlock.getSlot();

      boolean isSynced = slotRangeChecker.isSlotWithinEpsilon(slotBasedOnTime, slotBasedOnLatestBlock, allowedSlotsDelta);

      return SyncStatus.builder()
              .targetIndex(slotBasedOnTime)
              .currentIndex(slotBasedOnLatestBlock)
              .synced(isSynced)
              .build();
    });
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

  private boolean verifyNetwork(String inputNetwork) {
    Network currentNetwork = getSupportedNetwork();

    return switch ((int) currentNetwork.getProtocolMagic()) {
      case MAINNET_NETWORK_MAGIC -> inputNetwork.equalsIgnoreCase(MAINNET);
      case PREPROD_NETWORK_MAGIC -> inputNetwork.equals(PREPROD);
      case PREVIEW_NETWORK_MAGIC -> inputNetwork.equals(PREVIEW);
      case DEVKIT_NETWORK_MAGIC -> inputNetwork.equals(DEVKIT);

      default -> false;
    };
  }

}
