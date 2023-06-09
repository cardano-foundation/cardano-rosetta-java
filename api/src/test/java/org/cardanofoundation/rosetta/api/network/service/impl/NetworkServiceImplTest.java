package org.cardanofoundation.rosetta.api.network.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.cardanofoundation.rosetta.api.config.RosettaConfig;
import org.cardanofoundation.rosetta.api.model.Network;
import org.cardanofoundation.rosetta.api.service.LedgerDataProviderService;
import org.cardanofoundation.rosetta.api.service.impl.NetworkServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@ExtendWith(MockitoExtension.class)
class NetworkServiceImplTest {

  @Mock
  private RosettaConfig mockRosettaConfig;
  @Mock
  private LedgerDataProviderService mockLedgerDataProviderService;
  @Mock
  private ResourceLoader mockResourceLoader;

  private NetworkServiceImpl networkServiceImplUnderTest;

  @BeforeEach
  void setUp() {
    networkServiceImplUnderTest = new NetworkServiceImpl(mockRosettaConfig,
        mockLedgerDataProviderService, mockResourceLoader);
  }

//  @Test
//  void testGetNetworkList() throws Exception {
//    // Setup
//    final MetadataRequest metadataRequest = new MetadataRequest("metadata");
//
//    // Configure ResourceLoader.getResource(...).
//    final Resource resource = new ByteArrayResource("content".getBytes());
//    when(mockResourceLoader.getResource("location")).thenReturn(resource);
//
//    // Run the test
//    final NetworkListResponse result = networkServiceImplUnderTest.getNetworkList(metadataRequest);
//
//    // Verify the results
//  }
//
//  @Test
//  void testGetNetworkOptions() throws Exception {
//    // Setup
//    final SubNetworkIdentifier subNetworkIdentifier = new SubNetworkIdentifier();
//    final NetworkRequest networkRequest = new NetworkRequest(
//        new NetworkIdentifier("blockchain", "network", subNetworkIdentifier));
//    final Version version = new Version();
//    version.rosettaVersion("rosettaVersion");
//    version.nodeVersion("nodeVersion");
//    version.middlewareVersion("middlewareVersion");
//    version.metadata("metadata");
//    final Allow allow = new Allow();
//    final OperationStatus operationStatus = new OperationStatus();
//    operationStatus.status("value");
//    operationStatus.successful(false);
//    allow.operationStatuses(List.of(operationStatus));
//    allow.operationTypes(List.of("value"));
//    final Error error = new Error();
//    error.code(0);
//    error.message("message");
//    error.description("description");
//    error.retriable(false);
//    allow.errors(List.of(error));
//    allow.historicalBalanceLookup(false);
//    allow.callMethods(List.of("value"));
//    final BalanceExemption balanceExemption = new BalanceExemption();
//    allow.balanceExemptions(List.of(balanceExemption));
//    allow.mempoolCoins(false);
//    final NetworkOptionsResponse expectedResult = new NetworkOptionsResponse(version, allow);
//
//    // Configure ResourceLoader.getResource(...).
//    final Resource resource = new ByteArrayResource("content".getBytes());
//    when(mockResourceLoader.getResource(
//        "classpath:/rosetta-specifications-1.4.15/api.yaml")).thenReturn(resource);
//
//    when(mockRosettaConfig.getImplementationVersion()).thenReturn("middlewareVersion");
//
//    // Run the test
//    final NetworkOptionsResponse result = networkServiceImplUnderTest.getNetworkOptions(
//        networkRequest);
//
//    // Verify the results
//    assertThat(result).isEqualTo(expectedResult);
//  }
//
//  @Test
//  void testGetNetworkStatus_thenReturnConfigNotFoundException() throws Exception {
//    // Setup
//
//    NetworkRequest networkRequest = new NetworkRequest(
//        new NetworkIdentifier("blockchain", "network", null));
////    final NetworkStatusResponse expectedResult = NetworkStatusResponse.builder()
////        .currentBlockIdentifier(BlockIdentifier.builder()
////            .index(0L)
////            .hash("hash")
////            .build())
////        .currentBlockTimeStamp(0L)
////        .genesisBlockIdentifier(BlockIdentifier.builder()
////            .index(0L)
////            .hash("hash")
////            .build())
////        .peers(List.of(new Peer()))
////        .build();
//
//    // Configure LedgerDataProviderService.findLatestBlock(...).
//    final BlockDto blockDto = BlockDto.builder()
//        .hash("hash")
//        .number(0L)
//        .createdAt(0L)
//        .build();
//    when(mockLedgerDataProviderService.findLatestBlock()).thenReturn(blockDto);
//
//    when(mockLedgerDataProviderService.findGenesisBlock())
//        .thenReturn(new GenesisBlockDto("hash", 0L));
//
//    // Configure ResourceLoader.getResource(...).
////    when(mockResourceLoader.getResource(any())).th
//
//
//    // Verify the results
//    assertThrows(ServerException.class, () ->networkServiceImplUnderTest.getNetworkStatus(
//        networkRequest) ,"Expected ServerException to be thrown");
//  }

  @Test
  void testGetSupportedNetwork_ThenReturnPreProd() throws Exception {
    String genesisPath = "src/main/resources/network-config/network/preprod/genesis/shelley.json";
    String content = readFile(genesisPath);
    Resource resource = new ByteArrayResource(content.getBytes());
    doReturn(resource).when(mockResourceLoader).getResource(any());

    // Run the test
    Network result = networkServiceImplUnderTest.getSupportedNetwork();

    // Verify the results
    assertEquals(result.getNetworkId(), "preprod");
  }

  @Test
  void testGetSupportedNetwork_ThenReturnTestNet() throws Exception {
    String genesisPath = "src/main/resources/network-config/network/testnet/genesis/shelley.json";
    String content = readFile(genesisPath);
    Resource resource = new ByteArrayResource(content.getBytes());
    doReturn(resource).when(mockResourceLoader).getResource(any());

    // Run the test
    Network result = networkServiceImplUnderTest.getSupportedNetwork();

    // Verify the results
    assertEquals(result.getNetworkId(), "testnet");
  }

  @Test
  void testGetSupportedNetwork_ThenReturnNull() throws Exception {
    String genesisPath = "src/main/resources/network-config/network/alonzo-purple/genesis/shelley.json";
    String content = readFile(genesisPath);
    Resource resource = new ByteArrayResource(content.getBytes());
    doReturn(resource).when(mockResourceLoader).getResource(any());

    // Run the test
    Network result = networkServiceImplUnderTest.getSupportedNetwork();

    // Verify the results
    assertNull(result);
  }

  private String readFile(String path) throws IOException {
    try (InputStream input = new FileInputStream(path)) {
      byte[] fileBytes = input.readAllBytes();
      return new String(fileBytes , StandardCharsets.UTF_8);
    }
  }
}
