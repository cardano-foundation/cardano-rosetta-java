package org.cardanofoundation.rosetta.api.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.cardanofoundation.rosetta.api.IntegrationTestWithDB;
import org.cardanofoundation.rosetta.api.exception.Error;
import org.cardanofoundation.rosetta.api.model.rest.BlockRequest;
import org.cardanofoundation.rosetta.api.model.rest.BlockResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.BlockIdentifier;
import org.springframework.web.client.HttpServerErrorException;

public class BlockApiTest extends IntegrationTestWithDB {

  private static final String ENDPOINT = "/block";
  private static final String NETWORK = "mainnet";
  private final String BASE_DIRECTORY = "src/test/resources/block";

  @BeforeEach
  public void setUp() {
    baseUrl = baseUrl.concat(":").concat(String.valueOf(serverPort)).concat(ENDPOINT);
  }

  @Test
  void test_should_return_an_error_if_block_not_found() throws IOException {
    BlockRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_should_return_an_error_if_block_not_found_request.json"))),
        BlockRequest.class);

    try {
      var response = restTemplate.postForObject(baseUrl,
          request, BlockResponse.class);
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      Error error = objectMapper.readValue(responseBody, Error.class);
      assertEquals(500, e.getStatusCode().value());
      assertEquals(4001, error.getCode());
      assertEquals("Block not found", error.getMessage());
      assertFalse(error.isRetriable());
    }
  }

  @Test
  void test_should_properly_return_a_block_without_transactions_if_requested_by_block_number()
      throws IOException {
    BlockRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_should_properly_return_a_block_without_transactions_if_requested_by_block_number_request.json"))),
        BlockRequest.class);
    var response = restTemplate.postForObject(baseUrl,
        request, BlockResponse.class);
    var expectedResponse = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_should_properly_return_a_block_without_transactions_if_requested_by_block_number.json"))),
        BlockResponse.class);
    assert response != null;
    assertEquals(objectMapper.writeValueAsString(expectedResponse),
        objectMapper.writeValueAsString(response));
  }

  @Test
  void test_should_properly_return_a_block_without_transactions_if_requested_by_block_hash()
      throws IOException {
    BlockRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_should_properly_return_a_block_without_transactions_if_requested_by_block_hash_requested_by_block_number_request.json"))),
        BlockRequest.class);
    var response = restTemplate.postForObject(baseUrl,
        request, BlockResponse.class);
    var expectedResponse = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_should_properly_return_a_block_without_transactions_if_requested_by_block_hash.json"))),
        BlockResponse.class);
    assert response != null;
    assertEquals(objectMapper.writeValueAsString(expectedResponse),
        objectMapper.writeValueAsString(response));
  }

  @Test
  void test_should_be_able_to_fetch_latest_block_information() throws IOException {
    BlockRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_should_be_able_to_fetch_latest_block_information_request.json"))),
        BlockRequest.class);
    var response = restTemplate.postForObject(baseUrl,
        request, BlockResponse.class);

    assert response != null;
    BlockIdentifier blockIdentifier = response.getBlock().getBlockIdentifier();
    assertEquals(5593749, blockIdentifier.getIndex());
    assertEquals("1c42fd317888b2aafe9f84787fdd3b90b95be06687a217cf4e6ca95130157eb5",
        blockIdentifier.getHash());

  }

  @Test
  void test_should_properly_return_a_block_with_transactions() throws IOException {
    BlockRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_should_properly_return_a_block_with_transactions_request.json"))),
        BlockRequest.class);
    var response = restTemplate.postForObject(baseUrl,
        request, BlockResponse.class);
    var expectedResponse = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_should_properly_return_a_block_with_transactions.json"))),
        BlockResponse.class);
    assert response != null;
    assertEquals(objectMapper.writeValueAsString(expectedResponse),
        objectMapper.writeValueAsString(response));
  }

  @Test
  void test_should_properly_return_a_block_with_2_output_transactions() throws IOException {
    BlockRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_should_properly_return_a_block_with_2_output_transactions_request.json"))),
        BlockRequest.class);
    var response = restTemplate.postForObject(baseUrl,
        request, BlockResponse.class);
    var expectedResponse = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_should_properly_return_a_block_with_2_output_transactions.json"))),
        BlockResponse.class);
    assert response != null;
    assertEquals(objectMapper.writeValueAsString(expectedResponse),
        objectMapper.writeValueAsString(response));
  }

  @Test
  void test_should_properly_return_a_block_with_8_transactions_but_only_the_hashes_of_them()
      throws IOException {
    BlockRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_should_properly_return_a_block_with_8_transactions_but_only_the_hashes_of_them_request.json"))),
        BlockRequest.class);
    var response = restTemplate.postForObject(baseUrl,
        request, BlockResponse.class);
    var expectedResponse = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_should_properly_return_a_block_with_8_transactions_but_only_the_hashes_of_them.json"))),
        BlockResponse.class);
    assert response != null;
    assertEquals(objectMapper.writeValueAsString(expectedResponse),
        objectMapper.writeValueAsString(response));
  }

  @Test
  void test_return_an_error_if_hash_sent_in_the_request_does_not_match_the_one_in_block_0()
      throws IOException {
    BlockRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_return_an_error_if_hash_sent_in_the_request_does_not_match_the_one_in_block_0_request.json"))),
        BlockRequest.class);
    try {
      var response = restTemplate.postForObject(baseUrl,
          request, BlockResponse.class);
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      Error error = objectMapper.readValue(responseBody, Error.class);
      assertEquals(500, e.getStatusCode().value());
      assertEquals(4001, error.getCode());
      assertEquals("Block not found", error.getMessage());
      assertFalse(error.isRetriable());
    }
  }

  @Test
  void test_should_be_able_to_return_block_0() throws IOException {
    BlockRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_should_be_able_to_return_block_0_request.json"))),
        BlockRequest.class);
    var response = restTemplate.postForObject(baseUrl,
        request, BlockResponse.class);

    assert response != null;
    assertEquals("5f20df933584822601f9e3f8c024eb5eb252fe8cefb24d1317dc3d432e940ebb",
        response.getBlock().getBlockIdentifier().getHash());
    assertEquals(14505, response.getOtherTransactions().size());

  }

  @Test
  void test_should_return_boundary_block_1_if_requested_by_hash() throws IOException {
    BlockRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_should_return_boundary_block_1_if_requested_by_hash_request.json"))),
        BlockRequest.class);
    var response = restTemplate.postForObject(baseUrl,
        request, BlockResponse.class);
    var expectedResponse = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_should_return_boundary_block_1_if_requested_by_hash.json"))),
        BlockResponse.class);
    assert response != null;
    assertEquals(objectMapper.writeValueAsString(expectedResponse),
        objectMapper.writeValueAsString(response));
    assertEquals("5f20df933584822601f9e3f8c024eb5eb252fe8cefb24d1317dc3d432e940ebb",
        response.getBlock().getParentBlockIdentifier().getHash());
  }

//  @Test
//  void test_should_be_able_to_return_multiasset_token_transactions_with_several_tokens_in_the_bundle()
//      throws IOException {
//    BlockRequest request = objectMapper.readValue(new String(Files.readAllBytes(
//            Paths.get(BASE_DIRECTORY
//                + "/request/test_should_be_able_to_return_multiasset_token_transactions_with_several_tokens_in_the_bundle_request.json"))),
//        BlockRequest.class);
//    var response = restTemplate.postForObject(baseUrl,
//        request, BlockResponse.class);
//    var expectedResponse = objectMapper.readValue(new String(
//            Files.readAllBytes(
//                Paths.get(BASE_DIRECTORY
//                    + "/response/test_should_be_able_to_return_multiasset_token_transactions_with_several_tokens_in_the_bundle.json"))),
//        BlockResponse.class);
//    assert response != null;
//    assertEquals(objectMapper.writeValueAsString(expectedResponse),
//        objectMapper.writeValueAsString(response));
//
//  }

}
