package org.cardanofoundation.rosetta.api.data.account;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.jetbrains.annotations.NotNull;
import org.openapitools.client.model.AccountCoinsRequest;
import org.openapitools.client.model.AccountCoinsResponse;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.CurrencyMetadata;
import org.openapitools.client.model.Error;
import org.openapitools.client.model.NetworkIdentifier;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;

import static org.cardanofoundation.rosetta.testgenerator.common.TestConstants.URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AccountCoinsApiTest extends IntegrationTest {

  private final String myAssetPolicyId = "ae1ed1312d2e2e2e3e80e48e4485a9a0b1373ad71e28bde4764ca8c6";
  private final String emptyNamePolicyId = "8fb99d4762495fbf0feb0a5b2e32342f1f4f36046f8ac23476e81ef3";
  private final Currency myAssetCurrency = getCurrency(TestConstants.MY_ASSET_SYMBOL,
      Constants.MULTI_ASSET_DECIMALS, myAssetPolicyId);
  private final Currency ada = getCurrency(Constants.ADA, Constants.ADA_DECIMALS);
  private final Currency lovelace = getCurrency(Constants.LOVELACE, Constants.ADA_DECIMALS);

  @Test
  @Disabled("RA-61 issue - data mapper need to be updated to handle multi-asset coins")
  void accountCoins2Ada_Test() {
    ResponseEntity<AccountCoinsResponse> response = restTemplate.postForEntity(
        getAccountCoinsUrl(), getAccountCoinsRequest(TestConstants.TEST_ACCOUNT_ADDRESS),
        AccountCoinsResponse.class);
    AccountCoinsResponse accountCoinsResponse = response.getBody();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertNotNull(accountCoinsResponse);
    assertEquals(2, accountCoinsResponse.getCoins().size());
    assertEquals(TestConstants.ACCOUNT_BALANCE_LOVELACE_AMOUNT,
        accountCoinsResponse.getCoins().getFirst().getAmount().getValue());
    assertEquals(TestConstants.ACCOUNT_BALANCE_MINTED_TOKENS_AMOUNT,
        accountCoinsResponse.getCoins().get(1).getAmount().getValue());
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
    assertEquals(Constants.LOVELACE,
        accountCoinsResponse.getCoins().getFirst().getAmount().getCurrency().getSymbol());
    assertEquals(Constants.LOVELACE,
        accountCoinsResponse.getCoins().get(1).getAmount().getCurrency().getSymbol());
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
  @Disabled("RA-61 issue - data mapper need to be updated to handle multi-asset coins")
  void accountCoinsDifferentCoins_Test() {
    ResponseEntity<AccountCoinsResponse> response = restTemplate.postForEntity(
        getAccountCoinsUrl(), getAccountCoinsRequest(TestConstants.TEST_ACCOUNT_ADDRESS),
        AccountCoinsResponse.class);
    AccountCoinsResponse accountCoinsResponse = response.getBody();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertNotNull(accountCoinsResponse);
    assertEquals(3, accountCoinsResponse.getCoins().size());
    assertNotEquals(accountCoinsResponse.getCoins().getFirst().getCoinIdentifier(),
        accountCoinsResponse.getCoins().get(2).getCoinIdentifier());
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
    assertEquals(emptyNamePolicyId,
        accountCoinsResponse.getCoins().getFirst().getAmount().getCurrency().getSymbol());
    assertEquals(Constants.MULTI_ASSET_DECIMALS,
        accountCoinsResponse.getCoins().getFirst().getAmount().getCurrency().getDecimals());
    assertEquals(TestConstants.ACCOUNT_BALANCE_MINTED_TOKENS_AMOUNT,
        accountCoinsResponse.getCoins().getFirst().getAmount().getValue());
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
    assertEquals(Constants.LOVELACE,
        accountCoinsResponse.getCoins().getFirst().getAmount().getCurrency().getSymbol());
    assertEquals(Constants.MULTI_ASSET_DECIMALS,
        accountCoinsResponse.getCoins().getFirst().getAmount().getCurrency().getDecimals());
  }

  @Test
  @Disabled("RA-61 issue - validator need to be updated to handle lovelace coins")
  void accountCoinsLovelaceCurrency_Test() {
    ResponseEntity<AccountCoinsResponse> response = restTemplate.postForEntity(
        getAccountCoinsUrl(),
        getAccountCoinsRequestWithCurrencies(TestConstants.RECEIVER_1, lovelace),
        AccountCoinsResponse.class);
    AccountCoinsResponse accountCoinsResponse = response.getBody();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertNotNull(accountCoinsResponse);
    assertEquals(2, accountCoinsResponse.getCoins().size());
    assertEquals("969750",
        accountCoinsResponse.getCoins().getFirst().getAmount().getValue());
    assertEquals(Constants.LOVELACE,
        accountCoinsResponse.getCoins().getFirst().getAmount().getCurrency().getSymbol());
    assertEquals(Constants.MULTI_ASSET_DECIMALS,
        accountCoinsResponse.getCoins().getFirst().getAmount().getCurrency().getDecimals());
  }

  @Test
  @Disabled("RA-61 issue - data mapper need to be updated to handle multi-asset coins")
  void accountCoinsMultipleSpecifiedCurrencies_Test() {
    ResponseEntity<AccountCoinsResponse> response = restTemplate.postForEntity(
        getAccountCoinsUrl(),
        getAccountCoinsRequestWithCurrencies(TestConstants.TEST_ACCOUNT_ADDRESS, ada,
            myAssetCurrency), AccountCoinsResponse.class);
    AccountCoinsResponse accountCoinsResponse = response.getBody();

    assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
    assertNotNull(accountCoinsResponse);
    assertEquals(2, accountCoinsResponse.getCoins().size());
    assertEquals(TestConstants.ACCOUNT_BALANCE_LOVELACE_AMOUNT,
        accountCoinsResponse.getCoins().getFirst().getAmount().getValue());
    assertEquals(Constants.ADA,
        accountCoinsResponse.getCoins().getFirst().getAmount().getCurrency().getSymbol());
    assertEquals(Constants.ADA_DECIMALS,
        accountCoinsResponse.getCoins().getFirst().getAmount().getCurrency().getDecimals());
    assertEquals(TestConstants.ACCOUNT_BALANCE_MINTED_TOKENS_AMOUNT,
        accountCoinsResponse.getCoins().get(1).getAmount().getValue());
    assertEquals(TestConstants.MY_ASSET_SYMBOL,
        accountCoinsResponse.getCoins().get(1).getAmount().getCurrency().getSymbol());
    assertEquals(Constants.MULTI_ASSET_DECIMALS,
        accountCoinsResponse.getCoins().get(1).getAmount().getCurrency().getDecimals());
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
