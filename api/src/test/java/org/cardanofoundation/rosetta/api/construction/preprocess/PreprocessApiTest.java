package org.cardanofoundation.rosetta.api.construction.preprocess;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.exception.AddressExcepion;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openapitools.client.model.ConstructionPreprocessRequest;
import org.openapitools.client.model.ConstructionPreprocessResponse;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.construction.service.ConstructionApiService;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PreprocessApiTest extends IntegrationTest {

  @Autowired
  private ConstructionApiService constructionApiService;

  private ConstructionPreprocessRequest getPreprocessRequest(String fileName) throws IOException {
    File file = new File(this.getClass().getClassLoader().getResource(fileName).getFile());
    ObjectMapper mapper = new ObjectMapper();
    ConstructionPreprocessRequest request = mapper.readValue(file, ConstructionPreprocessRequest.class);
    return request;
  }

  @Test
  void simplePreprocessTest()
      throws IOException, CborException, AddressExcepion, CborSerializationException {
    ConstructionPreprocessRequest preprocessRequest = getPreprocessRequest(
        "testdata/construction/preprocess/simple_preprocess.json");

    ConstructionPreprocessResponse constructionPreprocessResponse = constructionApiService.constructionPreprocessService(
        preprocessRequest);
    Map<String, Double> options = (Map<String, Double>)constructionPreprocessResponse.getOptions();
    assertEquals(1000, options.get("relative_ttl"));
    assertEquals(224, options.get("transaction_size"));
  }

  @Test
  void twoWithdrawalsTest()
      throws IOException, CborException, AddressExcepion, CborSerializationException {
    ConstructionPreprocessRequest preprocessRequest = getPreprocessRequest(
        "testdata/construction/preprocess/two_withdrawals.json");

    ConstructionPreprocessResponse constructionPreprocessResponse = constructionApiService.constructionPreprocessService(
        preprocessRequest);
    Map<String, Double> options = (Map<String, Double>)constructionPreprocessResponse.getOptions();
    assertEquals(100, options.get("relative_ttl"));
    assertEquals(399, options.get("transaction_size"));
  }

  @Test
  void poolRegistrationTest()
      throws IOException, CborException, AddressExcepion, CborSerializationException {
    ConstructionPreprocessRequest preprocessRequest = getPreprocessRequest(
        "testdata/construction/preprocess/pool_registration.json");

    ConstructionPreprocessResponse constructionPreprocessResponse = constructionApiService.constructionPreprocessService(
        preprocessRequest);
    Map<String, Double> options = (Map<String, Double>)constructionPreprocessResponse.getOptions();
    assertEquals(100, options.get("relative_ttl"));
    assertEquals(921, options.get("transaction_size"));
  }


}
