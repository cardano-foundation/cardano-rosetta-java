package org.cardanofoundation.rosetta.api.account;

import static org.cardanofoundation.rosetta.api.common.constants.Constants.CARDANO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import org.cardanofoundation.rosetta.api.IntegrationTestWithDB;
import org.cardanofoundation.rosetta.api.exception.Error;
import org.cardanofoundation.rosetta.api.model.rest.AccountCoinsRequest;
import org.cardanofoundation.rosetta.api.model.rest.AccountCoinsResponse;
import org.cardanofoundation.rosetta.api.model.rest.AccountIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.Currency;
import org.cardanofoundation.rosetta.api.model.rest.NetworkIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpServerErrorException;

public class AccountCoinsApiTest extends IntegrationTestWithDB {

  private static final String ENDPOINT = "/account/coins";
  private static final String NETWORK = "mainnet";
  private final String BASE_DIRECTORY = "src/test/resources/accountCoins";

  @BeforeEach
  public void setUp() {
    baseUrl = baseUrl.concat(":").concat(String.valueOf(serverPort)).concat(ENDPOINT);
  }

  private AccountCoinsRequest generatePayload(String blockchain, String network, String address,
      List<Currency> currencies) {
    return AccountCoinsRequest.builder()
        .networkIdentifier(NetworkIdentifier.builder()
            .blockchain(Objects.nonNull(blockchain) ? blockchain : CARDANO)
            .network(Objects.nonNull(network) ? network : NETWORK)
            .build())
        .accountIdentifier(AccountIdentifier.builder()
            .address(address)
            .build())
        .currencies(currencies)
        .build();
  }

