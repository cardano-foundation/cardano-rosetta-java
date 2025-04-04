package org.cardanofoundation.rosetta.api.network.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openapitools.client.model.Error;
import org.openapitools.client.model.MetadataRequest;
import org.openapitools.client.model.NetworkIdentifier;
import org.openapitools.client.model.NetworkListResponse;
import org.openapitools.client.model.NetworkOptionsResponse;
import org.openapitools.client.model.NetworkRequest;
import org.openapitools.client.model.NetworkStatusResponse;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.EntityGenerator;
import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NetworkServiceImplTest extends IntegrationTest {

  @Autowired
  private NetworkService networkService;

  @Test
  void verifyCorrectNetworkTest() {
    // void function, no exception expected
    networkService.verifyNetworkRequest(
        createNetworkIdentifier(Constants.CARDANO_BLOCKCHAIN, Constants.DEVKIT));
  }

  @Test
  void verifyWrongNetworkTest() {
    NetworkIdentifier wrongNetworkIdentifier = createNetworkIdentifier(Constants.CARDANO_BLOCKCHAIN,
        Constants.MAINNET);
    ApiException apiException = assertThrows(ApiException.class,
        () -> networkService.verifyNetworkRequest(wrongNetworkIdentifier));
    assertEquals(RosettaErrorType.NETWORK_NOT_FOUND.getMessage(),
        apiException.getError().getMessage());
    assertEquals(RosettaErrorType.NETWORK_NOT_FOUND.getCode(), apiException.getError().getCode());
  }

  @Test
  void verifyWrongBlockchainTest() {
    NetworkIdentifier wrongBlockchain = createNetworkIdentifier("Wrong Blockchain",
        Constants.DEVKIT);
    ApiException apiException = assertThrows(ApiException.class,
        () -> networkService.verifyNetworkRequest(wrongBlockchain));
    assertEquals(RosettaErrorType.INVALID_BLOCKCHAIN.getMessage(),
        apiException.getError().getMessage());
    assertEquals(RosettaErrorType.INVALID_BLOCKCHAIN.getCode(), apiException.getError().getCode());
  }

  @Test
  void networkOptionTest() throws IOException {
    //given
    NetworkRequest networkRequest = getNetworkRequest();
    //when
    NetworkOptionsResponse networkOptions = networkService.getNetworkOptions(networkRequest);
    //then
    assertNotNull(networkOptions);
    assertEquals(getErrors(), networkOptions.getAllow().getErrors());
  }

  @Test
  void networkStatusTest() throws IOException {
    //given
    NetworkRequest networkRequest = getNetworkRequest();
    //when
    NetworkStatusResponse networkStatus = networkService.getNetworkStatus(networkRequest);
    //then
    assertNotNull(networkStatus);
    // check index, for Rosetta CLI / spec, they expect index to be 0 for a genesis block for mesh-cli tests to pass
    assertEquals(0, networkStatus.getGenesisBlockIdentifier().getIndex());
    assertEquals("Genesis", networkStatus.getGenesisBlockIdentifier().getHash());
  }

  @Test
  void networkListTest() {
    //given
    MetadataRequest metadataRequest = EntityGenerator.givenMetadataRequest();
    //when
    NetworkListResponse networkList = networkService.getNetworkList(metadataRequest);
    //then
    assertNotNull(networkList);
  }

  private NetworkRequest getNetworkRequest() throws IOException {
    File file = new File(this.getClass().getClassLoader()
            .getResource("testdata/networkIdentifier.json").getFile());
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(file, NetworkRequest.class);
  }

  private List<Error> getErrors() throws IOException {
    File file = new File(this.getClass().getClassLoader()
            .getResource("testdata/errors.json").getFile());
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(file, new TypeReference<>() {});
  }

  private NetworkIdentifier createNetworkIdentifier(String blockchain, String network) {
    return NetworkIdentifier.builder()
        .blockchain(blockchain)
        .network(network)
        .build();
  }
}
