//package org.cardanofoundation.rosetta.api.block;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import org.cardanofoundation.rosetta.api.IntegrationTestWithDB;
//import org.cardanofoundation.rosetta.api.exception.Error;
//import org.cardanofoundation.rosetta.api.model.rest.BlockTransactionRequest;
//import org.cardanofoundation.rosetta.api.model.rest.BlockTransactionResponse;
//import org.cardanofoundation.rosetta.api.util.RosettaConstants.RosettaErrorType;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.web.client.HttpServerErrorException;
//
//public class BlockTransactionApiTest extends IntegrationTestWithDB {
//
//  private static final String ENDPOINT = "/block/transaction";
//  private static final String NETWORK = "mainnet";
//  private final String BASE_DIRECTORY = "src/test/resources/blockTransaction";
//
//  @BeforeEach
//  public void setUp() {
//    baseUrl = baseUrl.concat(":").concat(String.valueOf(serverPort)).concat(ENDPOINT);
//  }
//
//  @Test
//  void test_should_return_the_transaction_if_a_valid_hash_is_sent()
//      throws IOException {
//    BlockTransactionRequest request = objectMapper.readValue(new String(Files.readAllBytes(
//            Paths.get(BASE_DIRECTORY
//                + "/request/test_should_return_the_transaction_if_a_valid_hash_is_sent_request.json"))),
//        BlockTransactionRequest.class);
//    var response = restTemplate.postForObject(baseUrl,
//        request, BlockTransactionResponse.class);
//    var expectedResponse = objectMapper.readValue(new String(
//            Files.readAllBytes(
//                Paths.get(BASE_DIRECTORY
//                    + "/response/test_should_return_the_transaction_if_a_valid_hash_is_sent.json"))),
//        BlockTransactionResponse.class);
//    assert response != null;
//    assertEquals(objectMapper.writeValueAsString(expectedResponse),
//        objectMapper.writeValueAsString(response));
//  }
//
//  @Test
//  void test_should_return_an_error_if_the_transaction_doesnt_exist()
//      throws IOException {
//    BlockTransactionRequest request = objectMapper.readValue(new String(Files.readAllBytes(
//            Paths.get(BASE_DIRECTORY
//                + "/request/test_should_return_an_error_if_the_transaction_doesnt_exist_request.json"))),
//        BlockTransactionRequest.class);
//
//    try {
//      var response = restTemplate.postForObject(baseUrl,
//          request, BlockTransactionResponse.class);
//    } catch (HttpServerErrorException e) {
//      String responseBody = e.getResponseBodyAsString();
//      Error error = objectMapper.readValue(responseBody, Error.class);
//      assertEquals(500, e.getStatusCode().value());
//      assertEquals(4006, error.getCode());
//      assertEquals(RosettaErrorType.TRANSACTION_NOT_FOUND.getMessage(), error.getMessage());
//      assertFalse(error.isRetriable());
//    }
//  }
//
//  @Test
//  void test_should_fail_if_incorrect_network_identifier_is_sent()
//      throws IOException {
//    BlockTransactionRequest request = objectMapper.readValue(new String(Files.readAllBytes(
//            Paths.get(BASE_DIRECTORY
//                + "/request/test_should_fail_if_incorrect_network_identifier_is_sent_request.json"))),
//        BlockTransactionRequest.class);
//
//    try {
//      var response = restTemplate.postForObject(baseUrl,
//          request, BlockTransactionResponse.class);
//    } catch (HttpServerErrorException e) {
//      String responseBody = e.getResponseBodyAsString();
//      Error error = objectMapper.readValue(responseBody, Error.class);
//      assertEquals(500, e.getStatusCode().value());
//      assertEquals(RosettaErrorType.NETWORK_NOT_FOUND.getCode(), error.getCode());
//      assertEquals(RosettaErrorType.NETWORK_NOT_FOUND.getMessage(), error.getMessage());
//      assertFalse(error.isRetriable());
//    }
//  }
//
//  @Test
//  void test_should_fail_if_incorrect_blockchain_identifier_is_sent()
//      throws IOException {
//    BlockTransactionRequest request = objectMapper.readValue(new String(Files.readAllBytes(
//            Paths.get(BASE_DIRECTORY
//                + "/request/test_should_fail_if_incorrect_blockchain_identifier_is_sent_request.json"))),
//        BlockTransactionRequest.class);
//
//    try {
//      var response = restTemplate.postForObject(baseUrl,
//          request, BlockTransactionResponse.class);
//    } catch (HttpServerErrorException e) {
//      String responseBody = e.getResponseBodyAsString();
//      Error error = objectMapper.readValue(responseBody, Error.class);
//      assertEquals(500, e.getStatusCode().value());
//      assertEquals(RosettaErrorType.INVALID_BLOCKCHAIN.getCode(), error.getCode());
//      assertEquals(RosettaErrorType.INVALID_BLOCKCHAIN.getMessage(), error.getMessage());
//      assertFalse(error.isRetriable());
//    }
//  }
//
//  @Test
//  void test_should_fail_if_requested_block_index_does_not_correspond_to_requested_block_hash()
//      throws IOException {
//    BlockTransactionRequest request = objectMapper.readValue(new String(Files.readAllBytes(
//            Paths.get(BASE_DIRECTORY
//                + "/request/test_should_fail_if_requested_block_index_does_not_correspond_to_requested_block_hash_request.json"))),
//        BlockTransactionRequest.class);
//
//    try {
//      var response = restTemplate.postForObject(baseUrl,
//          request, BlockTransactionResponse.class);
//    } catch (HttpServerErrorException e) {
//      String responseBody = e.getResponseBodyAsString();
//      Error error = objectMapper.readValue(responseBody, Error.class);
//      assertEquals(500, e.getStatusCode().value());
//      assertEquals(RosettaErrorType.TRANSACTION_NOT_FOUND.getCode(), error.getCode());
//      assertEquals(RosettaErrorType.TRANSACTION_NOT_FOUND.getMessage(), error.getMessage());
//      assertFalse(error.isRetriable());
//    }
//  }
//
//  @Test
//  void test_should_fail_if_requested_block_hash_does_not_correspond_to_requested_block_index()
//      throws IOException {
//    BlockTransactionRequest request = objectMapper.readValue(new String(Files.readAllBytes(
//            Paths.get(BASE_DIRECTORY
//                + "/request/test_should_fail_if_requested_block_hash_does_not_correspond_to_requested_block_index_request.json"))),
//        BlockTransactionRequest.class);
//
//    try {
//      var response = restTemplate.postForObject(baseUrl,
//          request, BlockTransactionResponse.class);
//    } catch (HttpServerErrorException e) {
//      String responseBody = e.getResponseBodyAsString();
//      Error error = objectMapper.readValue(responseBody, Error.class);
//      assertEquals(500, e.getStatusCode().value());
//      assertEquals(RosettaErrorType.TRANSACTION_NOT_FOUND.getCode(), error.getCode());
//      assertEquals(RosettaErrorType.TRANSACTION_NOT_FOUND.getMessage(), error.getMessage());
//      assertFalse(error.isRetriable());
//    }
//  }
//
//  @Test
//  void test_should_return_transaction_for_genesis_block_when_requested()
//      throws IOException {
//    BlockTransactionRequest request = objectMapper.readValue(new String(Files.readAllBytes(
//            Paths.get(BASE_DIRECTORY
//                + "/request/test_should_return_transaction_for_genesis_block_when_requested_request.json"))),
//        BlockTransactionRequest.class);
//
//    var response = restTemplate.postForObject(baseUrl,
//        request, BlockTransactionResponse.class);
//    var expectedResponse = objectMapper.readValue(new String(
//            Files.readAllBytes(
//                Paths.get(BASE_DIRECTORY
//                    + "/response/test_should_return_transaction_for_genesis_block_when_requested.json"))),
//        BlockTransactionResponse.class);
//    assert response != null;
//    assertEquals(objectMapper.writeValueAsString(expectedResponse),
//        objectMapper.writeValueAsString(response));
//  }
//
//  @Test
//  void test_should_return_transaction_withdrawals()
//      throws IOException {
//    BlockTransactionRequest request = objectMapper.readValue(new String(Files.readAllBytes(
//            Paths.get(BASE_DIRECTORY
//                + "/request/test_should_return_transaction_withdrawals_request.json"))),
//        BlockTransactionRequest.class);
//
//    var response = restTemplate.postForObject(baseUrl,
//        request, BlockTransactionResponse.class);
//    var expectedResponse = objectMapper.readValue(new String(
//            Files.readAllBytes(
//                Paths.get(BASE_DIRECTORY
//                    + "/response/test_should_return_transaction_withdrawals.json"))),
//        BlockTransactionResponse.class);
//    assert response != null;
//    assertEquals(objectMapper.writeValueAsString(expectedResponse),
//        objectMapper.writeValueAsString(response));
//  }
//
//  @Test
//  void test_should_return_transaction_registrations()
//      throws IOException {
//    BlockTransactionRequest request = objectMapper.readValue(new String(Files.readAllBytes(
//            Paths.get(BASE_DIRECTORY
//                + "/request/test_should_return_transaction_registrations_request.json"))),
//        BlockTransactionRequest.class);
//
//    var response = restTemplate.postForObject(baseUrl,
//        request, BlockTransactionResponse.class);
//    var expectedResponse = objectMapper.readValue(new String(
//            Files.readAllBytes(
//                Paths.get(BASE_DIRECTORY
//                    + "/response/test_should_return_transaction_registrations.json"))),
//        BlockTransactionResponse.class);
//    assert response != null;
//    assertEquals(objectMapper.writeValueAsString(expectedResponse),
//        objectMapper.writeValueAsString(response));
//  }
//
//  @Test
//  void test_should_return_transaction_delegations()
//      throws IOException {
//    BlockTransactionRequest request = objectMapper.readValue(new String(Files.readAllBytes(
//            Paths.get(BASE_DIRECTORY
//                + "/request/test_should_return_transaction_delegations_request.json"))),
//        BlockTransactionRequest.class);
//
//    var response = restTemplate.postForObject(baseUrl,
//        request, BlockTransactionResponse.class);
//    var expectedResponse = objectMapper.readValue(new String(
//            Files.readAllBytes(
//                Paths.get(BASE_DIRECTORY
//                    + "/response/test_should_return_transaction_delegations.json"))),
//        BlockTransactionResponse.class);
//    assert response != null;
//    assertEquals(objectMapper.writeValueAsString(expectedResponse),
//        objectMapper.writeValueAsString(response));
//  }
//
//  @Test
//  void test_should_return_transaction_deregistrations()
//      throws IOException {
//    BlockTransactionRequest request = objectMapper.readValue(new String(Files.readAllBytes(
//            Paths.get(BASE_DIRECTORY
//                + "/request/test_should_return_transaction_deregistrations_request.json"))),
//        BlockTransactionRequest.class);
//
//    var response = restTemplate.postForObject(baseUrl,
//        request, BlockTransactionResponse.class);
//    var expectedResponse = objectMapper.readValue(new String(
//            Files.readAllBytes(
//                Paths.get(BASE_DIRECTORY
//                    + "/response/test_should_return_transaction_deregistrations.json"))),
//        BlockTransactionResponse.class);
//    assert response != null;
//    assertEquals(objectMapper.writeValueAsString(expectedResponse),
//        objectMapper.writeValueAsString(response));
//  }
//
//  @Test
//  void test_should_return_a_pool_retirement_transaction()
//      throws IOException {
//    BlockTransactionRequest request = objectMapper.readValue(new String(Files.readAllBytes(
//            Paths.get(BASE_DIRECTORY
//                + "/request/test_should_return_a_pool_retirement_transaction_request.json"))),
//        BlockTransactionRequest.class);
//
//    var response = restTemplate.postForObject(baseUrl,
//        request, BlockTransactionResponse.class);
//    var expectedResponse = objectMapper.readValue(new String(
//            Files.readAllBytes(
//                Paths.get(BASE_DIRECTORY
//                    + "/response/test_should_return_a_pool_retirement_transaction.json"))),
//        BlockTransactionResponse.class);
//    assert response != null;
//    assertEquals(objectMapper.writeValueAsString(expectedResponse),
//        objectMapper.writeValueAsString(response));
//  }
//
//  @Test
//  void test_should_be_able_to_return_multiasset_token_transactions_with_several_tokens_in_the_bundle()
//      throws IOException {
//    BlockTransactionRequest request = objectMapper.readValue(new String(Files.readAllBytes(
//            Paths.get(BASE_DIRECTORY
//                + "/request/test_should_be_able_to_return_multiasset_token_transactions_with_several_tokens_in_the_bundle_request.json"))),
//        BlockTransactionRequest.class);
//
//    var response = restTemplate.postForObject(baseUrl,
//        request, BlockTransactionResponse.class);
//    var expectedResponse = objectMapper.readValue(new String(
//            Files.readAllBytes(
//                Paths.get(BASE_DIRECTORY
//                    + "/response/test_should_be_able_to_return_multiasset_token_transactions_with_several_tokens_in_the_bundle.json"))),
//        BlockTransactionResponse.class);
//    assert response != null;
//    assertEquals(objectMapper.writeValueAsString(expectedResponse),
//        objectMapper.writeValueAsString(response));
//  }
//
//  @Test
//  void test_should_return_transaction_pool_registrations()
//      throws IOException {
//    BlockTransactionRequest request = objectMapper.readValue(new String(Files.readAllBytes(
//            Paths.get(BASE_DIRECTORY
//                + "/request/test_should_return_transaction_pool_registrations_request.json"))),
//        BlockTransactionRequest.class);
//
//    var response = restTemplate.postForObject(baseUrl,
//        request, BlockTransactionResponse.class);
//    var expectedResponse = objectMapper.readValue(new String(
//            Files.readAllBytes(
//                Paths.get(BASE_DIRECTORY
//                    + "/response/test_should_return_transaction_pool_registrations.json"))),
//        BlockTransactionResponse.class);
//    assert response != null;
//    assertEquals(objectMapper.writeValueAsString(expectedResponse),
//        objectMapper.writeValueAsString(response));
//  }
//
//  @Test
//  void test_should_return_transaction_pool_registrations_with_multiple_owners()
//      throws IOException {
//    BlockTransactionRequest request = objectMapper.readValue(new String(Files.readAllBytes(
//            Paths.get(BASE_DIRECTORY
//                + "/request/test_should_return_transaction_pool_registrations_with_multiple_owners_request.json"))),
//        BlockTransactionRequest.class);
//
//    var response = restTemplate.postForObject(baseUrl,
//        request, BlockTransactionResponse.class);
//    var expectedResponse = objectMapper.readValue(new String(
//            Files.readAllBytes(
//                Paths.get(BASE_DIRECTORY
//                    + "/response/test_should_return_transaction_pool_registrations_with_multiple_owners.json"))),
//        BlockTransactionResponse.class);
//    assert response != null;
//    assertEquals(objectMapper.writeValueAsString(expectedResponse),
//        objectMapper.writeValueAsString(response));
//  }
//
//  @Test
//  void test_should_return_vote_registration_operations()
//      throws IOException {
//    BlockTransactionRequest request = objectMapper.readValue(new String(Files.readAllBytes(
//            Paths.get(BASE_DIRECTORY
//                + "/request/test_should_return_vote_registration_operations_request.json"))),
//        BlockTransactionRequest.class);
//
//    var response = restTemplate.postForObject(baseUrl,
//        request, BlockTransactionResponse.class);
//    var expectedResponse = objectMapper.readValue(new String(
//            Files.readAllBytes(
//                Paths.get(BASE_DIRECTORY
//                    + "/response/test_should_return_vote_registration_operations.json"))),
//        BlockTransactionResponse.class);
//    assert response != null;
//    assertEquals(objectMapper.writeValueAsString(expectedResponse),
//        objectMapper.writeValueAsString(response));
//  }
//
//  @Test
//  void test_should_not_return_a_vote_registration_operation_when_it_is_bad_formed()
//      throws IOException {
//    BlockTransactionRequest request = objectMapper.readValue(new String(Files.readAllBytes(
//            Paths.get(BASE_DIRECTORY
//                + "/request/test_should_not_return_a_vote_registration_operation_when_it_is_bad_formed_request.json"))),
//        BlockTransactionRequest.class);
//
//    var response = restTemplate.postForObject(baseUrl,
//        request, BlockTransactionResponse.class);
//    var expectedResponse = objectMapper.readValue(new String(
//            Files.readAllBytes(
//                Paths.get(BASE_DIRECTORY
//                    + "/response/test_should_not_return_a_vote_registration_operation_when_it_is_bad_formed.json"))),
//        BlockTransactionResponse.class);
//    assert response != null;
//    assertEquals(objectMapper.writeValueAsString(expectedResponse),
//        objectMapper.writeValueAsString(response));
//  }
//}
