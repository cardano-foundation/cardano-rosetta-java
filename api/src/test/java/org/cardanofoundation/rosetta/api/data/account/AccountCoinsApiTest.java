package org.cardanofoundation.rosetta.api.data.account;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.jetbrains.annotations.NotNull;
import org.openapitools.client.model.AccountCoinsRequest;
import org.openapitools.client.model.AccountCoinsResponse;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.Coin;
import org.openapitools.client.model.CoinTokens;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.CurrencyMetadata;
import org.openapitools.client.model.Error;
import org.openapitools.client.model.NetworkIdentifier;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;
import org.cardanofoundation.rosetta.testgenerator.common.TestTransactionNames;

import static org.cardanofoundation.rosetta.testgenerator.common.TestConstants.URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AccountCoinsApiTest extends IntegrationTest {

  private final String myAssetPolicyId = "ae1ed1312d2e2e2e3e80e48e4485a9a0b1373ad71e28bde4764ca8c6";
  private final String latestTxHashOnZeroSlot = generatedDataMap.get(
      TestTransactionNames.SIMPLE_NEW_EMPTY_NAME_COINS_TRANSACTION.getName()).txHash() + ":0";
  private final String expectedTestAccountCoinAmount = "1635030";
  private final String emptyNamePolicyId = "b6d9dfb09401df509e565d42f0eff419ce58a020a9dbbe07754969d5";
  private final Currency myAssetCurrency = getCurrency(TestConstants.MY_ASSET_SYMBOL,
      Constants.MULTI_ASSET_DECIMALS, myAssetPolicyId);
  private final Currency ada = getCurrency(Constants.ADA, Constants.ADA_DECIMALS);
  private final Currency lovelace = getCurrency(Constants.LOVELACE, Constants.MULTI_ASSET_DECIMALS);

  @Test
  void accountCoins2Ada_Test() {
    ResponseEntity<AccountCoinsResponse> response = restTemplate.postForEntity(
        getAccountCoinsUrl(), getAccountCoinsRequest(TestConstants.TEST_ACCOUNT_ADDRESS),
        AccountCoinsResponse.class);

    AccountCoinsResponse accountCoinsResponse = response.getBody();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertNotNull(accountCoinsResponse);
    assertEquals(1, accountCoinsResponse.getCoins().size());
    List<CoinTokens> metadata = accountCoinsResponse.getCoins().getFirst().getMetadata()
        .get(latestTxHashOnZeroSlot);
    assertEquals(2, metadata.size());
    assertEquals(expectedTestAccountCoinAmount,
        accountCoinsResponse.getCoins().getFirst().getAmount().getValue());
    assertEquals(TestConstants.ACCOUNT_BALANCE_MINTED_TOKENS_AMOUNT,
        metadata.getFirst().getTokens().getFirst().getValue());
  }

  @Test
  void accountCoins2Lovelace_Test() {
    AccountCoinsRequest accountCoinsRequest = getAccountCoinsRequest(
        TestConstants.RECEIVER_1);
    ResponseEntity<AccountCoinsResponse> response = restTemplate.postForEntity(
        getAccountCoinsUrl(), accountCoinsRequest, AccountCoinsResponse.class);
    AccountCoinsResponse accountCoinsResponse = response.getBody();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertNotNull(accountCoinsResponse);
    assertEquals(2, accountCoinsResponse.getCoins().size());
    assertNotEquals(accountCoinsResponse.getCoins().getFirst().getCoinIdentifier(),
        accountCoinsResponse.getCoins().get(1).getCoinIdentifier());
    assertEquals(Constants.ADA,
        accountCoinsResponse.getCoins().getFirst().getAmount().getCurrency().getSymbol());
    assertEquals(Constants.ADA,
        accountCoinsResponse.getCoins().get(1).getAmount().getCurrency().getSymbol());
    assertEquals(Constants.ADA_DECIMALS,
        accountCoinsResponse.getCoins().getFirst().getAmount().getCurrency().getDecimals());
    assertEquals(Constants.ADA_DECIMALS,
        accountCoinsResponse.getCoins().get(1).getAmount().getCurrency().getDecimals());
    assertEquals(accountCoinsResponse.getCoins().getFirst().getAmount().getValue(),
        accountCoinsResponse.getCoins().get(1).getAmount().getValue());
    assertEquals(1939500L,
        Long.parseLong(accountCoinsResponse.getCoins().getFirst().getAmount().getValue())
            + Long.parseLong(accountCoinsResponse.getCoins().get(1).getAmount().getValue()));
  }

  @Test
  void accountCoinsNoCoins_Test() {
    ResponseEntity<AccountCoinsResponse> response = restTemplate.postForEntity(
        getAccountCoinsUrl(), getAccountCoinsRequest(TestConstants.RECEIVER_2),
        AccountCoinsResponse.class);
    AccountCoinsResponse accountCoinsResponse = response.getBody();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertNotNull(accountCoinsResponse);
    assertEquals(0, accountCoinsResponse.getCoins().size());
  }

  @Test
  void accountCoinsNoCoinsForStakeAccount_Test() {
    ResponseEntity<AccountCoinsResponse> response = restTemplate.postForEntity(
        getAccountCoinsUrl(),
        getAccountCoinsRequest(TestConstants.STAKE_ADDRESS_WITH_EARNED_REWARDS),
        AccountCoinsResponse.class);
    AccountCoinsResponse accountCoinsResponse = response.getBody();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertNotNull(accountCoinsResponse);
    assertEquals(0, accountCoinsResponse.getCoins().size());
  }

  @Test
  void accountCoinsDifferentCoins_Test() {
    ResponseEntity<AccountCoinsResponse> response = restTemplate.postForEntity(
        getAccountCoinsUrl(), getAccountCoinsRequest(TestConstants.TEST_ACCOUNT_ADDRESS),
        AccountCoinsResponse.class);
    AccountCoinsResponse accountCoinsResponse = response.getBody();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertNotNull(accountCoinsResponse);
    assertEquals(1, accountCoinsResponse.getCoins().size());
    assertEquals(latestTxHashOnZeroSlot,
        accountCoinsResponse.getCoins().getFirst().getCoinIdentifier().getIdentifier());
    assertEquals(expectedTestAccountCoinAmount,
        accountCoinsResponse.getCoins().getFirst().getAmount().getValue());
    assertEquals(Constants.ADA,
        accountCoinsResponse.getCoins().getFirst().getAmount().getCurrency().getSymbol());
    assertEquals(Constants.ADA_DECIMALS,
        accountCoinsResponse.getCoins().getFirst().getAmount().getCurrency().getDecimals());
    List<CoinTokens> metadata = accountCoinsResponse.getCoins().getFirst().getMetadata()
        .get(latestTxHashOnZeroSlot);
    assertEquals(2, metadata.size());
    assertNotEquals(metadata.get(1).getPolicyId(), metadata.getFirst().getPolicyId());
    assertEquals(metadata.getFirst().getPolicyId(), metadata
        .getFirst().getTokens().getFirst().getCurrency().getMetadata().getPolicyId());
    assertEquals(metadata.get(1).getPolicyId(), metadata
        .get(1).getTokens().getFirst().getCurrency().getMetadata().getPolicyId());
    assertEquals(TestConstants.ACCOUNT_BALANCE_MINTED_TOKENS_AMOUNT,
        metadata.getFirst().getTokens().getFirst().getValue());
    assertEquals(TestConstants.ACCOUNT_BALANCE_MINTED_TOKENS_AMOUNT,
        metadata.get(1).getTokens().getFirst().getValue());
    assertNotEquals(metadata.get(1).getTokens().getFirst().getCurrency().getSymbol(),
        metadata.getFirst().getTokens().getFirst().getCurrency().getSymbol());
  }

  @Test
  void accountCoinsEmptyNameCoin_Test() {
    ResponseEntity<AccountCoinsResponse> response = restTemplate.postForEntity(
        getAccountCoinsUrl(),
        getAccountCoinsRequestWithCurrencies(TestConstants.TEST_ACCOUNT_ADDRESS,
            getCurrency("\\x", Constants.MULTI_ASSET_DECIMALS, emptyNamePolicyId)),
        AccountCoinsResponse.class);
    AccountCoinsResponse accountCoinsResponse = response.getBody();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertNotNull(accountCoinsResponse);
    assertEquals(1, accountCoinsResponse.getCoins().size());
    assertEquals(1, accountCoinsResponse.getCoins().getFirst().getMetadata().size());
    assertEquals(latestTxHashOnZeroSlot,
        accountCoinsResponse.getCoins().getFirst().getCoinIdentifier().getIdentifier());
    assertEquals(Constants.ADA,
        accountCoinsResponse.getCoins().getFirst().getAmount().getCurrency().getSymbol());
    assertEquals(Constants.ADA_DECIMALS,
        accountCoinsResponse.getCoins().getFirst().getAmount().getCurrency().getDecimals());
    assertEquals(emptyNamePolicyId,
        accountCoinsResponse.getCoins().getFirst().getMetadata().get(latestTxHashOnZeroSlot)
            .getFirst().getPolicyId());
    assertEquals(TestConstants.ACCOUNT_BALANCE_MINTED_TOKENS_AMOUNT,
        accountCoinsResponse.getCoins().getFirst().getMetadata().get(latestTxHashOnZeroSlot).getFirst()
            .getTokens().getFirst().getValue());
    Currency mintedTokenCurrency = accountCoinsResponse.getCoins().getFirst().getMetadata()
        .get(latestTxHashOnZeroSlot).getFirst().getTokens().getFirst().getCurrency();
    assertEquals("", mintedTokenCurrency.getSymbol());
    assertEquals(Constants.MULTI_ASSET_DECIMALS, mintedTokenCurrency.getDecimals());
    assertEquals(emptyNamePolicyId, mintedTokenCurrency.getMetadata().getPolicyId());
  }

  @Test
  void accountCoinsOneSpecifiedCurrency_Test() {
    ResponseEntity<AccountCoinsResponse> response = restTemplate.postForEntity(
        getAccountCoinsUrl(), getAccountCoinsRequestWithCurrencies(TestConstants.RECEIVER_1, ada),
        AccountCoinsResponse.class);
    AccountCoinsResponse accountCoinsResponse = response.getBody();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertNotNull(accountCoinsResponse);
    assertEquals(2, accountCoinsResponse.getCoins().size());
    assertEquals("969750",
        accountCoinsResponse.getCoins().getFirst().getAmount().getValue());
    assertEquals(Constants.ADA,
        accountCoinsResponse.getCoins().getFirst().getAmount().getCurrency().getSymbol());
    assertEquals(Constants.ADA_DECIMALS,
        accountCoinsResponse.getCoins().getFirst().getAmount().getCurrency().getDecimals());
  }

  @Test
  void accountCoinsMultipleSpecifiedCurrencies_Test() {
    ResponseEntity<AccountCoinsResponse> response = restTemplate.postForEntity(
        getAccountCoinsUrl(),
        getAccountCoinsRequestWithCurrencies(TestConstants.TEST_ACCOUNT_ADDRESS, ada,
            myAssetCurrency), AccountCoinsResponse.class);
    AccountCoinsResponse accountCoinsResponse = response.getBody();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertNotNull(accountCoinsResponse);
    assertEquals(1, accountCoinsResponse.getCoins().size());
    Coin coins = accountCoinsResponse.getCoins().getFirst();
    assertEquals(expectedTestAccountCoinAmount, coins.getAmount().getValue());
    assertEquals(Constants.ADA, coins.getAmount().getCurrency().getSymbol());
    assertEquals(Constants.ADA_DECIMALS, coins.getAmount().getCurrency().getDecimals());
    List<CoinTokens> coinsMetadata = coins.getMetadata().values().iterator().next();
    assertEquals(2, coinsMetadata.size());
    assertNotEquals(coinsMetadata.getFirst().getPolicyId(), coinsMetadata.get(1).getPolicyId());
    assertEquals(TestConstants.ACCOUNT_BALANCE_MINTED_TOKENS_AMOUNT, coinsMetadata.getFirst()
        .getTokens().getFirst().getValue());
    assertEquals(coinsMetadata.getFirst().getPolicyId(),
        coinsMetadata.getFirst().getTokens().getFirst().getCurrency().getMetadata().getPolicyId());
    assertEquals(Constants.MULTI_ASSET_DECIMALS,
        coinsMetadata.getFirst().getTokens().getFirst().getCurrency().getDecimals());
    assertEquals(TestConstants.ACCOUNT_BALANCE_MINTED_TOKENS_AMOUNT, coinsMetadata.get(1)
        .getTokens().getFirst().getValue());
    assertEquals(coinsMetadata.get(1).getPolicyId(),
        coinsMetadata.get(1).getTokens().getFirst().getCurrency().getMetadata().getPolicyId());
    assertEquals(Constants.MULTI_ASSET_DECIMALS,
        coinsMetadata.get(1).getTokens().getFirst().getCurrency().getDecimals());
  }

  @Test
  void accountCoinsException_Test() {
    AccountCoinsRequest accountCoinsRequest = getAccountCoinsRequest(
        TestConstants.TEST_ACCOUNT_ADDRESS);
    accountCoinsRequest.getAccountIdentifier().setAddress("invalid_address");
    ResponseEntity<Error> response = restTemplate.postForEntity(
        getAccountCoinsUrl(), accountCoinsRequest, Error.class);
    Error accountCoinsError = response.getBody();

    assertEquals(HttpStatusCode.valueOf(500), response.getStatusCode());
    assertNotNull(accountCoinsError);
    assertEquals("Provided address is invalid", accountCoinsError.getMessage());
    assertEquals("invalid_address",
        ((HashMap<String, String>) accountCoinsError.getDetails()).get("message"));
    assertEquals(4015, accountCoinsError.getCode());
  }

  @Test
  void accountCoinsNonHexCurrencySymbolException_Test() {
    ResponseEntity<Error> response = restTemplate.postForEntity(
        getAccountCoinsUrl(),
        getAccountCoinsRequestWithCurrencies(TestConstants.TEST_ACCOUNT_ADDRESS,
            getCurrency("thisIsANonHexString", Constants.MULTI_ASSET_DECIMALS)),
        Error.class);
    Error accountCoinsError = response.getBody();

    assertEquals(HttpStatusCode.valueOf(500), response.getStatusCode());
    assertNotNull(accountCoinsError);
    assertEquals("Invalid token name", accountCoinsError.getMessage());
    assertEquals("Given name is thisIsANonHexString",
        ((HashMap<String, String>) accountCoinsError.getDetails()).get("message"));
    assertEquals(4024, accountCoinsError.getCode());
  }

  @Test
  void accountCoinsLovelaceCurrencySymbolException_Test() {
    ResponseEntity<Error> response = restTemplate.postForEntity(
        getAccountCoinsUrl(),
        getAccountCoinsRequestWithCurrencies(TestConstants.RECEIVER_1, lovelace),
        Error.class);
    Error accountCoinsError = response.getBody();

    assertEquals(HttpStatusCode.valueOf(500), response.getStatusCode());
    assertNotNull(accountCoinsError);
    assertEquals("Invalid token name", accountCoinsError.getMessage());
    assertEquals("Given name is lovelace",
        ((HashMap<String, String>) accountCoinsError.getDetails()).get("message"));
    assertEquals(4024, accountCoinsError.getCode());
  }

  @Test
  void accountCoinsTooLongCurrencyNameException_Test() {
    String tooLongCurrencyName = Stream.generate(() -> "0").limit(Constants.ASSET_NAME_LENGTH + 2)
        .collect(Collectors.joining());
    ResponseEntity<Error> response = restTemplate.postForEntity(
        getAccountCoinsUrl(),
        getAccountCoinsRequestWithCurrencies(TestConstants.TEST_ACCOUNT_ADDRESS,
            getCurrency(tooLongCurrencyName, Constants.MULTI_ASSET_DECIMALS)),
        Error.class);
    Error accountCoinsError = response.getBody();

    assertEquals(HttpStatusCode.valueOf(500), response.getStatusCode());
    assertNotNull(accountCoinsError);
    assertEquals("Invalid token name", accountCoinsError.getMessage());
    assertEquals("Given name is " + tooLongCurrencyName,
        ((HashMap<String, String>) accountCoinsError.getDetails()).get("message"));
    assertEquals(4024, accountCoinsError.getCode());
  }

  @Test
  void accountCoinsTooLongPolicyIdException_Test() {
    String tooLongPolicyId = Stream.generate(() -> "w").limit(Constants.POLICY_ID_LENGTH + 2)
        .collect(Collectors.joining());
    ResponseEntity<Error> response = restTemplate.postForEntity(
        getAccountCoinsUrl(),
        getAccountCoinsRequestWithCurrencies(TestConstants.TEST_ACCOUNT_ADDRESS,
            getCurrency(TestConstants.CURRENCY_HEX_SYMBOL, Constants.MULTI_ASSET_DECIMALS)
                .metadata(new CurrencyMetadata(tooLongPolicyId))),
        Error.class);
    Error accountCoinsError = response.getBody();

    assertEquals(HttpStatusCode.valueOf(500), response.getStatusCode());
    assertNotNull(accountCoinsError);
    assertEquals("Invalid policy id", accountCoinsError.getMessage());
    assertEquals("Given policy id is " + tooLongPolicyId,
        ((HashMap<String, String>) accountCoinsError.getDetails()).get("message"));
    assertEquals(4023, accountCoinsError.getCode());
  }

  @Test
  void accountCoinsNonHexPolicyIdException_Test() {
    ResponseEntity<Error> response = restTemplate.postForEntity(
        getAccountCoinsUrl(),
        getAccountCoinsRequestWithCurrencies(TestConstants.TEST_ACCOUNT_ADDRESS,
            getCurrency(TestConstants.CURRENCY_HEX_SYMBOL, Constants.MULTI_ASSET_DECIMALS)
                .metadata(new CurrencyMetadata("thisIsNonHexPolicyId"))),
        Error.class);
    Error accountCoinsError = response.getBody();

    assertEquals(HttpStatusCode.valueOf(500), response.getStatusCode());
    assertNotNull(accountCoinsError);
    assertEquals("Invalid policy id", accountCoinsError.getMessage());
    assertEquals("Given policy id is thisIsNonHexPolicyId",
        ((HashMap<String, String>) accountCoinsError.getDetails()).get("message"));
    assertEquals(4023, accountCoinsError.getCode());
  }

  @NotNull
  private String getAccountCoinsUrl() {
    String accountCoinsPath = "/account/coins";
    return URL + serverPort + accountCoinsPath;
  }

  private AccountCoinsRequest getAccountCoinsRequest(String accountAddress) {
    return AccountCoinsRequest.builder()
        .networkIdentifier(NetworkIdentifier.builder()
            .blockchain(TestConstants.TEST_BLOCKCHAIN)
            .network(TestConstants.TEST_NETWORK)
            .build())
        .accountIdentifier(AccountIdentifier.builder()
            .address(accountAddress)
            .build())
        .includeMempool(true)
        .build();
  }

  private AccountCoinsRequest getAccountCoinsRequestWithCurrencies(String accountAddress,
      Currency... currencies) {
    return AccountCoinsRequest.builder()
        .networkIdentifier(NetworkIdentifier.builder()
            .blockchain(TestConstants.TEST_BLOCKCHAIN)
            .network(TestConstants.TEST_NETWORK)
            .build())
        .accountIdentifier(AccountIdentifier.builder()
            .address(accountAddress)
            .build())
        .currencies(Arrays.asList(currencies))
        .includeMempool(true)
        .build();
  }

  private Currency getCurrency(String symbol, int decimals) {
    return Currency.builder()
        .symbol(symbol)
        .decimals(decimals)
        .build();
  }

  private Currency getCurrency(String symbol, int decimals, String policyId) {
    return Currency.builder()
        .symbol(symbol)
        .decimals(decimals)
        .metadata(CurrencyMetadata.builder().policyId(policyId).build())
        .build();
  }
}
