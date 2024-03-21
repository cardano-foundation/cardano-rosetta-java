package org.cardanofoundation.rosetta.api.construction.derive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.construction.service.ConstructionApiService;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.ConstructionDeriveRequest;
import org.openapitools.client.model.ConstructionDeriveResponse;

public class DeriveApiTest extends IntegrationTest {

  private ConstructionApiService constructionApiService;

  private ConstructionDeriveRequest getDeriveRequest(String fileName) throws IOException {
    File file = new File(this.getClass().getClassLoader().getResource(fileName).getFile());
    ObjectMapper mapper = new ObjectMapper();
    ConstructionDeriveRequest request = mapper.readValue(file, ConstructionDeriveRequest.class);
    return request;
  }

  @Test
  public void deriveAddressTest() throws IOException, IllegalAccessException {
    ConstructionDeriveRequest deriveRequest = getDeriveRequest(
        "testdata/construction/derive/derive_request.json");
    ConstructionDeriveResponse constructionDeriveResponse = constructionApiService.constructionDeriveService(
        deriveRequest);

    String address = "addr_test1vza5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7c6mzywr";
    assertEquals(address, constructionDeriveResponse.getAccountIdentifier().getAddress());
  }

}
