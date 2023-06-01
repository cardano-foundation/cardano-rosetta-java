package org.cardanofoundation.rosetta.api.constructionApiService.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionHashRequest;
import org.cardanofoundation.rosetta.api.model.rest.TransactionIdentifierResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    TransactionIdentifierResponse transactionIdentifierResponse = restTemplate.postForObject(
        baseUrl, request, TransactionIdentifierResponse.class);
    assertEquals( transactionIdentifierResponse.getTransactionIdentifier().getHash(),"333a6ccaaa639f7b451ce93764f54f654ef499fdb7b8b24374ee9d99eab9d795");

  }

}
