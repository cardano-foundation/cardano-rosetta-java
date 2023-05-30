package org.cardanofoundation.rosetta.api.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.model.rest.NetworkRequest;
import org.cardanofoundation.rosetta.api.model.rest.NetworkStatusResponse;
import org.cardanofoundation.rosetta.api.network.utils.Common;
import org.cardanofoundation.rosetta.api.util.RosettaConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class NetworkApiDelegateImplStatusTest  extends IntegrationTest {
  @BeforeEach
  public void setUp() {
    baseUrl = baseUrl.concat(":").concat(serverPort + "").concat("/network/status");
  }

  @Test
  void test_when_request_with_valid_payload_should_return_object_containing_proper_status_information(){
    NetworkRequest requestPayload = Common.generateNetworkPayload(RosettaConstants.BLOCKCHAIN_NAME,
        RosettaConstants.MAINNET);


    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<NetworkRequest> requestEntity = new HttpEntity<>(requestPayload , headers);
    ResponseEntity<NetworkStatusResponse> responseEntity = restTemplate.postForEntity(baseUrl, requestEntity,
        NetworkStatusResponse.class);
    assertEquals(HttpStatus.OK , responseEntity.getStatusCode() );
    assertNotNull(responseEntity.getBody().getGenesisBlockIdentifier());
    assertNotNull(responseEntity.getBody().getCurrentBlockIdentifier());
//    assertEquals(responseEntity.getBody().getGenesisBlockIdentifier() , );
  }

}
