package org.cardanofoundation.rosetta.api.construction.service;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openapitools.client.model.ConstructionDeriveRequest;
import org.openapitools.client.model.ConstructionDeriveResponse;
import org.openapitools.client.model.NetworkIdentifier;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.common.exception.ApiException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeriveApiTest extends IntegrationTest {

  @Autowired
  private ConstructionApiService constructionApiService;

  private ConstructionDeriveRequest getDeriveRequest(String fileName) throws IOException {
    File file = new File(this.getClass().getClassLoader().getResource(fileName).getFile());
    ObjectMapper mapper = new ObjectMapper();
    ConstructionDeriveRequest request = mapper.readValue(file, ConstructionDeriveRequest.class);
    return request;
  }

  @Nested
  class DeriveAddressTest {
    @Test
    void shouldReturnCorrectAddress() throws IOException {
      ConstructionDeriveRequest deriveRequest = getDeriveRequest(
          "testdata/construction/derive/derive_request.json");
      ConstructionDeriveResponse constructionDeriveResponse = constructionApiService.constructionDeriveService(
          deriveRequest);

      String address = "addr_test1vza5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7c6mzywr";
      assertEquals(address, constructionDeriveResponse.getAccountIdentifier().getAddress());
    }
  }

  @Nested
  class InvalidNetworkConfigurationTest {

    @Test
    void shouldThrowException() throws IOException {
      ConstructionDeriveRequest deriveRequest = getDeriveRequest(
          "testdata/construction/derive/derive_request.json");
      deriveRequest.setNetworkIdentifier(new NetworkIdentifier());

      assertThrows(ApiException.class,
          () -> constructionApiService.constructionDeriveService(deriveRequest));
    }

    @Test
    void shouldHaveCorrectErrorCodeAndMessage() throws IOException {
      ConstructionDeriveRequest deriveRequest = getDeriveRequest(
          "testdata/construction/derive/derive_request.json");
      deriveRequest.setNetworkIdentifier(new NetworkIdentifier());
      
      ApiException exception = assertThrows(ApiException.class,
          () -> constructionApiService.constructionDeriveService(deriveRequest));
          
      assertEquals(4000, exception.getError().getCode());
      assertEquals("Invalid Network configuration", exception.getError().getMessage());
    }
  }

  @Nested
  class CustomAddressTypeTest {

    @Test
    void shouldThrowExceptionForInvalidAddressType() throws IOException {
      ConstructionDeriveRequest deriveRequest = getDeriveRequest(
          "testdata/construction/derive/derive_request.json");

      // Test Invalid address type
      deriveRequest.getMetadata().setAddressType("InvalidType");
      assertThrows(ApiException.class,
          () -> constructionApiService.constructionDeriveService(deriveRequest));
    }

    @Test
    void shouldHaveCorrectErrorCodeAndMessageForInvalidAddressType() throws IOException {
      ConstructionDeriveRequest deriveRequest = getDeriveRequest(
          "testdata/construction/derive/derive_request.json");

      deriveRequest.getMetadata().setAddressType("InvalidType");
      ApiException exception = assertThrows(ApiException.class,
          () -> constructionApiService.constructionDeriveService(deriveRequest));

      assertEquals(5032, exception.getError().getCode());
      assertEquals("Invalid Address Type", exception.getError().getMessage());
    }

    @Test
    void shouldReturnEnterpriseAddressWhenTypeIsEmpty() throws IOException {
      ConstructionDeriveRequest deriveRequest = getDeriveRequest(
          "testdata/construction/derive/derive_request.json");

      // Test Empty address type defaults to Enterprise
      deriveRequest.getMetadata().setAddressType("");
      ConstructionDeriveResponse response = constructionApiService.constructionDeriveService(deriveRequest);

      String expectedAddress = "addr_test1vza5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7c6mzywr";
      assertEquals(expectedAddress, response.getAccountIdentifier().getAddress());
    }
  }

}
