package org.cardanofoundation.rosetta.api.construction.service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openapitools.client.model.ConstructionPreprocessRequest;
import org.openapitools.client.model.ConstructionPreprocessResponse;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PreprocessApiTest extends IntegrationTest {

  @Autowired
  private ConstructionApiService constructionApiService;

  private ConstructionPreprocessRequest getPreprocessRequest(String constructionPayloadFile)
      throws IOException {
    File file =
        new File(this.getClass().getClassLoader().getResource(constructionPayloadFile).getFile());
    ObjectMapper mapper = new ObjectMapper();
    ConstructionPreprocessRequest request = mapper.readValue(file,
        ConstructionPreprocessRequest.class);

    return request;
  }

  @Test
  void simplePreprocessTest() throws IOException {
    assertPreprocessRequest("testdata/construction/preprocess/simple_preprocess.json", 1000, 234);
  }

  @Test
  void twoWithdrawalsTest() throws IOException {
    assertPreprocessRequest("testdata/construction/preprocess/two_withdrawals.json", 100, 409);
  }

  @Test
  void poolRegistrationTest() throws IOException {
    assertPreprocessRequest("testdata/construction/preprocess/pool_registration.json", 100, 930);
  }

  @Test
  void dRepDelegationTest() throws IOException {
    assertPreprocessRequest("testdata/construction/preprocess/drep_vote_delegation.json", 100, 405);
  }

  private void assertPreprocessRequest(String constructionPayloadFile, int expectedTtl,
      int expectedTransactionSize)
      throws IOException {
    ConstructionPreprocessRequest preprocessRequest = getPreprocessRequest(constructionPayloadFile);

    ConstructionPreprocessResponse constructionPreprocessResponse = constructionApiService.constructionPreprocessService(
        preprocessRequest);
    Map<String, Integer> options = (Map<String, Integer>) constructionPreprocessResponse.getOptions();

    assertEquals(expectedTtl, options.get("relative_ttl"), "relative_ttl is not as expected");
    assertEquals(expectedTransactionSize, options.get("transaction_size"), "transaction_size is not as expected");
  }

}
