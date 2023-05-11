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

public class ConstructionApiDelegateImplMetadataTests extends IntegrationTest {

  private final double latestBlockSlot = 26912827;

  @BeforeEach
  public void setUp() {
    baseUrl = baseUrl.concat(":").concat(serverPort + "").concat("/construction/metadata");
  }

  @Test
  void test_parameters_are_valid() throws IOException {
    ConstructionMetadataRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get("src/test/resources/files/construction/construction_metadata_request_valid.json"))),
        ConstructionMetadataRequest.class);

    request.getOptions().setTransactionSize(TestFixedData.TRANSACTION_SIZE_IN_BYTES);

    ConstructionMetadataResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionMetadataResponse.class);

    assertEquals(response.getSuggestedFee().get(0).getCurrency().getSymbol(), "ADA");
    assertEquals(response.getSuggestedFee().get(0).getCurrency().getDecimals(), 6);

  }
}
