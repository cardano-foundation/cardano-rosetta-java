package org.cardanofoundation.rosetta.api.constructionApiService.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionMetadataRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionMetadataResponse;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionPayloadsRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionPayloadsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConstructionApiDelegateImplPayload extends IntegrationTest {

  @BeforeEach
  public void setUp() {
    baseUrl = baseUrl.concat(":").concat(serverPort + "").concat("/construction/payloads");
  }

  @Test
  void test_send_valid_input_and_output_operations() throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get("src/test/resources/files/construction/construction_payloads_request_valid.json"))),
        ConstructionPayloadsRequest.class);

    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionPayloadsResponse.class);
  }

}