  @Test
  void test_consider_coins_till_latest_block() throws Exception {
    var request = generatePayload(CARDANO, NETWORK,
        "DdzFFzCqrhsdufpFxByLTQmktKJnTrudktaHq1nK2MAEDLXjz5kbRcr5prHi9gHb6m8pTvhgK6JbFDZA1LTiTcP6g8KuPSF1TfKP8ewp",
        null);
    var response = restTemplate.postForObject(baseUrl,
        request, AccountCoinsResponse.class);

    var expectedResponse = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(
                    BASE_DIRECTORY + "/response/test_consider_coins_till_latest_block.json"))),
        AccountCoinsResponse.class);
    assert response != null;
    assertEquals(response.getCoins().size(), expectedResponse.getCoins().size());
    assertEquals(objectMapper.writeValueAsString(expectedResponse),
        objectMapper.writeValueAsString(response));
  }

  @Test
  void test_return_empty_if_address_doesnt_exist() throws JsonProcessingException {
    var request = generatePayload(CARDANO, NETWORK, "fakeAddress", null);
    try {
      var response = restTemplate.postForObject(baseUrl,
          request, AccountCoinsResponse.class);
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      Error error = objectMapper.readValue(responseBody, Error.class);
      assertEquals(500, e.getStatusCode().value());
      assertEquals(4015, error.getCode());
      assertEquals("Provided address is invalid", error.getMessage());
      assertTrue(error.isRetriable());
    }
  }

  @Test
  void test_no_coins_for_an_account_with_zero_balance_replace_space() throws IOException {
    var request = generatePayload(CARDANO, NETWORK,
        "DdzFFzCqrhsszHTvbjTmYje5hehGbadkT6WgWbaqCy5XNxNttsPNF13eAjjBHYT7JaLJz2XVxiucam1EvwBRPSTiCrT4TNCBas4hfzic",
        null);
    var response = restTemplate.postForObject(baseUrl,
        request, AccountCoinsResponse.class);

    var expectedResponse = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_no_coins_for_an_account_with_zero_balance_replace_space.json"))),
        AccountCoinsResponse.class);
    assert response != null;
    assertEquals(response.getCoins().size(), expectedResponse.getCoins().size());
    assertEquals(objectMapper.writeValueAsString(expectedResponse),
        objectMapper.writeValueAsString(response));

  }

  @Test
  void test_no_return_coins_for_stake_accounts() throws IOException {
    var request = generatePayload(CARDANO, NETWORK,
        "stake1uyqq2a22arunrft3k9ehqc7yjpxtxjmvgndae80xw89mwyge9skyp", null);
    var response = restTemplate.postForObject(baseUrl,
        request, AccountCoinsResponse.class);

    var expectedResponse = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_no_return_coins_for_stake_accounts.json"))),
        AccountCoinsResponse.class);
    assertEquals(objectMapper.writeValueAsString(expectedResponse),
        objectMapper.writeValueAsString(response));

  }

  @Test
  void test_return_coins_with_multi_assets_currencies() throws IOException {
    var request = generatePayload(CARDANO, NETWORK,
        "addr1q8a3rmnxnp986vy3tzz3vd3mdk9lmjnnw6w68uaaa8g4t4u5lddnau28pea3mdy84uls504lsc7uk9zyzmqtcxyy7jyqqjm7sg",
        null);
    var response = restTemplate.postForObject(baseUrl,
        request, AccountCoinsResponse.class);
    var expectedResponse = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_return_coins_with_multi_assets_currencies.json"))),
        AccountCoinsResponse.class);
    System.out.println(expectedResponse);
    assert response != null;
    assertEquals(response.getCoins().size(), expectedResponse.getCoins().size());
    assertEquals(objectMapper.writeValueAsString(expectedResponse),
        objectMapper.writeValueAsString(response));

  }

  @Test
  void test_return_coins_for_ma_with_empty_name() throws IOException {
    var request = generatePayload(CARDANO, NETWORK,
        "addr1qx5d5d8aqn0970nl3km63za5q87fwh2alm79zwuxvh6rh9lg96s8las2lwer5psc7yr59kmafzkz2l5jz4dyxghs7pvqj24sft",
        null);
    var response = restTemplate.postForObject(baseUrl,
        request, AccountCoinsResponse.class);
    var expectedResponse = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_return_coins_for_ma_with_empty_name.json"))),
        AccountCoinsResponse.class);
    assert response != null;
    assertEquals(response.getCoins().size(), expectedResponse.getCoins().size());
    assertEquals(objectMapper.writeValueAsString(expectedResponse),
        objectMapper.writeValueAsString(response));

  }

  @Test
  void test_return_coins_for_one_specified_currency() throws IOException {
    AccountCoinsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_return_coins_for_one_specified_currency_request.json"))),
        AccountCoinsRequest.class);
    var response = restTemplate.postForObject(baseUrl,
        request, AccountCoinsResponse.class);
    var expectedResponse = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_return_coins_for_one_specified_currency.json"))),
        AccountCoinsResponse.class);
    assert response != null;
    assertEquals(response.getCoins().size(), expectedResponse.getCoins().size());
    assertEquals(objectMapper.writeValueAsString(expectedResponse),
        objectMapper.writeValueAsString(response));
  }

  @Test
  void test_return_coins_for_multiple_specified_currency() throws IOException {
    AccountCoinsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_return_coins_for_multiple_specified_currency_request.json"))),
        AccountCoinsRequest.class);
    var response = restTemplate.postForObject(baseUrl,
        request, AccountCoinsResponse.class);
    var expectedResponse = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_return_coins_for_multiple_specified_currency.json"))),
        AccountCoinsResponse.class);
    assert response != null;
    assertEquals(response.getCoins().size(), expectedResponse.getCoins().size());
    assertEquals(objectMapper.writeValueAsString(expectedResponse),
        objectMapper.writeValueAsString(response));
  }

  @Test
  void test_return_coins_for_multi_asset_currency_with_empty_name() throws IOException {
    AccountCoinsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_return_coins_for_multi_asset_currency_with_empty_name_request.json"))),
        AccountCoinsRequest.class);
    var response = restTemplate.postForObject(baseUrl,
        request, AccountCoinsResponse.class);
    var expectedResponse = objectMapper.readValue(new String(
            Files.readAllBytes(
                Paths.get(BASE_DIRECTORY
                    + "/response/test_return_coins_for_multi_asset_currency_with_empty_name.json"))),
        AccountCoinsResponse.class);
    assert response != null;
    assertEquals(response.getCoins().size(), expectedResponse.getCoins().size());
    assertEquals(objectMapper.writeValueAsString(expectedResponse),
        objectMapper.writeValueAsString(response));
  }

  @Test
  void test_fail_when_querying_for_a_currency_with_non_hex_string_symbol() throws IOException {
    AccountCoinsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_fail_when_querying_for_a_currency_with_non_hex_string_symbol_request.json"))),
        AccountCoinsRequest.class);
    try {
      var response = restTemplate.postForObject(baseUrl,
          request, AccountCoinsResponse.class);
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      Error error = objectMapper.readValue(responseBody, Error.class);
      assertEquals(500, e.getStatusCode().value());
      assertEquals(4024, error.getCode());
      assertEquals("Invalid token name", error.getMessage());
      assertFalse(error.isRetriable());
      assertEquals("Given name is thisIsANonHexString", error.getDetails().toString());
    }
  }

  @Test
  void test_fail_when_querying_for_a_currency_with_a_symbol_longer_than_expected()
      throws IOException {
    AccountCoinsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_fail_when_querying_for_a_currency_with_a_symbol_longer_than_expected.json"))),
        AccountCoinsRequest.class);
    try {
      var response = restTemplate.postForObject(baseUrl,
          request, AccountCoinsResponse.class);
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      Error error = objectMapper.readValue(responseBody, Error.class);
      assertEquals(500, e.getStatusCode().value());
      assertEquals(4024, error.getCode());
      assertEquals("Invalid token name", error.getMessage());
      assertFalse(error.isRetriable());
      assertEquals(
          "Given name is 00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
          error.getDetails().toString());
    }
  }

  @Test
  void test_fail_when_querying_for_a_currency_with_a_policy_id_longer_than_expected()
      throws IOException {
    AccountCoinsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_fail_when_querying_for_a_currency_with_a_policy_id_longer_than_expected.json"))),
        AccountCoinsRequest.class);
    try {
      var response = restTemplate.postForObject(baseUrl,
          request, AccountCoinsResponse.class);
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      Error error = objectMapper.readValue(responseBody, Error.class);
      assertEquals(500, e.getStatusCode().value());
      assertEquals(4023, error.getCode());
      assertEquals("Invalid policy id", error.getMessage());
      assertFalse(error.isRetriable());
      assertEquals("Given policy id is wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww",
          error.getDetails().toString());
    }
  }

  @Test
  void test_fail_when_querying_for_a_currency_with_a_non_hex_policy_id() throws IOException {
    AccountCoinsRequest request = objectMapper.readValue(new String(Files.readAllBytes(
            Paths.get(BASE_DIRECTORY
                + "/request/test_fail_when_querying_for_a_currency_with_a_non_hex_policy_id.json"))),
        AccountCoinsRequest.class);
    try {
      var response = restTemplate.postForObject(baseUrl,
          request, AccountCoinsResponse.class);
    } catch (HttpServerErrorException e) {
      String responseBody = e.getResponseBodyAsString();
      Error error = objectMapper.readValue(responseBody, Error.class);
      assertEquals(500, e.getStatusCode().value());
      assertEquals(4023, error.getCode());
      assertEquals("Invalid policy id", error.getMessage());
      assertFalse(error.isRetriable());
      assertEquals("Given policy id is thisIsANonHexString", error.getDetails().toString());
    }
  }
}
