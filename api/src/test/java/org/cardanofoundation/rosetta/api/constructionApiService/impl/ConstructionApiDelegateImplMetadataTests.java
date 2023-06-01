package org.cardanofoundation.rosetta.api.constructionApiService.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionMetadataRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionMetadataResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConstructionApiDelegateImplMetadataTests extends IntegrationTest {

  @BeforeEach
  public void setUp() {
    baseUrl = baseUrl.concat(":").concat(serverPort + "").concat("/construction/metadata");
  }

  private final String BASE_DIRECTORY = "src/test/resources/files/construction/metadata";

  @Test
  void test_parameters_are_valid() throws IOException {
    ConstructionMetadataRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_metadata_request_valid.json"))),
        ConstructionMetadataRequest.class);

    request.getOptions().setTransactionSize(TestFixedData.TRANSACTION_SIZE_IN_BYTES);

    ConstructionMetadataResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionMetadataResponse.class);

    assertEquals(response.getSuggestedFee().get(0).getCurrency().getSymbol(), "ADA");
    assertEquals(response.getSuggestedFee().get(0).getCurrency().getDecimals(), 6);
    assertEquals(response.getMetadata().getProtocolParameters().getMinPoolCost(), "340000000");
    assertEquals(response.getMetadata().getProtocolParameters().getPoolDeposit(), "500000000");
    assertEquals(response.getMetadata().getProtocolParameters().getKeyDeposit(), "2000000");
  }
}
