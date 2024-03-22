package org.cardanofoundation.rosetta.api.data.account;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.openapitools.client.model.AccountBalanceRequest;
import org.openapitools.client.model.AccountBalanceResponse;
import org.openapitools.client.model.AccountCoinsRequest;
import org.openapitools.client.model.AccountCoinsResponse;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.Error;
import org.openapitools.client.model.NetworkIdentifier;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.account.controller.AccountApiImplementation;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;

import static org.cardanofoundation.rosetta.testgenerator.common.TestConstants.URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AccountApiTest extends IntegrationTest {

  private final String accountBalancePath = "/account/balance";
  private final String accountCoinsPath = "/account/coins";
  @Autowired
  private AccountApiImplementation accountApi;

  @Test
  void accountBalance2Ada_Test() {
    ResponseEntity<AccountBalanceResponse> response = restTemplate.postForEntity(
        URL + serverPort + accountBalancePath,
        getAccountBalanceRequest(), AccountBalanceResponse.class);
    AccountBalanceResponse accountBalanceResponse = response.getBody();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertNotNull(accountBalanceResponse);
    assertEquals(TestConstants.ACCOUNT_BALANCE_ADA_AMOUNT,
        accountBalanceResponse.getBalances().getFirst().getValue());
    assertEquals(TestConstants.ADA_SYMBOL,
        accountBalanceResponse.getBalances().getFirst().getCurrency().getSymbol());
  }

  @Test
  void accountBalanceException_Test() {
    AccountBalanceRequest accountBalanceRequest = getAccountBalanceRequest();
    accountBalanceRequest.getAccountIdentifier().setAddress("invalid_address");
    ResponseEntity<Error> response = restTemplate.postForEntity(
        URL + serverPort + accountBalancePath,
        accountBalanceRequest, Error.class);
    Error accountBalanceError = response.getBody();

    assertEquals(HttpStatusCode.valueOf(500), response.getStatusCode());
    assertNotNull(accountBalanceError);
    assertEquals("Provided address is invalid", accountBalanceError.getMessage());
    assertEquals("invalid_address",
        ((HashMap<String, String>) accountBalanceError.getDetails()).get("message"));
    assertEquals(4015, accountBalanceError.getCode());
  }

  @Test
  void accountCoins2Ada_Test() {
    ResponseEntity<AccountCoinsResponse> response = restTemplate.postForEntity(
        URL + serverPort + accountCoinsPath,
        getAccountCoinsRequest(), AccountCoinsResponse.class);
    AccountCoinsResponse accountCoinsResponse = response.getBody();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertNotNull(accountCoinsResponse);
    assertEquals(1, accountCoinsResponse.getCoins().size());
    assertEquals(TestConstants.ACCOUNT_BALANCE_ADA_AMOUNT,
        accountCoinsResponse.getCoins().getFirst().getAmount().getValue());
  }

  @Test
  void accountCoinsException_Test() {
    AccountCoinsRequest accountCoinsRequest = getAccountCoinsRequest();
    accountCoinsRequest.getAccountIdentifier().setAddress("invalid_address");
    ResponseEntity<Error> response = restTemplate.postForEntity(
        URL + serverPort + accountCoinsPath,
        accountCoinsRequest, Error.class);
    Error accountCoinsError = response.getBody();

    assertEquals(HttpStatusCode.valueOf(500), response.getStatusCode());
    assertNotNull(accountCoinsError);
    assertEquals("Provided address is invalid", accountCoinsError.getMessage());
    assertEquals("invalid_address",
        ((HashMap<String, String>) accountCoinsError.getDetails()).get("message"));
    assertEquals(4015, accountCoinsError.getCode());
  }

  private static AccountCoinsRequest getAccountCoinsRequest() {
    return AccountCoinsRequest.builder()
        .networkIdentifier(NetworkIdentifier.builder()
            .blockchain(TestConstants.TEST_BLOCKCHAIN)
            .network(TestConstants.TEST_NETWORK)
            .build())
        .accountIdentifier(AccountIdentifier.builder()
            .address(TestConstants.TEST_ACCOUNT_ADDRESS)
            .build())
        .includeMempool(true)
        .build();
  }

  private static AccountBalanceRequest getAccountBalanceRequest() {
    return AccountBalanceRequest.builder()
        .networkIdentifier(NetworkIdentifier.builder()
            .blockchain(TestConstants.TEST_BLOCKCHAIN)
            .network(TestConstants.TEST_NETWORK)
            .build())
        .accountIdentifier(AccountIdentifier.builder()
            .address(TestConstants.TEST_ACCOUNT_ADDRESS)
            .build())
        .build();
  }
}
