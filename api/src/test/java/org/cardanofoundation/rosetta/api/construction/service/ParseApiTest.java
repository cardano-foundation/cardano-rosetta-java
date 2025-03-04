package org.cardanofoundation.rosetta.api.construction.service;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openapitools.client.model.ConstructionParseRequest;
import org.openapitools.client.model.ConstructionParseResponse;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParseApiTest extends IntegrationTest {

  @Autowired
  private ConstructionApiService constructionApiService;

  private ConstructionParseRequest getParseRequest(String fileName) throws IOException {
    File file = new File(this.getClass().getClassLoader().getResource(fileName).getFile());
    ObjectMapper mapper = new ObjectMapper();
    ConstructionParseRequest request = mapper.readValue(file, ConstructionParseRequest.class);

    return request;
  }

  @Test
  void depositParseTest() throws IOException {
    ConstructionParseRequest parseRequest = getParseRequest(
        "testdata/construction/parse/deposit_request.json");
    ConstructionParseResponse parseResponse = constructionApiService.constructionParseService(
        parseRequest);

    assertEquals(4, parseResponse.getOperations().size());
    assertEquals("-9000000", parseResponse.getOperations().get(0).getAmount().getValue());
    assertEquals("addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx", parseResponse.getOperations().get(1).getAccount().getAddress());
    assertEquals("40000", parseResponse.getOperations().get(2).getAmount().getValue());
    assertEquals("1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F", parseResponse.getOperations().get(3).getMetadata().getStakingCredential().getHexBytes());
  }

  @Test
  void refundParseTest() throws IOException {
    ConstructionParseRequest parseRequest = getParseRequest(
        "testdata/construction/parse/refund_request.json");
    ConstructionParseResponse parseResponse = constructionApiService.constructionParseService(
        parseRequest);

    assertEquals(4, parseResponse.getOperations().size());
    assertEquals("-90000000", parseResponse.getOperations().get(0).getAmount().getValue());
    assertEquals("addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx", parseResponse.getOperations().get(1).getAccount().getAddress());
    assertEquals("4", parseResponse.getOperations().get(2).getAmount().getValue());
    assertEquals("1B400D60AAF34EAF6DCBAB9BBA46001A23497886CF11066F7846933D30E5AD3F", parseResponse.getOperations().get(3).getMetadata().getStakingCredential().getHexBytes());
  }

  @Test
  void signedMultiAssetTest() throws IOException {
    ConstructionParseRequest parseRequest = getParseRequest(
        "testdata/construction/parse/signed_multiasset_request.json");
    ConstructionParseResponse parseResponse = constructionApiService.constructionParseService(
        parseRequest);

    assertEquals(3, parseResponse.getOperations().size());
    assertEquals("-90000", parseResponse.getOperations().get(0).getAmount().getValue());
    assertEquals("b0d07d45fe9514f80213f4020e5a61241458be626841cde717cb38a7", parseResponse.getOperations().get(0).getMetadata().getTokenBundle().get(0).getPolicyId());
    assertEquals("addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx", parseResponse.getOperations().get(1).getAccount().getAddress());
    assertEquals(3, parseResponse.getOperations().get(1).getMetadata().getTokenBundle().get(0).getTokens().size());
    assertEquals("40000", parseResponse.getOperations().get(2).getAmount().getValue());
  }
}
