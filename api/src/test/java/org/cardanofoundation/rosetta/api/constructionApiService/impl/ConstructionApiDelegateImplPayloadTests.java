package org.cardanofoundation.rosetta.api.constructionApiService.impl;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionDeriveResponse;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionPayloadsRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionPayloadsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpServerErrorException;

class ConstructionApiDelegateImplPayloadTests extends IntegrationTest {

  private static final String INVALID_OPERATION_TYPE_ERROR_MESSAGE = "invalidOperationTypeError";

  private static final String BASE_DIRECTORY = "src/test/resources/files/construction/payload";

  @BeforeEach
  public void setUp() {
    baseUrl = baseUrl.concat(":").concat(serverPort + "").concat("/construction/payloads");
  }

  @Test
  void test_send_valid_input_and_output_operations() throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY + "/construction_payloads_request_valid.json"))),
        ConstructionPayloadsRequest.class);

    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionPayloadsResponse.class);

    assertEquals(response.getPayloads().get(0).getAccountIdentifier().getAddress(),
        "addr1vxa5pudxg77g3sdaddecmw8tvc6hmynywn49lltt4fmvn7cpnkcpx");
    assertEquals(response.getPayloads().get(0).getHexBytes(),
        "333a6ccaaa639f7b451ce93764f54f654ef499fdb7b8b24374ee9d99eab9d795");
  }

  @Test
  void test_send_a_input_with_Byron_Address() throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY + "/construction_payloads_request_with_byron_input.json"))),
        ConstructionPayloadsRequest.class);

    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionPayloadsResponse.class);

    assertEquals(response.getPayloads().get(0).getAccountIdentifier().getAddress(),
        "Ae2tdPwUPEZC6WJfVQxTNN2tWw4skGrN6zRVukvxJmTFy1nYkVGQBuURU3L");
    assertEquals(response.getPayloads().get(0).getHexBytes(),
        "333a6ccaaa639f7b451ce93764f54f654ef499fdb7b8b24374ee9d99eab9d795");
  }

  @Test
  void test_receive_single_payload_for_each_input_address() throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY + "/construction_payload_multiple_inputs.json"))),
        ConstructionPayloadsRequest.class);

    ConstructionPayloadsResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionPayloadsResponse.class);

    assertEquals(response.getPayloads().size(), 1);

  }

  @Test
  void test_return_an_error_when_operations_with_invalid_types() throws IOException {
    ConstructionPayloadsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY + "/construction_payloads_invalid_operation_type.json"))),
        ConstructionPayloadsRequest.class);

    try {
      restTemplate.postForObject(baseUrl, request, ConstructionDeriveResponse.class);
      fail("Expected exception");
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      assertTrue(responseBody.contains(INVALID_OPERATION_TYPE_ERROR_MESSAGE));
      assertEquals(500, e.getRawStatusCode());
    }
  }

  @Test
  void test_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_including_stake_key_registration() {

  }

  @Test
  void test_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_including_stake_delegation() {

  }

  @Test
  void test_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_including_stake_key_registration_stake_delegation() {

  }

  @Test
  void test_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_including_withdraw() {

  }

  @Test
  void test_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_including_withdraw_and_stake_registration() {

  }

  @Test
  void test_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_including_stake_key_deregistration() {

  }

  @Test
  void test_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_including_pool_retirement() {

  }

  @Test
  void test_should_throw_an_error_when_no_epoch_was_sent_on_pool_retirement_operation() {

  }

  @Test
  void test_return_an_error_when_no_staking_key_is_provided_in_staking_key_registration_operation() {

  }

  @Test
  void test_return_an_error_when_staking_key_in_one_operation_has_invalid_format() {

  }

  @Test
  void test_return_an_error_when_staking_key_in_one_operation_has_a_bigger_length_than_32() {

  }

  @Test
  void test_return_an_error_when_staking_key_in_one_operation_has_a_smaller_length_than_32() {

  }

  @Test
  void test_return_an_error_when_no_pool_key_hash_is_provided_for_stake_delegation() {

  }

  @Test
  void test_return_an_error_when_an_invalid_pool_key_hash_is_provided_for_stake_delegation() {

  }

  @Test
  void test_should_return_a_valid_unsigned_transaction_hash_when_sending_valid_operation_including_ma_amount() {

  }

  @Test
  void test_should_fail_if_MultiAsset_policy_id_is_shorter_than_expected() {

  }

  @Test
  void test_should_fail_if_MultiAsset_policy_id_is_not_a_hex_string() {

  }

  @Test
  void test_should_fail_if_MultiAsset_policy_id_is_longer_than_expected() {

  }

  @Test
  void test_should_fail_if_MultiAsset_symbol_is_not_a_hex_string() {

  }

  @Test
  void test_should_fail_if_MultiAsset_symbol_longer_than_expected() {

  }

  @Test
  void test_should_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_with_pool_registration_with_pledge() {

  }

  @Test
  void test_should_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_including_pool_registration_with_Single_Host_Addr_relay() {

  }

  @Test
  void test_should_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_including_pool_registration_with_Single_Host_Name_relay() {

  }

  @Test
  void test_should_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_including_pool_registration_with_multi_host_name_relay() {

  }

  @Test
  void test_should_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_including_pool_registration_with_no_pool_metadata() {

  }

  @Test
  void test_should_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_including_pool_registration_with_multiple_relay() {

  }

  @Test
  void test_should_throw_an_error_when_there_are_oprations_in() {

  }

  @Test
  void test_should_throw_error_when_operations_include_pool_registration_with_invalid_cold_key_hash() {
    // Test code here
  }

  @Test
  void test_should_throw_error_when_operations_include_pool_registration_with_missing_cold_key_hash() {

  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_empty_pool_relays() {

  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_invalid_pool_relay_type() {

  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_missing_pool_relay_type() {

  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_invalid_pool_relays_with_invalid_ipv4() {

  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_invalid_pool_relays_with_invalid_port() {

  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_pool_relays_with_invalid_pool_metadata_hash() {

  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_pool_relays_with_invalid_pool_owners() {

  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_negative_cost() {

  }

  @Test
  void should_throw_an_error_when_there_are_operations_including_pool_registration_with_pool_relays_with_negative_pledge() {

  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_pool_relays_with_negative_pledge() {

  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_pool_relays_with_negative_denominator() {

  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_pool_relays_with_alphabetical_numerator() {

  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_no_margin() {

  }

  @Test
  void test_should_throw_an_error_when_there_are_operations_including_pool_registration_with_invalid_reward_address() {

  }

  @Test
  void test_should_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_with_pool_registration_with_cert() {

  }

  @Test
  void test_should_throw_an_error_when_sending_operations_with_pool_registration_with_invalid_cert() {

  }

  @Test
  void test_should_throw_an_error_when_sending_operations_with_pool_registration_with_invalid_cert_type() {

  }

  @Test
  void test_should_return_a_valid_unsigned_transaction_hash_when_sending_valid_operations_with_a_vote_registration() {

  }

  @Test
  void test_should_throw_an_error_when_the_voting_key_is_empty() {

  }

  @Test
  void test_should_throw_an_error_when_the_voting_key_is_not_valid() {

  }

  @Test
  void test_should_throw_an_error_when_the_reward_address_is_empty() {

  }

  @Test
  void test_should_throw_an_error_when_the_reward_address_is_not_valid() {

  }

  @Test
  void test_should_throw_an_error_when_the_stake_key_is_empty() {

  }

  @Test
  void test_should_throw_an_error_when_the_stake_key_is_not_valid() {

  }

  @Test
  void test_should_throw_an_error_when_the_voting_nonce_is_not_greater_than_zero() {

  }

  @Test
  void test_should_throw_an_error_when_the_voting_signature_is_empty() {

  }

  @Test
  void test_should_throw_an_error_when_the_voting_signature_is_not_valid() {

  }

  @Test
  void test_should_throw_an_error_when_the_transaction_has_no_metadata() {

  }

  @Test
  void test_should_throw_an_error_when_there_is_no_vote_registration_metadata() {

  }
}
