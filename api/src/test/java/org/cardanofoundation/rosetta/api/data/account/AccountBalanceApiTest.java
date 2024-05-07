package org.cardanofoundation.rosetta.api.data.account;

import java.util.HashMap;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.openapitools.client.model.AccountBalanceRequest;
import org.openapitools.client.model.AccountBalanceResponse;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.Error;
import org.openapitools.client.model.NetworkIdentifier;
import org.openapitools.client.model.PartialBlockIdentifier;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;
import org.cardanofoundation.rosetta.testgenerator.common.TestTransactionNames;

import static org.cardanofoundation.rosetta.testgenerator.common.TestConstants.URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AccountBalanceApiTest extends IntegrationTest {

  private final String upToBlockHash = generatedDataMap.get(
      TestTransactionNames.SIMPLE_LOVELACE_FIRST_TRANSACTION.getName()).blockHash();
  private final Long upToBlockNumber = generatedDataMap.get(
      TestTransactionNames.SIMPLE_LOVELACE_FIRST_TRANSACTION.getName()).blockNumber();

  private final String currentAdaBalance = "1635030";
  private final String currentLovelaceBalance = "1939500";

  @Test
  void accountBalance2Ada_Test() {
    ResponseEntity<AccountBalanceResponse> response = restTemplate.postForEntity(
        getAccountBalanceUrl(), getAccountBalanceRequest(TestConstants.TEST_ACCOUNT_ADDRESS),
        AccountBalanceResponse.class);
    AccountBalanceResponse accountBalanceResponse = response.getBody();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertNotNull(accountBalanceResponse);
    assertEquals(currentAdaBalance,
        accountBalanceResponse.getBalances().getFirst().getValue());
    assertEquals(Constants.ADA,
        accountBalanceResponse.getBalances().getFirst().getCurrency().getSymbol());
  }

  @Test
  void accountBalance2Lovelace_Test() {
    AccountBalanceRequest accountBalanceRequest = getAccountBalanceRequest(
        TestConstants.RECEIVER_1);
    ResponseEntity<AccountBalanceResponse> response = restTemplate.postForEntity(
        getAccountBalanceUrl(), accountBalanceRequest, AccountBalanceResponse.class);
    AccountBalanceResponse accountBalanceResponse = response.getBody();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertNotNull(accountBalanceResponse);
    assertEquals(1, accountBalanceResponse.getBalances().size());
    assertAdaCurrency(accountBalanceResponse);
    assertEquals(currentLovelaceBalance,
        accountBalanceResponse.getBalances().getFirst().getValue());
  }

  @Test
  void accountBalanceMintedToken_Test() {
    AccountBalanceRequest accountBalanceRequest = getAccountBalanceRequest(
        TestConstants.TEST_ACCOUNT_ADDRESS);
    ResponseEntity<AccountBalanceResponse> response = restTemplate.postForEntity(
        getAccountBalanceUrl(), accountBalanceRequest, AccountBalanceResponse.class);
    AccountBalanceResponse accountBalanceResponse = response.getBody();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertNotNull(accountBalanceResponse);
    assertEquals(3, accountBalanceResponse.getBalances().size());
    assertAdaCurrency(accountBalanceResponse);
    assertEquals(TestConstants.ACCOUNT_BALANCE_MINTED_TOKENS_AMOUNT,
        accountBalanceResponse.getBalances().get(1).getValue());
    assertNotEquals(accountBalanceResponse.getBalances().getFirst().getCurrency().getSymbol(),
        accountBalanceResponse.getBalances().get(1).getCurrency().getSymbol());
  }

  @Test
  void AddressBalanceMintedTokenWithEmptyName_Test() {
    AccountBalanceRequest accountBalanceRequest = getAccountBalanceRequest(
        TestConstants.TEST_ACCOUNT_ADDRESS);
    ResponseEntity<AccountBalanceResponse> response = restTemplate.postForEntity(
        getAccountBalanceUrl(), accountBalanceRequest, AccountBalanceResponse.class);
    AccountBalanceResponse accountBalanceResponse = response.getBody();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertNotNull(accountBalanceResponse);
    assertEquals(3, accountBalanceResponse.getBalances().size());
    assertAdaCurrency(accountBalanceResponse);
    assertEquals(TestConstants.ACCOUNT_BALANCE_MINTED_TOKENS_AMOUNT,
        accountBalanceResponse.getBalances().get(1).getValue());
    assertNotEquals(accountBalanceResponse.getBalances().getFirst().getCurrency().getSymbol(),
        accountBalanceResponse.getBalances().get(1).getCurrency().getSymbol());
    assertEquals("", accountBalanceResponse.getBalances().get(2).getCurrency().getSymbol());
  }

  @Test
  void accountBalanceUntilBlockByHash_Test() {
    String balanceUpToBlock = "969750";
    AccountBalanceRequest accountBalanceRequest = getAccountBalanceRequestUntilBlock(
        TestConstants.RECEIVER_1, null, upToBlockHash);
    ResponseEntity<AccountBalanceResponse> response = restTemplate.postForEntity(
        getAccountBalanceUrl(), accountBalanceRequest, AccountBalanceResponse.class);
    AccountBalanceResponse accountBalanceResponse = response.getBody();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertNotNull(accountBalanceResponse);
    assertEquals(1, accountBalanceResponse.getBalances().size());
    assertEquals(accountBalanceResponse.getBlockIdentifier().getHash(), upToBlockHash);
    assertEquals(accountBalanceResponse.getBlockIdentifier().getIndex(), upToBlockNumber);
    assertEquals(balanceUpToBlock, accountBalanceResponse.getBalances().getFirst().getValue());
    assertAdaCurrency(accountBalanceResponse);
  }

  @Test
  void accountBalanceUntilBlockByIndex_Test() {
    String balanceUpToBlock = "969750";
    AccountBalanceRequest accountBalanceRequest = getAccountBalanceRequestUntilBlock(
        TestConstants.RECEIVER_1, upToBlockNumber, null);
    ResponseEntity<AccountBalanceResponse> response = restTemplate.postForEntity(
        getAccountBalanceUrl(), accountBalanceRequest, AccountBalanceResponse.class);
    AccountBalanceResponse accountBalanceResponse = response.getBody();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertNotNull(accountBalanceResponse);
    assertEquals(1, accountBalanceResponse.getBalances().size());
    assertEquals(accountBalanceResponse.getBlockIdentifier().getHash(), upToBlockHash);
    assertEquals(accountBalanceResponse.getBlockIdentifier().getIndex(), upToBlockNumber);
    assertEquals(balanceUpToBlock, accountBalanceResponse.getBalances().getFirst().getValue());
    assertAdaCurrency(accountBalanceResponse);
  }

  @Test
  void accountBalanceUntilBlockByIndexAndHash_Test() {
    String balanceUpToBlock = "969750";
    AccountBalanceRequest accountBalanceRequest = getAccountBalanceRequestUntilBlock(
        TestConstants.RECEIVER_1, upToBlockNumber, upToBlockHash);
    ResponseEntity<AccountBalanceResponse> response = restTemplate.postForEntity(
        getAccountBalanceUrl(), accountBalanceRequest, AccountBalanceResponse.class);
    AccountBalanceResponse accountBalanceResponse = response.getBody();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertNotNull(accountBalanceResponse);
    assertEquals(1, accountBalanceResponse.getBalances().size());
    assertEquals(accountBalanceResponse.getBlockIdentifier().getHash(), upToBlockHash);
    assertEquals(accountBalanceResponse.getBlockIdentifier().getIndex(), upToBlockNumber);
    assertEquals(balanceUpToBlock, accountBalanceResponse.getBalances().getFirst().getValue());
    assertAdaCurrency(accountBalanceResponse);
  }

  @Test
  void accountBalanceUntilBlockException_Test() {
    AccountBalanceRequest accountBalanceRequest = getAccountBalanceRequestUntilBlock(
        TestConstants.TEST_ACCOUNT_ADDRESS, upToBlockNumber + 1L, upToBlockHash);
    ResponseEntity<Error> response = restTemplate.postForEntity(
        getAccountBalanceUrl(), accountBalanceRequest, Error.class);
    Error accountBalanceError = response.getBody();

    assertEquals(HttpStatusCode.valueOf(500), response.getStatusCode());
    assertNotNull(accountBalanceError);
    assertEquals("Block not found", accountBalanceError.getMessage());
    assertEquals(4001, accountBalanceError.getCode());
  }

  @Test
  void accountBalanceException_Test() {
    AccountBalanceRequest accountBalanceRequest = getAccountBalanceRequest(
        TestConstants.TEST_ACCOUNT_ADDRESS);
    accountBalanceRequest.getAccountIdentifier().setAddress("invalid_address");
    ResponseEntity<Error> response = restTemplate.postForEntity(
        getAccountBalanceUrl(), accountBalanceRequest, Error.class);
    Error accountBalanceError = response.getBody();

    assertEquals(HttpStatusCode.valueOf(500), response.getStatusCode());
    assertNotNull(accountBalanceError);
    assertEquals("Provided address is invalid", accountBalanceError.getMessage());
    assertEquals("invalid_address",
        ((HashMap<String, String>) accountBalanceError.getDetails()).get("message"));
    assertEquals(4015, accountBalanceError.getCode());
  }

  @Test
  @Disabled("No test setup for stake address with rewards yet implemented")
  void accountBalanceStakeAddressWithNoEarnedRewards_Test() {
    AccountBalanceRequest accountBalanceRequest = getAccountBalanceRequest(
        TestConstants.STAKE_ADDRESS_WITH_NO_EARNED_REWARDS);
    ResponseEntity<AccountBalanceResponse> response = restTemplate.postForEntity(
        getAccountBalanceUrl(), accountBalanceRequest, AccountBalanceResponse.class);
    AccountBalanceResponse accountBalanceResponse = response.getBody();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertNotNull(accountBalanceResponse);
    assertEquals(1, accountBalanceResponse.getBalances().size());
    assertEquals("0", accountBalanceResponse.getBalances().getFirst().getValue());
    assertAdaCurrency(accountBalanceResponse);
  }

  @Test
  void accountBalanceStakeAddressUntilBlockNumber_Test() {
    AccountBalanceRequest accountBalanceRequest = getAccountBalanceRequestUntilBlock(
        TestConstants.STAKE_ADDRESS_WITH_EARNED_REWARDS, upToBlockNumber, null);
    ResponseEntity<AccountBalanceResponse> response = restTemplate.postForEntity(
        getAccountBalanceUrl(), accountBalanceRequest, AccountBalanceResponse.class);
    AccountBalanceResponse accountBalanceResponse = response.getBody();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertNotNull(accountBalanceResponse);
    assertEquals(1, accountBalanceResponse.getBalances().size());
    assertEquals(TestConstants.STAKE_ACCOUNT_BALANCE_AMOUNT,
        accountBalanceResponse.getBalances().getFirst().getValue());
    assertAdaCurrency(accountBalanceResponse);
  }

  @Test
  @Disabled("No test setup for minted tokens on stake address yet implemented")
  void accountBalanceStakeAddressWithMintedUtxo_Test() {
    AccountBalanceRequest accountBalanceRequest = getAccountBalanceRequest(
        TestConstants.STAKE_ADDRESS_WITH_MINTED_TOKENS);
    ResponseEntity<AccountBalanceResponse> response = restTemplate.postForEntity(
        getAccountBalanceUrl(), accountBalanceRequest, AccountBalanceResponse.class);
    AccountBalanceResponse accountBalanceResponse = response.getBody();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertNotNull(accountBalanceResponse);
    assertEquals(1, accountBalanceResponse.getBalances().size());
    assertAdaCurrency(accountBalanceResponse);
    assertEquals(TestConstants.STAKE_ACCOUNT_BALANCE_AMOUNT,
        accountBalanceResponse.getBalances().getFirst().getValue());
    assertEquals(TestConstants.ACCOUNT_BALANCE_MINTED_TOKENS_AMOUNT,
        accountBalanceResponse.getBalances().get(1).getValue());
    assertNotEquals(accountBalanceResponse.getBalances().getFirst().getCurrency().getSymbol(),
        accountBalanceResponse.getBalances().get(1).getCurrency().getSymbol());
    // On the account there are 2 minted tokens with the same amount
    assertEquals(TestConstants.ACCOUNT_BALANCE_MINTED_TOKENS_AMOUNT,
        accountBalanceResponse.getBalances().get(1).getValue());
    assertNotEquals(accountBalanceResponse.getBalances().getFirst().getCurrency().getSymbol(),
        accountBalanceResponse.getBalances().get(1).getCurrency().getSymbol());
  }

  @Test
  void accountBalanceBetweenTwoBlocksWithMintedCoins_Test() {
    String upToBlockHashTestAccount = generatedDataMap.get(
        TestTransactionNames.SIMPLE_NEW_EMPTY_NAME_COINS_TRANSACTION.getName()).blockHash();
    long upToBlockNumberTestAccount = generatedDataMap.get(
        TestTransactionNames.SIMPLE_NEW_EMPTY_NAME_COINS_TRANSACTION.getName()).blockNumber();

    AccountBalanceRequest accountBalanceRequest = getAccountBalanceRequestUntilBlock(
        TestConstants.TEST_ACCOUNT_ADDRESS, upToBlockNumberTestAccount, null);
    ResponseEntity<AccountBalanceResponse> response = restTemplate.postForEntity(
        getAccountBalanceUrl(), accountBalanceRequest, AccountBalanceResponse.class);
    AccountBalanceResponse accountBalanceResponseWith3Tokens = response.getBody();

    accountBalanceRequest = getAccountBalanceRequestUntilBlock(
        TestConstants.TEST_ACCOUNT_ADDRESS, upToBlockNumberTestAccount - 1L, null);
    response = restTemplate.postForEntity(
        getAccountBalanceUrl(), accountBalanceRequest, AccountBalanceResponse.class);
    AccountBalanceResponse accountBalanceResponseWith2Tokens = response.getBody();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    // check the balance on the current block
    assertNotNull(accountBalanceResponseWith3Tokens);
    assertEquals(3, accountBalanceResponseWith3Tokens.getBalances().size());
    assertEquals(currentAdaBalance,
        accountBalanceResponseWith3Tokens.getBalances().getFirst().getValue());
    assertEquals(TestConstants.ACCOUNT_BALANCE_MINTED_TOKENS_AMOUNT,
        accountBalanceResponseWith3Tokens.getBalances().get(1).getValue());
    assertNotEquals(
        accountBalanceResponseWith3Tokens.getBalances().getFirst().getCurrency().getSymbol(),
        accountBalanceResponseWith3Tokens.getBalances().get(2).getCurrency().getSymbol());
    assertEquals(upToBlockHashTestAccount,
        accountBalanceResponseWith3Tokens.getBlockIdentifier().getHash());
    assertEquals(upToBlockNumberTestAccount,
        accountBalanceResponseWith3Tokens.getBlockIdentifier().getIndex());
    // Check the balance on the previous block
    assertNotNull(accountBalanceResponseWith2Tokens);
    assertEquals(2, accountBalanceResponseWith2Tokens.getBalances().size());
    assertEquals(TestConstants.ACCOUNT_BALANCE_MINTED_TOKENS_AMOUNT,
        accountBalanceResponseWith2Tokens.getBalances().get(1).getValue());
    assertEquals(upToBlockNumberTestAccount - 1L,
        accountBalanceResponseWith2Tokens.getBlockIdentifier().getIndex());
    String mintedTokenSymbol = accountBalanceResponseWith2Tokens.getBalances().get(1)
        .getCurrency()
        .getSymbol();
    assertNotEquals(Constants.ADA, mintedTokenSymbol);
    assertNotEquals(Constants.LOVELACE, mintedTokenSymbol);
    assertNotEquals("", mintedTokenSymbol);
  }

  private String getAccountBalanceUrl() {
    String accountBalancePath = "/account/balance";
    return URL + serverPort + accountBalancePath;
  }

  private AccountBalanceRequest getAccountBalanceRequest(String accountAddress) {
    return AccountBalanceRequest.builder()
        .networkIdentifier(NetworkIdentifier.builder()
            .blockchain(TestConstants.TEST_BLOCKCHAIN)
            .network(TestConstants.TEST_NETWORK)
            .build())
        .accountIdentifier(AccountIdentifier.builder()
            .address(accountAddress)
            .build())
        .build();
  }

  private AccountBalanceRequest getAccountBalanceRequestUntilBlock(String accountAddress,
      Long blockIndex, String blockHash) {
    return AccountBalanceRequest.builder()
        .networkIdentifier(NetworkIdentifier.builder()
            .blockchain(TestConstants.TEST_BLOCKCHAIN)
            .network(TestConstants.TEST_NETWORK)
            .build())
        .accountIdentifier(AccountIdentifier.builder()
            .address(accountAddress)
            .build())
        .blockIdentifier(PartialBlockIdentifier.builder()
            .index(blockIndex)
            .hash(blockHash)
            .build())
        .build();
  }

  private static void assertAdaCurrency(AccountBalanceResponse accountBalanceResponse) {
    assertFalse(accountBalanceResponse.getBalances().isEmpty());
    assertEquals(Constants.ADA,
        accountBalanceResponse.getBalances().getFirst().getCurrency().getSymbol());
    assertEquals(Constants.ADA_DECIMALS,
        accountBalanceResponse.getBalances().getFirst().getCurrency().getDecimals());
  }
}
