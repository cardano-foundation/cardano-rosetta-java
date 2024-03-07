package org.cardanofoundation.rosetta.api.network.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import org.cardanofoundation.rosetta.api.model.Network;
import org.cardanofoundation.rosetta.api.model.rest.NetworkIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.NetworkRequest;
import org.cardanofoundation.rosetta.api.model.rest.NetworkStatusResponse;
import org.cardanofoundation.rosetta.api.service.LedgerDataProviderService;
import org.cardanofoundation.rosetta.api.service.impl.NetworkServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.cardanofoundation.rosetta.api.model.dto.BlockDto;
import org.cardanofoundation.rosetta.api.model.dto.GenesisBlockDto;

@ExtendWith(MockitoExtension.class)
class NetworkServiceImplTest {

  @Mock
  private LedgerDataProviderService ledgerDataProviderService;
  @InjectMocks
  private NetworkServiceImpl networkServiceImplUnderTest;



  @Test
  void testGetSupportedNetwork_ThenReturnPreProd() throws Exception {
    String genesisPath = "src/main/resources/network-config/network/preprod/genesis/shelley.json";
    ReflectionTestUtils.setField(networkServiceImplUnderTest , "genesisPath" , genesisPath);

    //TODO EPAM: I would suggest to use //given //when //then  - patterns or spec pattern: it should ...
    // and also would add here assertj library https://github.com/assertj/assertj

    // Run the test
    Network result = networkServiceImplUnderTest.getSupportedNetwork();

    // Verify the results
    assertEquals(result.getNetworkId(), "preprod");
  }

  @Test
  void testGetSupportedNetwork_ThenReturnTestNet() throws Exception {
    String genesisPath = "src/main/resources/network-config/network/testnet/genesis/shelley.json";
    ReflectionTestUtils.setField(networkServiceImplUnderTest , "genesisPath" , genesisPath);

    // Run the test
    Network result = networkServiceImplUnderTest.getSupportedNetwork();

    // Verify the results
    assertEquals(result.getNetworkId(), "testnet");
  }

  @Test
  void testGetSupportedNetwork_ThenReturnNull() throws Exception {
    String genesisPath = "src/main/resources/network-config/network/alonzo-purple/genesis/shelley.json";
    ReflectionTestUtils.setField(networkServiceImplUnderTest , "genesisPath" , genesisPath);

    // Run the test
    Network result = networkServiceImplUnderTest.getSupportedNetwork();

    // Verify the results
    assertNull(result);
  }

  @Test
  void testgetNetworkStatus_ThenReturnSuccess() throws Exception {

    NetworkRequest networkRequest = new NetworkRequest(new NetworkIdentifier("cardano" , "mainnet" , null));
    String topologyFilePath = "src/test/resources/config/topology-test.json";
    ReflectionTestUtils.setField(networkServiceImplUnderTest , "topologyFilepath" , topologyFilePath);
    BlockDto blockDto = new BlockDto();
    GenesisBlockDto genesisBlockDto = new GenesisBlockDto();
    when(ledgerDataProviderService.findLatestBlock()).thenReturn(blockDto);
    when(ledgerDataProviderService.findGenesisBlock()).thenReturn(genesisBlockDto);
    //    doReturn(any(BlockDto.class)).when(ledgerDataProviderService).findLatestBlock();
//    doReturn(any(BlockDto.class)).when(ledgerDataProviderService).findGenesisBlock();

    // Run the test
    NetworkStatusResponse result = networkServiceImplUnderTest.getNetworkStatus(networkRequest);


    // Verify the results
    assertEquals(result.getPeers().get(0).getAddr(), "relays-new.cardano-mainnet.iohk.io");
  }
}
