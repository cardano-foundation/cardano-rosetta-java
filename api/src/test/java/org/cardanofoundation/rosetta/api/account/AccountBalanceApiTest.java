package org.cardanofoundation.rosetta.api.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.cardanofoundation.rosetta.api.IntegrationTestWithDB;
import org.cardanofoundation.rosetta.api.exception.Error;
import org.cardanofoundation.rosetta.api.model.rest.AccountBalanceRequest;
import org.cardanofoundation.rosetta.api.model.rest.AccountBalanceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpServerErrorException;

public class AccountBalanceApiTest extends IntegrationTestWithDB {

  private static final String ENDPOINT = "/account/balance";
  private static final String NETWORK = "mainnet";
  private final String BASE_DIRECTORY = "src/test/resources/accountBalance";

  @BeforeEach
  public void setUp() {
    baseUrl = baseUrl.concat(":").concat(serverPort + "").concat(ENDPOINT);
  }

  @Test
  void test_return_all_utxos_until_last_block_if_no_block_number_is_specified() throws IOException {
    AccountBalanceRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_return_all_utxos_until_last_block_if_no_block_number_is_specified_request.json"))),
        AccountBalanceRequest.class);
    var response = restTemplate.postForObject(baseUrl,
        request, AccountBalanceResponse.class);
    var expectedResponse = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_return_all_utxos_until_last_block_if_no_block_number_is_specified.json"))),
        AccountBalanceResponse.class);
    assert response != null;
    assertEquals(objectMapper.writeValueAsString(expectedResponse),
        objectMapper.writeValueAsString(response));
  }

  @Test
  void test_should_only_consider_balance_up_to_block_number_if_specified() throws IOException {
    AccountBalanceRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_should_only_consider_balance_up_to_block_number_if_specified_request.json"))),
        AccountBalanceRequest.class);
    var response = restTemplate.postForObject(baseUrl,
        request, AccountBalanceResponse.class);
    var expectedResponse = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_should_only_consider_balance_up_to_block_number_if_specified.json"))),
        AccountBalanceResponse.class);
    assert response != null;
    assertEquals(objectMapper.writeValueAsString(expectedResponse),
        objectMapper.writeValueAsString(response));
  }

  @Test
  void test_should_only_consider_balance_up_to_block_identifier_if_specified() throws IOException {
    AccountBalanceRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_should_only_consider_balance_up_to_block_identifier_if_specified_request.json"))),
        AccountBalanceRequest.class);
    var response = restTemplate.postForObject(baseUrl,
        request, AccountBalanceResponse.class);
    var expectedResponse = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_should_only_consider_balance_up_to_block_identifier_if_specified.json"))),
        AccountBalanceResponse.class);
    assert response != null;
    assertEquals(objectMapper.writeValueAsString(expectedResponse),
        objectMapper.writeValueAsString(response));
  }

  @Test
  void test_should_use_both_identifier_and_number_if_specified() throws IOException {
    AccountBalanceRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_should_use_both_identifier_and_number_if_specified_request.json"))),
        AccountBalanceRequest.class);
    var response = restTemplate.postForObject(baseUrl,
        request, AccountBalanceResponse.class);
    var expectedResponse = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_should_use_both_identifier_and_number_if_specified.json"))),
        AccountBalanceResponse.class);
    assert response != null;
    assertEquals(objectMapper.writeValueAsString(expectedResponse),
        objectMapper.writeValueAsString(response));
  }

  @Test
  void test_should_fail_if_specified_block_number_and_hash_dont_match() throws IOException {
    AccountBalanceRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_should_fail_if_specified_block_number_and_hash_dont_match_request.json"))),
        AccountBalanceRequest.class);

    try {
      var response = restTemplate.postForObject(baseUrl,
          request, AccountBalanceResponse.class);
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
  void test_should_only_consider_balance_till_latest_block() throws IOException {
    AccountBalanceRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_should_only_consider_balance_till_latest_block_request.json"))),
        AccountBalanceRequest.class);
    var response = restTemplate.postForObject(baseUrl,
        request, AccountBalanceResponse.class);
    var expectedResponse = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_should_only_consider_balance_till_latest_block.json"))),
        AccountBalanceResponse.class);
    assert response != null;
    assertEquals(objectMapper.writeValueAsString(expectedResponse),
        objectMapper.writeValueAsString(response));
  }

  @Test
  void test_should_return_empty_if_address_doesnt_exist() throws IOException {
    AccountBalanceRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_should_return_empty_if_address_doesnt_exist_request.json"))),
        AccountBalanceRequest.class);
    try {
      var response = restTemplate.postForObject(baseUrl,
          request, AccountBalanceResponse.class);
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      Error error = objectMapper.readValue(responseBody, Error.class);
      assertEquals(4015, error.getCode());
      assertTrue(error.isRetriable());

    }
  }

  @Test
  void test_should_only_consider_balance_till_block_3337_and_balance_should_not_be_0()
      throws IOException {
    AccountBalanceRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_should_only_consider_balance_till_block_3337_and_balance_should_not_be_0_request.json"))),
        AccountBalanceRequest.class);
    var response = restTemplate.postForObject(baseUrl,
        request, AccountBalanceResponse.class);
    var expectedResponse = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_should_only_consider_balance_till_block_3337_and_balance_should_not_be_0.json"))),
        AccountBalanceResponse.class);
    assert response != null;
    assertEquals(objectMapper.writeValueAsString(expectedResponse),
        objectMapper.writeValueAsString(response));
  }

  @Test
  void test_should_only_consider_balance_till_last_block_and_balance_should_be_0()
      throws IOException {
    AccountBalanceRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_should_only_consider_balance_till_last_block_and_balance_should_be_0_request.json"))),
        AccountBalanceRequest.class);
    var response = restTemplate.postForObject(baseUrl,
        request, AccountBalanceResponse.class);
    var expectedResponse = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_should_only_consider_balance_till_last_block_and_balance_should_be_0.json"))),
        AccountBalanceResponse.class);
    assert response != null;
    assertEquals(objectMapper.writeValueAsString(expectedResponse),
        objectMapper.writeValueAsString(response));
  }

  @Test
  void test_should_return_0_for_the_balance_of_stake_account_at_block_with_no_earned_rewards()
      throws IOException {
    AccountBalanceRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_should_return_0_for_the_balance_of_stake_account_at_block_with_no_earned_rewards_request.json"))),
        AccountBalanceRequest.class);
    var response = restTemplate.postForObject(baseUrl,
        request, AccountBalanceResponse.class);
    var expectedResponse = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_should_return_0_for_the_balance_of_stake_account_at_block_with_no_earned_rewards.json"))),
        AccountBalanceResponse.class);
    assert response != null;
    assertEquals(objectMapper.writeValueAsString(expectedResponse),
        objectMapper.writeValueAsString(response));
  }

  @Test
  void test_should_sum_all_rewards_and_subtract_all_withdrawals_till_block_4853177()
      throws IOException {
    AccountBalanceRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_should_sum_all_rewards_and_subtract_all_withdrawals_till_block_4853177_request.json"))),
        AccountBalanceRequest.class);
    var response = restTemplate.postForObject(baseUrl,
        request, AccountBalanceResponse.class);
    var expectedResponse = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_should_sum_all_rewards_and_subtract_all_withdrawals_till_block_4853177.json"))),
        AccountBalanceResponse.class);
    assert response != null;
    assertEquals(objectMapper.writeValueAsString(expectedResponse),
        objectMapper.writeValueAsString(response));
  }

  @Test
  void test_should_return_payment_balance_and_list_of_ma_balances() throws IOException {
    AccountBalanceRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_should_return_payment_balance_and_list_of_ma_balances_request.json"))),
        AccountBalanceRequest.class);
    var response = restTemplate.postForObject(baseUrl,
        request, AccountBalanceResponse.class);
    var expectedResponse = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_should_return_payment_balance_and_list_of_ma_balances.json"))),
        AccountBalanceResponse.class);
    assert response != null;
    assertEquals(objectMapper.writeValueAsString(expectedResponse),
        objectMapper.writeValueAsString(response));
  }

  @Test
  void test_token_balance_should_not_be_seen_at_the_address_balance_for_the_next_block()
      throws IOException {
    AccountBalanceRequest requestAtBlock5406841 = objectMapper.readValue(
        new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_token_balance_should_not_be_seen_at_the_address_balance_for_the_next_block_request_AtBlock5406841.json"))),
        AccountBalanceRequest.class);
    AccountBalanceRequest requestAtBlock5406842 = objectMapper.readValue(
        new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_token_balance_should_not_be_seen_at_the_address_balance_for_the_next_block_request_AtBlock5406842.json"))),
        AccountBalanceRequest.class);
    var responseAtBlock5406841 = restTemplate.postForObject(baseUrl,
        requestAtBlock5406841, AccountBalanceResponse.class);
    var responseAtBlock5406842 = restTemplate.postForObject(baseUrl,
        requestAtBlock5406842, AccountBalanceResponse.class);
    var expectedResponseAtBlock5406841 = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_token_balance_should_not_be_seen_at_the_address_balance_for_the_next_block_AtBlock5406841.json"))),
        AccountBalanceResponse.class);
    var expectedResponseAtBlock5406842 = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_token_balance_should_not_be_seen_at_the_address_balance_for_the_next_block_AtBlock5406842.json"))),
        AccountBalanceResponse.class);
    assert responseAtBlock5406841 != null;
    assertEquals(objectMapper.writeValueAsString(expectedResponseAtBlock5406841),
        objectMapper.writeValueAsString(responseAtBlock5406841));
    assert responseAtBlock5406842 != null;
    assertEquals(objectMapper.writeValueAsString(expectedResponseAtBlock5406842),
        objectMapper.writeValueAsString(responseAtBlock5406842));
  }
  @Test
  void test_should_return_balances_for_ma_with_empty_name() throws IOException {
    AccountBalanceRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_should_return_balances_for_ma_with_empty_name_request.json"))),
        AccountBalanceRequest.class);
    var response = restTemplate.postForObject(baseUrl,
        request, AccountBalanceResponse.class);
    var expectedResponse = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_should_return_balances_for_ma_with_empty_name.json"))),
        AccountBalanceResponse.class);
    assert response != null;
    assertEquals(objectMapper.writeValueAsString(expectedResponse),
        objectMapper.writeValueAsString(response));
  }

}
