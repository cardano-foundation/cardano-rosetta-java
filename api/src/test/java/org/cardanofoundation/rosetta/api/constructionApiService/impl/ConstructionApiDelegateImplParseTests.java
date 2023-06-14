package org.cardanofoundation.rosetta.api.constructionApiService.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.IntegrationTestWithDB;
import org.cardanofoundation.rosetta.api.common.enumeration.OperationType;
import org.cardanofoundation.rosetta.api.model.Operation;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionParseRequest;
import org.cardanofoundation.rosetta.api.model.rest.ConstructionParseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpServerErrorException;

class ConstructionApiDelegateImplParseTests extends IntegrationTest {

  private final String BASE_DIRECTORY = "src/test/resources/files/construction/parse";

  @BeforeEach
  public void setUp() {
    baseUrl = baseUrl.concat(":").concat(String.valueOf(serverPort)).concat("/construction/parse");
  }

  @Test
  void test_should_return_1_input_2_outputs_and_signers_if_a_valid_signed_transaction_is_set()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_return_1_input_2_outputs_and_signers_if_a_valid_signed_transaction_is_set.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);

    int inputCount = 0;
    int outputCount = 0;
    List<Operation> operations = response.getOperations();
    assertTrue(operations.size() > 0);

    for (Operation operation : operations) {
      if (operation.getType().equals(OperationType.INPUT.getValue())) {
        inputCount++;
      } else if (operation.getType().equals(OperationType.OUTPUT.getValue())) {
        outputCount++;
      }
    }

    assertEquals(inputCount, 1);
    assertEquals(outputCount, 2);
    assertTrue(response.getAccountIdentifierSigners().size() > 0);
  }

  @Test
  void test_should_return_1_input_with_byron_address_and_signers_if_a_valid_signed_transaction_is_set()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_return_1_input_with_byron_address_and_signers_if_a_valid_signed_transaction_is_set.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);

    int inputCount = 0;
    int outputCount = 0;
    List<Operation> operations = response.getOperations();
    assertTrue(operations.size() > 0);

    for (Operation operation : operations) {
      if (operation.getType().equals(OperationType.INPUT.getValue())) {
        inputCount++;
      } else if (operation.getType().equals(OperationType.OUTPUT.getValue())) {
        outputCount++;
      }
    }

    assertEquals(inputCount, 1);
    assertEquals(outputCount, 2);
    assertTrue(response.getAccountIdentifierSigners().size() > 0);
  }

  @Test
  void test_should_return_valid_data_if_a_valid_signed_transaction_with_a_byron_address_is_set()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_return_valid_data_if_a_valid_signed_transaction_with_a_byron_address_is_set.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);

    int inputCount = 0;
    int outputCount = 0;
    List<Operation> operations = response.getOperations();
    assertTrue(operations.size() > 0);

    for (Operation operation : operations) {
      if (operation.getType().equals(OperationType.INPUT.getValue())) {
        inputCount++;
      } else if (operation.getType().equals(OperationType.OUTPUT.getValue())) {
        outputCount++;
      }
    }

    assertEquals(inputCount, 1);
    assertEquals(outputCount, 2);
    assertTrue(response.getAccountIdentifierSigners().size() > 0);
  }

  @Test
  void test_should_return_1_input_2_outputs_1_stake_key_registration_and_signers_with_payment_and_stake_addresses()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_return_1_input_2_outputs_1_stake_key_registration_and_signers_with_payment_and_stake_addresses.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);

    int inputCount = 0;
    int outputCount = 0;
    int stakeKeyRegistrationOutput = 0;
    List<Operation> operations = response.getOperations();
    assertTrue(operations.size() > 0);

    for (Operation operation : operations) {
      if (operation.getType().equals(OperationType.INPUT.getValue())) {
        inputCount++;
      } else if (operation.getType().equals(OperationType.OUTPUT.getValue())) {
        outputCount++;
      } else if (operation.getType().equals(OperationType.STAKE_KEY_REGISTRATION.getValue())) {
        stakeKeyRegistrationOutput++;
      }
    }

    assertEquals(inputCount, 1);
    assertEquals(outputCount, 2);
    assertEquals(stakeKeyRegistrationOutput, 1);
  }

  @Test
  void test_should_return_1_input_2_outputs_1_stake_key_registration_1_withdrawal_and_signers_addresses_should_be_unique()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_return_1_input_2_outputs_1_stake_key_registration_1_withdrawal_and_signers_addresses_should_be_unique.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);

    int inputCount = 0;
    int outputCount = 0;
    int stakeKeyRegistrationOutput = 0;
    int withdrawalCount = 0;
    List<Operation> operations = response.getOperations();
    assertTrue(operations.size() > 0);

    for (Operation operation : operations) {
      if (operation.getType().equals(OperationType.INPUT.getValue())) {
        inputCount++;
      } else if (operation.getType().equals(OperationType.OUTPUT.getValue())) {
        outputCount++;
      } else if (operation.getType().equals(OperationType.WITHDRAWAL.getValue())) {
        stakeKeyRegistrationOutput++;
      } else if (operation.getType().equals(OperationType.STAKE_KEY_REGISTRATION.getValue())) {
        withdrawalCount++;
      }
    }

    assertEquals(inputCount, 1);
    assertEquals(outputCount, 2);
    assertEquals(withdrawalCount, 1);
    assertEquals(stakeKeyRegistrationOutput, 1);
    assertEquals(stakeKeyRegistrationOutput, 1);
  }

  @Test
  void test_should_return_1_input_2_outputs_and_empty_signers_if_a_valid_unsigned_transaction_is_set()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_return_1_input_2_outputs_and_empty_signers_if_a_valid_unsigned_transaction_is_set.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);

    int inputCount = 0;
    int outputCount = 0;

    List<Operation> operations = response.getOperations();
    assertTrue(operations.size() > 0);

    for (Operation operation : operations) {
      if (operation.getType().equals(OperationType.INPUT.getValue())) {
        inputCount++;
      } else if (operation.getType().equals(OperationType.OUTPUT.getValue())) {
        outputCount++;
      }
    }

    assertEquals(inputCount, 1);
    assertEquals(outputCount, 2);
    assertTrue(response.getAccountIdentifierSigners().isEmpty());
  }

  @Test
  void test_should_return_1_input_2_outputs_1_stake_key_registration_and_empty_signers_if_a_valid_unsigned_transaction_is_set()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_return_1_input_2_outputs_1_stake_key_registration_and_empty_signers_if_a_valid_unsigned_transaction_is_set.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);

    int inputCount = 0;
    int outputCount = 0;
    int stakeKeyRegistrationCount = 0;
    List<Operation> operations = response.getOperations();
    assertTrue(operations.size() > 0);

    for (Operation operation : operations) {
      if (operation.getType().equals(OperationType.INPUT.getValue())) {
        inputCount++;
      } else if (operation.getType().equals(OperationType.OUTPUT.getValue())) {
        outputCount++;
      } else if (operation.getType().equals(OperationType.STAKE_KEY_REGISTRATION.getValue())) {
        stakeKeyRegistrationCount++;
      }
    }

    assertEquals(inputCount, 1);
    assertEquals(outputCount, 2);
    assertEquals(stakeKeyRegistrationCount, 1);
    assertTrue(response.getAccountIdentifierSigners().isEmpty());
  }

  @Test
  void test_should_return_1_input_2_outputs_1_stake_key_deregistration_and_empty_signers_if_a_valid_unsigned_transaction_is_set()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_return_1_input_2_outputs_1_stake_key_deregistration_and_empty_signers_if_a_valid_unsigned_transaction_is_set.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);

    int inputCount = 0;
    int outputCount = 0;
    int stakeKeyDeregistration = 0;
    List<Operation> operations = response.getOperations();
    assertTrue(operations.size() > 0);

    for (Operation operation : operations) {
      if (operation.getType().equals(OperationType.INPUT.getValue())) {
        inputCount++;
      } else if (operation.getType().equals(OperationType.OUTPUT.getValue())) {
        outputCount++;
      } else if (operation.getType().equals(OperationType.STAKE_KEY_DEREGISTRATION.getValue())) {
        stakeKeyDeregistration++;
      }
    }

    assertEquals(inputCount, 1);
    assertEquals(outputCount, 2);
    assertEquals(stakeKeyDeregistration, 1);
    assertTrue(response.getAccountIdentifierSigners().isEmpty());
  }

  @Test
  void test_should_return_1_input_2_outputs_1_stake_delegation_and_empty_signers_if_a_valid_unsigned_transaction_is_set()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_return_1_input_2_outputs_1_stake_delegation_and_empty_signers_if_a_valid_unsigned_transaction_is_set.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);

    int inputCount = 0;
    int outputCount = 0;
    int stakeDelegationCount = 0;
    List<Operation> operations = response.getOperations();
    assertTrue(operations.size() > 0);

    for (Operation operation : operations) {
      if (operation.getType().equals(OperationType.INPUT.getValue())) {
        inputCount++;
      } else if (operation.getType().equals(OperationType.OUTPUT.getValue())) {
        outputCount++;
      } else if (operation.getType().equals(OperationType.STAKE_DELEGATION.getValue())) {
        stakeDelegationCount++;
      }
    }

    assertEquals(inputCount, 1);
    assertEquals(outputCount, 2);
    assertEquals(stakeDelegationCount, 1);
    assertTrue(response.getAccountIdentifierSigners().isEmpty());
  }

  @Test
  void test_should_return_1_input_2_outputs_1_stake_key_registration_1_stake_delegation_and_empty_signers_if_a_valid_unsigned_transaction_is_set()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_return_1_input_2_outputs_1_stake_key_registration_1_stake_delegation_and_empty_signers_if_a_valid_unsigned_transaction_is_set.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);

    int inputCount = 0;
    int outputCount = 0;
    int stakeKeyRegistration = 0;
    int stakeDelegationCount = 0;
    List<Operation> operations = response.getOperations();
    assertTrue(operations.size() > 0);

    for (Operation operation : operations) {
      if (operation.getType().equals(OperationType.INPUT.getValue())) {
        inputCount++;
      } else if (operation.getType().equals(OperationType.OUTPUT.getValue())) {
        outputCount++;
      } else if (operation.getType().equals(OperationType.STAKE_DELEGATION.getValue())) {
        stakeDelegationCount++;
      } else if (operation.getType().equals(OperationType.STAKE_KEY_REGISTRATION.getValue())) {
        stakeKeyRegistration++;
      }
    }

    assertEquals(inputCount, 1);
    assertEquals(outputCount, 2);
    assertEquals(stakeDelegationCount, 1);
    assertEquals(stakeKeyRegistration, 1);
    assertTrue(response.getAccountIdentifierSigners().isEmpty());
  }

  @Test
  void test_should_return_1_input_2_outputs_1_withdrawal_and_empty_signers_if_a_valid_unsigned_transaction_is_set()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_return_1_input_2_outputs_1_withdrawal_and_empty_signers_if_a_valid_unsigned_transaction_is_set.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);

    int inputCount = 0;
    int outputCount = 0;
    int withdrawal = 0;
    List<Operation> operations = response.getOperations();
    assertTrue(operations.size() > 0);

    for (Operation operation : operations) {
      if (operation.getType().equals(OperationType.INPUT.getValue())) {
        inputCount++;
      } else if (operation.getType().equals(OperationType.OUTPUT.getValue())) {
        outputCount++;
      } else if (operation.getType().equals(OperationType.WITHDRAWAL.getValue())) {
        withdrawal++;
      }
    }

    assertEquals(inputCount, 1);
    assertEquals(outputCount, 2);
    assertEquals(withdrawal, 1);
    assertTrue(response.getAccountIdentifierSigners().isEmpty());
  }

  @Test
  void test_should_return_1_input_2_outputs_1_stake_key_registration_1_withdrawal_and_empty_signers_if_a_valid_unsigned_transaction_is_set()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_return_1_input_2_outputs_1_stake_key_registration_1_withdrawal_and_empty_signers_if_a_valid_unsigned_transaction_is_set.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);

    int inputCount = 0;
    int outputCount = 0;
    int withdrawal = 0;
    int stakeKeyRegistration = 0;
    List<Operation> operations = response.getOperations();
    assertTrue(operations.size() > 0);

    for (Operation operation : operations) {
      if (operation.getType().equals(OperationType.INPUT.getValue())) {
        inputCount++;
      } else if (operation.getType().equals(OperationType.OUTPUT.getValue())) {
        outputCount++;
      } else if (operation.getType().equals(OperationType.WITHDRAWAL.getValue())) {
        withdrawal++;
      } else if (operation.getType().equals(OperationType.STAKE_KEY_REGISTRATION.getValue())) {
        stakeKeyRegistration++;
      }
    }

    assertEquals(inputCount, 1);
    assertEquals(outputCount, 2);
    assertEquals(withdrawal, 1);
    assertEquals(stakeKeyRegistration, 1);
    assertTrue(response.getAccountIdentifierSigners().isEmpty());
  }

  @Test
  void test_should_throw_an_error_when_invalid_signed_transaction_bytes_are_provided()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_throw_an_error_when_invalid_signed_transaction_bytes_are_provided.json"))),
        ConstructionParseRequest.class);
    try {
      ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
          request, ConstructionParseResponse.class);
    } catch (HttpServerErrorException e) {
      assertEquals(500, e.getRawStatusCode());
      assertTrue(e.getResponseBodyAsString().contains("4011"));
    }
  }

  @Test
  void test_should_throw_an_error_when_invalid_unsigned_transaction_bytes_are_provided()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_throw_an_error_when_invalid_unsigned_transaction_bytes_are_provided.json"))),
        ConstructionParseRequest.class);
    try {
      ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
          request, ConstructionParseResponse.class);
    } catch (HttpServerErrorException e) {
      assertEquals(500, e.getRawStatusCode());
      assertTrue(e.getResponseBodyAsString().contains("4012"));
    }
  }

  @Test
  void test_should_throw_an_error_when_valid_unsigned_transaction_bytes_but_signed_flag_is_true_are_provided()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_throw_an_error_when_valid_unsigned_transaction_bytes_but_signed_flag_is_true_are_provided.json"))),
        ConstructionParseRequest.class);
    try {
      ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
          request, ConstructionParseResponse.class);
    } catch (HttpServerErrorException e) {
      assertEquals(500, e.getRawStatusCode());
      assertTrue(e.getResponseBodyAsString().contains("4011"));
    }
  }

  @Test
  void test_should_throw_an_error_when_valid_signed_transaction_bytes_but_signed_flag_is_false_are_provided()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_throw_an_error_when_valid_unsigned_transaction_bytes_but_signed_flag_is_true_are_provided.json"))),
        ConstructionParseRequest.class);
    try {
      ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
          request, ConstructionParseResponse.class);
    } catch (HttpServerErrorException e) {
      assertEquals(500, e.getRawStatusCode());
      assertTrue(e.getResponseBodyAsString().contains("4011"));
    }
  }

  @Test
  void test_should_return_1_input_and_2_outputs_first_input_and_output_with_multiassets()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_return_1_input_and_2_outputs_first_input_and_output_with_multiassets.json"))),
        ConstructionParseRequest.class);
    try {
      ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
          request, ConstructionParseResponse.class);
    } catch (HttpServerErrorException e) {
      assertEquals(500, e.getRawStatusCode());
      assertTrue(e.getResponseBodyAsString().contains("4012"));
    }
  }

  @Test
  void test_should_correctly_parse_operations_with_two_multiassets() throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_correctly_parse_operations_with_two_multiassets.json"))),
        ConstructionParseRequest.class);

    restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);

  }

  @Test
  void test_should_correctly_parse_operations_with_several_multiassets() throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_correctly_parse_operations_with_several_multiassets.json"))),
        ConstructionParseRequest.class);

    restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_correctly_parse_operations_with_multiassets_without_name() throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_correctly_parse_operations_with_multiassets_without_name.json"))),
        ConstructionParseRequest.class);

    restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_correctly_parse_operations_with_pool_registrations_with_pledge()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_correctly_parse_operations_with_pool_registrations_with_pledge.json"))),
        ConstructionParseRequest.class);

    restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_correctly_parse_operations_with_pool_registrations_with_multiple_relays()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/should_correctly_parse_operations_with_pool_registrations_with_multiple_relays.json"))),
        ConstructionParseRequest.class);

    restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_correctly_parse_operations_with_pool_registrations_with_no_pool_metadata()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/should_correctly_parse_operations_with_pool_registrations_with_no_pool_metadata.json"))),
        ConstructionParseRequest.class);

    restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_correctly_parse_operations_with_pool_registrations_with_cert()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/constructionn_parse_should_correctly_parse_operations_with_pool_registrations_with_cert.json"))),
        ConstructionParseRequest.class);

    restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_return_1_input_2_outputs_1_pool_retirement_and_empty_signers_if_a_valid_unsigned_transaction_is_set()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_return_1_input_2_outputs_1_pool_retirement_and_empty_signers_if_a_valid_unsigned_transaction_is_set.json"))),
        ConstructionParseRequest.class);

    ConstructionParseResponse response = restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);

    int inputCount = 0;
    int poolRetirementCount = 0;
    List<Operation> operations = response.getOperations();
    assertTrue(operations.size() > 0);

    for (Operation operation : operations) {
      if (operation.getType().equals(OperationType.INPUT.getValue())) {
        inputCount++;
      } else if (operation.getType().equals(OperationType.POOL_RETIREMENT.getValue())) {
        poolRetirementCount++;
      }
    }

    assertEquals(inputCount, 1);
    assertEquals(poolRetirementCount, 1);
  }

  @Test
  void test_should_correctly_parse_operations_with_vote_registration_data_for_signed_transactions()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/constructionn_parse_should_correctly_parse_operations_with_vote_registration_data_for_signed_transactions.json"))),
        ConstructionParseRequest.class);

    restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_correctly_parse_operations_with_vote_registration_data_for_unsigned_transactions()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/constructionn_parse_should_correctly_parse_operations_with_vote_registration_data_for_unsigned_transactions.json"))),
        ConstructionParseRequest.class);

    restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_correctly_parse_operations_with_related_operation()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_correctly_parse_operations_with_related_operation.json"))),
        ConstructionParseRequest.class);

    restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_correctly_parse_operations_with_sub_account()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_correctly_parse_operations_with_sub_account.json"))),
        ConstructionParseRequest.class);

    restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }
  @Test
  void test_should_correctly_parse_operations_with_amount_withdrawal_metadata()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_correctly_parse_operations_with_withdrawal.json"))),
        ConstructionParseRequest.class);

    restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_correctly_parse_operations_with_amount_refund_metadata()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_correctly_parse_operations_with_refund.json"))),
        ConstructionParseRequest.class);

    restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }

  @Test
  void test_should_correctly_parse_operations_with_amount_deposit_metadata()
      throws IOException {
    ConstructionParseRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(
                BASE_DIRECTORY
                    + "/construction_parse_should_correctly_parse_operations_with_deposit.json"))),
        ConstructionParseRequest.class);

    restTemplate.postForObject(baseUrl,
        request, ConstructionParseResponse.class);
  }
}
