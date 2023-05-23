package org.cardanofoundation.rosetta.api.constructionApiService.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionParseRequest;
import org.cardanofoundation.rosetta.crawler.model.rest.ConstructionParseResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpServerErrorException;

class ConstructionApiDelegateImplParseTests extends IntegrationTest {

  private static final String BASE_DIRECTORY = "src/test/resources/files/construction/parse";

  @Test
  void test_should_return_1_input_2_outputs_and_signers_if_a_valid_signed_transaction_is_set()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_return_1_input_with_byron_address_and_signers_if_a_valid_signed_transaction_is_set()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_return_valid_data_if_a_valid_signed_transaction_with_a_byron_address_is_set()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_return_1_input_2_outputs_1_stake_key_registration_and_signers_with_payment_and_stake_addresses()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_return_1_input_2_outputs_1_stake_key_registration_1_withdrawal_and_signers_addresses_should_be_unique()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_return_1_input_2_outputs_and_empty_signers_if_a_valid_unsigned_transaction_is_set()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_return_1_input_2_outputs_1_stake_key_registration_and_empty_signers_if_a_valid_unsigned_transaction_is_set()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_return_1_input_2_outputs_1_stake_key_deregistration_and_empty_signers_if_a_valid_unsigned_transaction_is_set()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_return_1_input_2_outputs_1_stake_delegation_and_empty_signers_if_a_valid_unsigned_transaction_is_set()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_return_1_input_2_outputs_1_stake_key_registration_1_stake_delegation_and_empty_signers_if_a_valid_unsigned_transaction_is_set()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_return_1_input_2_outputs_1_withdrawal_and_empty_signers_if_a_valid_unsigned_transaction_is_set()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_return_1_input_2_outputs_1_stake_key_registration_1_withdrawal_and_empty_signers_if_a_valid_unsigned_transaction_is_set()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_throw_an_error_when_invalid_signed_transaction_bytes_are_provided()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    try {
      ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
          request, ConstructionParseResponse.class);
    } catch (HttpServerErrorException e) {

    }
  }

  @Test
  void test_should_throw_an_error_when_invalid_unsigned_transaction_bytes_are_provided()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    try {
      ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
          request, ConstructionParseResponse.class);
    } catch (HttpServerErrorException e) {

    }
  }

  @Test
  void test_should_throw_an_error_when_valid_unsigned_transaction_bytes_but_signed_flag_is_true_are_provided()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    try {
      ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
          request, ConstructionParseResponse.class);
    } catch (HttpServerErrorException e) {

    }
  }

  @Test
  void test_should_throw_an_error_when_valid_signed_transaction_bytes_but_signed_flag_is_false_are_provided()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    try {
      ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
          request, ConstructionParseResponse.class);
    } catch (HttpServerErrorException e) {

    }
  }

  @Test
  void test_should_return_1_input_and_2_outputs_first_input_and_output_with_multiassets()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_correctly_parse_operations_with_two_multiassets() throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_correctly_parse_operations_with_several_multiassets() throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_correctly_parse_operations_with_multiassets_without_name() throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_correctly_parse_operations_with_pool_registrations_with_pledge()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_correctly_parse_operations_with_pool_registrations_with_multiple_relays()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_correctly_parse_operations_with_pool_registrations_with_no_pool_metadata()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_correctly_parse_operations_with_pool_registrations_with_cert()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_return_1_input_2_outputs_1_pool_retirement_and_empty_signers_if_a_valid_unsigned_transaction_is_set()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_correctly_parse_operations_with_vote_registration_data_for_signed_transactions()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_correctly_parse_operations_with_vote_registration_data_for_unsigned_transactions()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_payload_valid_operations_including_stake_delegation.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }
}
