package org.cardanofoundation.rosetta.api.constructionApiService.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.RosettaApiApplication;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionDeriveResponse;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionPreprocessRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionPreprocessResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.HttpServerErrorException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = RosettaApiApplication.class)
@Slf4j
class ConstructionApiDelegateImplPreprocessTests extends IntegrationTest{

  @BeforeEach
  public void setUp() {
    baseUrl = baseUrl.concat(":").concat(serverPort + "").concat("/construction/preprocess");
  }

  private final String BASE_DIRECTORY = "src/test/resources/files/construction/preprocess";

  @Test
   void test_should_return_a_valid_ttl_when_the_parameters_are_valid() throws IOException {

    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_request_ttl_valid.json"))),
        ConstructionPreprocessRequest.class);

    ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate.postForObject(
        baseUrl, request, ConstructionPreprocessResponse.class);

    assertEquals(constructionPreprocessResponse.getOptions().getRelativeTtl(), 100);
    assertEquals(constructionPreprocessResponse.getOptions().getTransactionSize(), TestFixedData.TRANSACTION_SIZE_IN_BYTES);
  }

  @Test
  void test_should_return_a_valid_ttl_when_the_operations_include_an_input_with_a_byron_address() throws IOException {
    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_request_ttl_valid.json"))),
        ConstructionPreprocessRequest.class);

    ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate.postForObject(
        baseUrl, request, ConstructionPreprocessResponse.class);

    assertEquals(constructionPreprocessResponse.getOptions().getRelativeTtl(), 100);
    assertEquals(constructionPreprocessResponse.getOptions().getTransactionSize(), TestFixedData.TRANSACTION_SIZE_IN_BYTES);
  }

  @Test
  void test_throw_error_when_invalid_outputs_are_sent_as_parameters() throws IOException {
    ConstructionPreprocessRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY + "/construction_preprocess_request_invalid_outputs.json"))),
        ConstructionPreprocessRequest.class);

    try {
      ConstructionPreprocessResponse constructionPreprocessResponse = restTemplate.postForObject(
          baseUrl, request, ConstructionPreprocessResponse.class);
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertTrue(responseBody.contains("ThisIsAnInvalidAddressaddr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpxInvalid"));
      assertEquals(500, e.getRawStatusCode());
    }
  }
}
