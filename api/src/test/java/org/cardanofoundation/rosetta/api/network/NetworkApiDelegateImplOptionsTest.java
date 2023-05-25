package org.cardanofoundation.rosetta.api.network;



import static org.junit.jupiter.api.Assertions.assertEquals;

import org.cardanofoundation.rosetta.api.IntegrationTest;

import org.cardanofoundation.rosetta.api.model.rest.NetworkOptionsResponse;
import org.cardanofoundation.rosetta.api.model.rest.NetworkRequest;
import org.cardanofoundation.rosetta.api.network.utils.Common;
import org.cardanofoundation.rosetta.api.util.RosettaConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class NetworkApiDelegateImplOptionsTest extends IntegrationTest {
  @BeforeEach
  public void setUp() {
    baseUrl = baseUrl.concat(":").concat(serverPort + "").concat("/network/options");
  }

  @Test
  void test_when_request_with_valid_payload_should_return_object_containing_proper_version_information(){
    NetworkRequest requestPayload = Common.generateNetworkPayload(RosettaConstants.BLOCKCHAIN_NAME,
        RosettaConstants.MAINNET);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<NetworkRequest> requestEntity = new HttpEntity<>(requestPayload , headers);
    ResponseEntity<NetworkOptionsResponse> responseEntity = restTemplate.postForEntity(baseUrl, requestEntity,
        NetworkOptionsResponse.class);
    assertEquals(HttpStatus.OK , responseEntity.getStatusCode() );
  }

}
