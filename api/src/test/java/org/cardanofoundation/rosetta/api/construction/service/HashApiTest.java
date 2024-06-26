package org.cardanofoundation.rosetta.api.construction.service;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openapitools.client.model.ConstructionHashRequest;
import org.openapitools.client.model.TransactionIdentifierResponse;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HashApiTest extends IntegrationTest {

  @Autowired
  private ConstructionApiService constructionApiService;

  private ConstructionHashRequest getHashRequest(String fileName) throws IOException {
    File file = new File(this.getClass().getClassLoader().getResource(fileName).getFile());
    ObjectMapper mapper = new ObjectMapper();
    ConstructionHashRequest request = mapper.readValue(file, ConstructionHashRequest.class);
    return request;
  }

  @Test
  void hashTest() throws IOException {
    ConstructionHashRequest hashRequest = getHashRequest(
        "testdata/construction/hash/hash_request.json");
    TransactionIdentifierResponse transactionIdentifierResponse = constructionApiService.constructionHashService(
        hashRequest);

    String hash = "333a6ccaaa639f7b451ce93764f54f654ef499fdb7b8b24374ee9d99eab9d795";
    assertEquals(hash, transactionIdentifierResponse.getTransactionIdentifier().getHash());
  }
}
