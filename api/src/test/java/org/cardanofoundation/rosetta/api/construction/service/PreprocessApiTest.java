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
    assertPreprocessRequest("testdata/construction/preprocess/simple_preprocess.json", 1000, 224);
  }

  @Test
  void twoWithdrawalsTest() throws IOException {
    assertPreprocessRequest("testdata/construction/preprocess/two_withdrawals.json", 100, 399);
  }

  @Test
  void poolRegistrationTest() throws IOException {
    assertPreprocessRequest("testdata/construction/preprocess/pool_registration.json", 100, 921);
  }

  private void assertPreprocessRequest(String constructionPayloadFile, int expectedTtl,
      int expectedTransactionSize)
      throws IOException {
    ConstructionPreprocessRequest preprocessRequest = getPreprocessRequest(constructionPayloadFile);

    ConstructionPreprocessResponse constructionPreprocessResponse = constructionApiService.constructionPreprocessService(
        preprocessRequest);
    Map<String, Double> options = (Map<String, Double>) constructionPreprocessResponse.getOptions();
    assertEquals(expectedTtl, options.get("relative_ttl"));
    assertEquals(expectedTransactionSize, options.get("transaction_size"));
  }
}
