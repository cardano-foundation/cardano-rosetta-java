package org.cardanofoundation.rosetta.api.constructionApiService.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.cardanofoundation.rosetta.api.exception.Error;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionHashRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionPreprocessRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionPreprocessResponse;
import org.cardanofoundation.rosetta.api.model.rest.TransactionIdentifierResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpServerErrorException;

class ConstructionApiDelegateImplHashTests extends IntegrationTest{
  @BeforeEach
  public void setUp() {
    baseUrl = baseUrl.concat(":").concat(serverPort + "").concat("/construction/hash");
  }

  private final String BASE_DIRECTORY = "src/test/resources/files/construction/hash";
  @Test
  void test_should_return_a_valid_hash_when_providing_a_proper_signed_transaction()
      throws IOException {
    ConstructionHashRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_hash_success.json"))),
        ConstructionHashRequest.class);

    TransactionIdentifierResponse transactionIdentifierResponse = restTemplate.postForObject(
        baseUrl, request, TransactionIdentifierResponse.class);
    assertEquals( transactionIdentifierResponse.getTransactionIdentifier().getHash(),"333a6ccaaa639f7b451ce93764f54f654ef499fdb7b8b24374ee9d99eab9d795");
  }

  @Test
  void test_should_return_an_error_when_providing_an_invalid_transaction() throws IOException {
    ConstructionHashRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_hash_failed.json"))),
        ConstructionHashRequest.class);

    try {
      TransactionIdentifierResponse constructionPreprocessResponse = restTemplate.postForObject(
          baseUrl, request, TransactionIdentifierResponse.class);
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      Error error=objectMapper.readValue(responseBody,Error.class);
      assertTrue(!error.isRetriable());
      assertEquals(5003,error.getCode());
      assertEquals("Parse signed transaction error",error.getMessage());
    }
  }
}
