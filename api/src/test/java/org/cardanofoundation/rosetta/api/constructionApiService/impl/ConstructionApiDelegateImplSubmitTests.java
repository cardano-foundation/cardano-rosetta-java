package org.cardanofoundation.rosetta.api.constructionApiService.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.cardanofoundation.rosetta.api.exception.Error;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionHashRequest;
import org.cardanofoundation.rosetta.api.model.rest.TransactionIdentifierResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpServerErrorException;

public class ConstructionApiDelegateImplSubmitTests extends IntegrationTest {
  @BeforeEach
  public void setUp() {
    baseUrl = baseUrl.concat(":").concat(serverPort + "").concat("/construction/submit");
  }
  private final String BASE_DIRECTORY = "src/test/resources/files/construction/submit";
  @Test
  void test_should_return_the_transaction_identifier_if_request_is_valid() throws IOException {
    ConstructionHashRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_submit_success.json"))),
        ConstructionHashRequest.class);
    try {
    restTemplate.postForObject(
        baseUrl, request, TransactionIdentifierResponse.class);
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      Error error=objectMapper.readValue(responseBody,Error.class);
      assertTrue(!error.isRetriable());
      assertEquals(5019,error.getCode());
      assertEquals("The transaction submission has been rejected",error.getMessage());
    }

  }

}
