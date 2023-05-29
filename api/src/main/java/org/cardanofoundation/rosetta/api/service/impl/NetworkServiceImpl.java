package org.cardanofoundation.rosetta.api.service.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.cardanofoundation.rosetta.api.common.constants.Constants;
import org.cardanofoundation.rosetta.api.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.api.common.enumeration.OperationTypeStatus;
import org.cardanofoundation.rosetta.api.config.RosettaConfig;
import org.cardanofoundation.rosetta.api.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.api.exception.ServerException;
import org.cardanofoundation.rosetta.api.mapper.DataMapper;
import org.cardanofoundation.rosetta.api.model.Network;
import org.cardanofoundation.rosetta.api.model.NetworkStatus;
import org.cardanofoundation.rosetta.api.model.Peer;
import org.cardanofoundation.rosetta.api.model.Producer;
import org.cardanofoundation.rosetta.api.model.PublicRoot;
import org.cardanofoundation.rosetta.api.model.TopologyConfig;
import org.cardanofoundation.rosetta.api.model.rest.MetadataRequest;
import org.cardanofoundation.rosetta.api.model.rest.NetworkListResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkOptionsResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkRequest;
import org.cardanofoundation.rosetta.api.model.rest.NetworkStatusResponse;
import org.cardanofoundation.rosetta.api.projection.dto.BlockDto;
import org.cardanofoundation.rosetta.api.projection.dto.GenesisBlockDto;
import org.cardanofoundation.rosetta.api.service.BlockService;
import org.cardanofoundation.rosetta.api.service.LedgerDataProviderService;
import org.cardanofoundation.rosetta.api.service.NetworkService;
import org.cardanofoundation.rosetta.api.util.RosettaConstants;
import org.cardanofoundation.rosetta.api.util.cli.CardanoNode;
import org.openapitools.client.model.Allow;
import org.openapitools.client.model.BalanceExemption;
import org.openapitools.client.model.Error;
import org.openapitools.client.model.OperationStatus;
import org.openapitools.client.model.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

@Service
@Slf4j
public class NetworkServiceImpl implements NetworkService {
  @Autowired
  private RosettaConfig rosettaConfig;

  @Autowired
  private BlockService blockService;


  @Autowired
  private LedgerDataProviderService ledgerDataProviderService;


  private List<BalanceExemption> balanceExemptions;

  @Value("${cardano.rosetta.EXEMPTION_TYPES_PATH}")
  private String exemptionPath ;

  @Value("${cardano.rosetta.TOPOLOGY_FILEPATH}")
  private String topologyFilepath ;

  @Value("${cardano.rosetta.GENESIS_SHELLEY_PATH}")
  private String genesisPath;

  @Value("${cardano.rosetta.CARDANO_NODE_PATH}")
  private String cardanoNodePath;

  private String networkId;
  private BigInteger networkMagic;

  private List<BalanceExemption> loadExemptionsFile() {
    if (exemptionPath != null) {
      final ObjectMapper objectMapper = new ObjectMapper();
      try {
        File exemptionFile = ResourceUtils.getFile(exemptionPath);
        balanceExemptions = objectMapper.readValue(
            new File(exemptionFile.toURI()),
            new TypeReference<List<BalanceExemption>>() {
            });
      } catch (IOException e) {
        return new ArrayList<>();
      }
    } else {
      balanceExemptions = List.of();
    }
    return balanceExemptions;
  }

  @Override
  public NetworkListResponse getNetworkList(MetadataRequest metadataRequest)
      throws FileNotFoundException{
    log.info("[networkList] Looking for all supported networks");
    Network supportedNetwork = getSupportedNetwork();
    return DataMapper.mapToNetworkListResponse(supportedNetwork);
  }

  @Override
  public NetworkOptionsResponse getNetworkOptions(NetworkRequest networkRequest)
      throws IOException, InterruptedException {
    log.info("[networkOptions] Looking for networkOptions");
    SwaggerParseResult result = new OpenAPIParser().readLocation("./rosetta-specifications-1.4.15/api.yaml", null,
        null);

    OpenAPI openAPI = result.getOpenAPI();
    String rosettaVersion = openAPI.getInfo().getVersion();
    String implementationVersion = rosettaConfig.getImplementationVersion();

    String cardanoNodeVersion = CardanoNode.getCardanoNodeVersion(cardanoNodePath);
    OperationStatus success = new OperationStatus().successful(true).status(OperationTypeStatus.SUCCESS.getValue());
    OperationStatus invalid = new OperationStatus().successful(false).status(OperationTypeStatus.INVALID.getValue());
    List<OperationStatus> operationStatuses = List.of(success,invalid);

    return NetworkOptionsResponse.builder()
        .version(new Version().nodeVersion(cardanoNodeVersion)
            .rosettaVersion(rosettaVersion)
            .middlewareVersion(implementationVersion)
            .metadata(new LinkedHashMap<>()))
        .allow(new Allow().operationStatuses(operationStatuses)
            .operationTypes(Arrays.stream(OperationType.values()).map(OperationType::getValue).toList())
            .errors(RosettaConstants.ROSETTA_ERRORS.stream().map(error ->
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
            .balanceExemptions(loadExemptionsFile())
            .mempoolCoins(false))
        .build();
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
  public Network getSupportedNetwork() throws FileNotFoundException {

    File genesisFile;
    genesisFile = ResourceUtils.getFile(genesisPath);
    InputStream input = new FileInputStream(genesisFile);
    HashMap<String,Object> object ;
    try {
      object = (HashMap<String,Object>) new JSONParser(input).parse();
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
    networkId = ((String) object.get("networkId")).toLowerCase();
    networkMagic = (BigInteger) object.get("networkMagic");

    if(networkId.equals("mainnet")){
      return Network.builder().networkId(networkId).build();
    } else if (networkMagic.equals(BigInteger.valueOf(Constants.PREPROD_NETWORK_MAGIC))) {
      return Network.builder().networkId("preprod").build();
    } else if (networkMagic.equals(BigInteger.valueOf(Constants.PREVIEW_NETWORK_MAGIC))) {
      return Network.builder().networkId("preview").build();
    }
    return null;
  }

  private NetworkStatus networkStatus() throws ServerException {
    log.info("[networkStatus] Looking for latest block");
    BlockDto latestBlock = ledgerDataProviderService.findLatestBlock();
    log.debug("[networkStatus] Latest block found " + latestBlock);
    log.debug("[networkStatus] Looking for genesis block");
    GenesisBlockDto genesisBlock = ledgerDataProviderService.findGenesisBlock();
    log.debug("[networkStatus] Genesis block found " + genesisBlock);
    return NetworkStatus.builder()
        .latestBlock(latestBlock)
        .genesisBlock(genesisBlock)
        .peers(getPeerFromConfig(readFromFileConfig()))
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

  private TopologyConfig readFromFileConfig() throws ServerException {
    try {
      ObjectMapper  mapper = new ObjectMapper();
      File file = ResourceUtils.getFile(topologyFilepath);
      return mapper.readValue(file,TopologyConfig.class);

    } catch (IOException e) {
      throw ExceptionFactory.configNotFoundException();
    }
  }
}
