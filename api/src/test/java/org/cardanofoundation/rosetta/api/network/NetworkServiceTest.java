package org.cardanofoundation.rosetta.api.network;



import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.cardanofoundation.rosetta.api.RosettaApiApplication;
import org.cardanofoundation.rosetta.api.config.IndexerConfig;
import org.cardanofoundation.rosetta.api.config.NetworkConfig;
import org.cardanofoundation.rosetta.api.config.NodeBridgeConfig;
import org.cardanofoundation.rosetta.api.config.RosettaConfig;
import org.cardanofoundation.rosetta.api.model.rest.MetadataRequest;
import org.cardanofoundation.rosetta.api.model.rest.NetworkIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.NetworkListResponse;
import org.cardanofoundation.rosetta.api.service.impl.NetworkServiceImpl;
import org.cardanofoundation.rosetta.api.service.BlockService;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = RosettaApiApplication.class)

public class NetworkServiceTest {
  @Mock
  private RosettaConfig rosettaConfig;

  @Mock
  private BlockService blockService;

  private final String topologyFilepath = "../config/topology-test.json";

  @InjectMocks
  private NetworkServiceImpl networkService;

  @Before
  public void setUp(){
    MockitoAnnotations.initMocks(this);
    IndexerConfig indexerConfig = new IndexerConfig("ledgersync","http://localhost:8080");
    NodeBridgeConfig nodeBridgeConfig = new NodeBridgeConfig("yaci","http://localhost:1337");

    NetworkConfig networkConfig1 = new NetworkConfig();
    networkConfig1.setId("mainnet");
    networkConfig1.setNodeVersion("cardano node 1.30.1");
    networkConfig1.setIndexer(indexerConfig);
    networkConfig1.setNodeBridge(nodeBridgeConfig);

    NetworkConfig networkConfig2 = new NetworkConfig();
    networkConfig2.setId("preprod");
    networkConfig2.setProtocolMagic(1L);
    networkConfig2.setNodeVersion("cardano node 1.30.1");
    networkConfig2.setIndexer(indexerConfig);
    networkConfig2.setNodeBridge(nodeBridgeConfig);

    NetworkConfig networkConfig3 = new NetworkConfig();
    networkConfig3.setId("preview");
    networkConfig3.setProtocolMagic(2L);
    networkConfig3.setNodeVersion("cardano node 1.30.1");
    networkConfig3.setIndexer(indexerConfig);
    networkConfig3.setNodeBridge(nodeBridgeConfig);

    Mockito.when(rosettaConfig.getNetworks()).thenReturn(Arrays.asList(networkConfig1 , networkConfig2 , networkConfig3));

  }

  @Test
  public void testGetNetworkList() throws IOException {
    MetadataRequest metadataRequest = new MetadataRequest(null);
    NetworkListResponse response = networkService.getNetworkList(metadataRequest);

    List<NetworkIdentifier> networkIdentifiers = response.getNetworkIdentifiers();
    Assertions.assertEquals(3, networkIdentifiers.size());
    NetworkIdentifier networkIdentifier1 = networkIdentifiers.get(0);
    Assertions.assertEquals("cardano", networkIdentifier1.getBlockchain());
    Assertions.assertEquals("mainnet", networkIdentifier1.getNetwork());

    NetworkIdentifier networkIdentifier2 = networkIdentifiers.get(1);
    Assertions.assertEquals("cardano", networkIdentifier2.getBlockchain());
    Assertions.assertEquals("preprod", networkIdentifier2.getNetwork());

    NetworkIdentifier networkIdentifier3= networkIdentifiers.get(2);
    Assertions.assertEquals("cardano", networkIdentifier3.getBlockchain());
    Assertions.assertEquals("preview", networkIdentifier3.getNetwork());

  }
}
