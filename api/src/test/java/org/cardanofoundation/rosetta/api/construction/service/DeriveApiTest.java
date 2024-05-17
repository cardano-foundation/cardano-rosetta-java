package org.cardanofoundation.rosetta.api.construction.service;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openapitools.client.model.ConstructionDeriveRequest;
import org.openapitools.client.model.ConstructionDeriveResponse;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeriveApiTest extends IntegrationTest {

  @Autowired
  private ConstructionApiService constructionApiService;

  private ConstructionDeriveRequest getDeriveRequest(String fileName) throws IOException {
    File file = new File(this.getClass().getClassLoader().getResource(fileName).getFile());
    ObjectMapper mapper = new ObjectMapper();
    ConstructionDeriveRequest request = mapper.readValue(file, ConstructionDeriveRequest.class);
    return request;
  }

  @Test
  void deriveAddressTest() throws IOException {
    ConstructionDeriveRequest deriveRequest = getDeriveRequest(
        "testdata/construction/derive/derive_request.json");
    ConstructionDeriveResponse constructionDeriveResponse = constructionApiService.constructionDeriveService(
        deriveRequest);

    String address = "addr_test1vza5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7c6mzywr";
    assertEquals(address, constructionDeriveResponse.getAccountIdentifier().getAddress());
  }

}
