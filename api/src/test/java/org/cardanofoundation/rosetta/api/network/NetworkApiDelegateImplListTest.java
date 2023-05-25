package org.cardanofoundation.rosetta.api.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.model.rest.MetadataRequest;
import org.cardanofoundation.rosetta.api.model.rest.NetworkIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.NetworkListResponse;
import org.cardanofoundation.rosetta.api.util.RosettaConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class NetworkApiDelegateImplListTest extends IntegrationTest {
  @BeforeEach
  public void setUp() {
    baseUrl = baseUrl.concat(":").concat(serverPort + "").concat("/network/list");
  }

  @Test
  void test_when_request_with_an_empty_request_body_should_return_an_array_of_one_element_equal_to_cardano_mainnet(){
    MetadataRequest request = MetadataRequest.builder().metadata(new HashMap<>()).build();
    NetworkIdentifier identifier = NetworkIdentifier.builder().blockchain(RosettaConstants.BLOCKCHAIN_NAME)
        .network(RosettaConstants.MAINNET).build();
    NetworkListResponse expectedResponse = new NetworkListResponse();
    expectedResponse.addNetworkIdentifiersItem(identifier);

    NetworkListResponse actualResponse = restTemplate.postForObject(baseUrl, request, NetworkListResponse.class);
    assertEquals(actualResponse.getNetworkIdentifiers() , expectedResponse.getNetworkIdentifiers());

  }
}
