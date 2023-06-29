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

//  @Test
//  void testFilePathExistingValidator_returnServerException() {
//    String topologyFilepath = "path/to/topologyFile";
//    ReflectionTestUtils.setField(networkServiceImplUnderTest , "topologyFilepath" , topologyFilepath);
//    when(mockResourceLoader.getResource(topologyFilepath)).thenReturn(null);
//
//    assertThrows(ServerException.class ,() -> networkServiceImplUnderTest.filePathExistingValidator());
//  }


  private String readFile(String path) throws IOException {
    try (InputStream input = new FileInputStream(path)) {
      byte[] fileBytes = input.readAllBytes();
      return new String(fileBytes, StandardCharsets.UTF_8);
    }
  }
}
