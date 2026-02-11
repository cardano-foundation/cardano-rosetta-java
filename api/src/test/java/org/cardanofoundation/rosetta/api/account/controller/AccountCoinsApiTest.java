package org.cardanofoundation.rosetta.api.account.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.openapitools.client.model.*;

import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.BaseSpringMvcSetup;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;
import org.cardanofoundation.rosetta.testgenerator.common.TestTransactionNames;

import static org.cardanofoundation.rosetta.testgenerator.common.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountCoinsApiTest extends BaseSpringMvcSetup {

  private final String myAssetPolicyId = "d97e36383ae494e72b736ace04080f2953934626376ee06cf84adeb4";

  private final String latestTxHashOnZeroSlot = "%s:0".formatted(generatedDataMap.get(
          TestTransactionNames.SIMPLE_NEW_EMPTY_NAME_COINS_TRANSACTION.getName()).txHash());

  private final String expectedTestAccountCoinAmount = "1818351";
  private final CurrencyRequest myAssetCurrency =
      getCurrency(TestConstants.MY_ASSET_SYMBOL,Constants.MULTI_ASSET_DECIMALS, myAssetPolicyId);
  private final CurrencyRequest ada = getCurrency(Constants.ADA, Constants.ADA_DECIMALS);
  private final CurrencyRequest lovelace = getCurrency(Constants.LOVELACE, Constants.MULTI_ASSET_DECIMALS);

  @Test
  void accountCoins2Ada_Test() {
    AccountCoinsResponse accountCoinsResponse = post(getAccountCoinsRequest(TEST_ACCOUNT_ADDRESS));

    assertNotNull(accountCoinsResponse);
    assertEquals(3, accountCoinsResponse.getCoins().size());
    List<CoinTokens> metadata = accountCoinsResponse.getCoins().getFirst().getMetadata()
        .get(latestTxHashOnZeroSlot);
    assertEquals(1, metadata.size());
    assertEquals(expectedTestAccountCoinAmount,
        accountCoinsResponse.getCoins().getFirst().getAmount().getValue());
    assertEquals(TestConstants.ACCOUNT_BALANCE_MINTED_TOKENS_AMOUNT,
        metadata.getFirst().getTokens().getFirst().getValue());
  }

  @Test
  void accountCoins2Lovelace_Test() {
    AccountCoinsResponse accountCoinsResponse = post(getAccountCoinsRequest(RECEIVER_1));

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
    AccountCoinsResponse accountCoinsResponse = post(getAccountCoinsRequest(RECEIVER_2));

    assertNotNull(accountCoinsResponse);
    assertEquals(0, accountCoinsResponse.getCoins().size());
  }

  @Test
  void accountCoinsNoCoinsForStakeAccount_Test() {
    AccountCoinsResponse accountCoinsResponse =
        post(getAccountCoinsRequest(STAKE_ADDRESS_WITH_EARNED_REWARDS));

    assertNotNull(accountCoinsResponse);
    assertEquals(0, accountCoinsResponse.getCoins().size());
  }

  @Test
  void accountCoinsDifferentCoins_Test() {
    AccountCoinsResponse accountCoinsResponse = post(getAccountCoinsRequest(TEST_ACCOUNT_ADDRESS));

    assertNotNull(accountCoinsResponse);
    assertEquals(3, accountCoinsResponse.getCoins().size());
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
    assertEquals(1, metadata.size());
    // Metadata no longer contains policyId - it's not duplicated in response
    assertEquals(TestConstants.ACCOUNT_BALANCE_MINTED_TOKENS_AMOUNT,
        metadata.getFirst().getTokens().getFirst().getValue());
  }

//  @Test
//  void accountCoinsEmptyNameCoin_Test() {
//    AccountCoinsResponse accountCoinsResponse = post(
//        getAccountCoinsRequestWithCurrencies(TEST_ACCOUNT_ADDRESS,
//            getCurrency("\\x", Constants.MULTI_ASSET_DECIMALS, myAssetPolicyId)));
//
//    assertNotNull(accountCoinsResponse);
//    assertEquals(2, accountCoinsResponse.getCoins().size());
//
//    var coins = accountCoinsResponse.getCoins();
//
//    assertEquals(1, accountCoinsResponse.getCoins().getFirst().getMetadata().size());
//
//    assertEquals(latestTxHashOnZeroSlot,
//        accountCoinsResponse.getCoins().getFirst().getCoinIdentifier().getIdentifier());
//
//    assertEquals(Constants.ADA,
//        accountCoinsResponse.getCoins().getFirst().getAmount().getCurrency().getSymbol());
//
//    assertEquals(Constants.ADA_DECIMALS,
//        accountCoinsResponse.getCoins().getFirst().getAmount().getCurrency().getDecimals());
//
//    assertEquals(myAssetPolicyId,
//        accountCoinsResponse.getCoins().getFirst().getMetadata().get(latestTxHashOnZeroSlot)
//            .getFirst().getPolicyId());
//
//    assertEquals(TestConstants.ACCOUNT_BALANCE_MINTED_TOKENS_AMOUNT,
//        accountCoinsResponse.getCoins().getFirst().getMetadata().get(latestTxHashOnZeroSlot).getFirst()
//            .getTokens().getFirst().getValue());
//    Currency mintedTokenCurrency = accountCoinsResponse.getCoins().getFirst().getMetadata()
//        .get(latestTxHashOnZeroSlot).getFirst().getTokens().getFirst().getCurrency();
//    assertEquals("", mintedTokenCurrency.getSymbol());
//    assertEquals(Constants.MULTI_ASSET_DECIMALS, mintedTokenCurrency.getDecimals());
//    assertEquals(myAssetPolicyId, mintedTokenCurrency.getMetadata().getPolicyId());
//  }

  @Test
  void accountCoinsOneSpecifiedCurrency_Test() {
    AccountCoinsResponse accountCoinsResponse =
        post(getAccountCoinsRequestWithCurrencies(RECEIVER_1, ada));

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
    AccountCoinsResponse accountCoinsResponse =
        post(getAccountCoinsRequestWithCurrencies(TEST_ACCOUNT_ADDRESS, ada, myAssetCurrency));

    assertNotNull(accountCoinsResponse);
    assertEquals(3, accountCoinsResponse.getCoins().size());
    Coin coins = accountCoinsResponse.getCoins().getFirst();
    assertEquals(expectedTestAccountCoinAmount, coins.getAmount().getValue());
    assertEquals(Constants.ADA, coins.getAmount().getCurrency().getSymbol());
    assertEquals(Constants.ADA_DECIMALS, coins.getAmount().getCurrency().getDecimals());
    List<CoinTokens> coinsMetadata = coins.getMetadata().values().iterator().next();
    assertEquals(1, coinsMetadata.size());
    assertEquals(TestConstants.ACCOUNT_BALANCE_MINTED_TOKENS_AMOUNT, coinsMetadata.getFirst()
        .getTokens().getFirst().getValue());
    // With TokenRegistry integration, decimals come from metadata instead of default
    assertEquals(6,
        coinsMetadata.getFirst().getTokens().getFirst().getCurrency().getDecimals());
  }

  @Test
  void accountCoinsException_Test() throws Exception {
    AccountCoinsRequest accountCoinsRequest = getAccountCoinsRequest(
        TEST_ACCOUNT_ADDRESS);
    accountCoinsRequest.getAccountIdentifier().setAddress("invalid_address");

    mockMvc.perform(MockMvcRequestBuilders.post("/account/coins")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(accountCoinsRequest)))
        .andExpect(jsonPath("$.code").value(4015))
        .andExpect(jsonPath("$.message").value("Provided address is invalid"))
        .andExpect(jsonPath("$.details.message").value("invalid_address"))
        .andExpect(jsonPath("$.retriable").value(true));
  }

  @Test
  void accountCoinsNonHexCurrencySymbolException_Test() throws Exception {
    AccountCoinsRequest thisIsANonHexString = getAccountCoinsRequestWithCurrencies(TEST_ACCOUNT_ADDRESS,
        getCurrency("thisIsANonHexString", Constants.MULTI_ASSET_DECIMALS));

    mockMvc.perform(MockMvcRequestBuilders.post("/account/coins")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(thisIsANonHexString)))
        .andExpect(jsonPath("$.code").value(4024))
        .andExpect(jsonPath("$.message").value("Invalid token name"))
        .andExpect(jsonPath("$.details.message").value("Given name is thisIsANonHexString"))
        .andExpect(jsonPath("$.retriable").value(true));
  }

  @Test
  void accountCoinsLovelaceCurrencySymbolException_Test() throws Exception {

    mockMvc.perform(MockMvcRequestBuilders.post("/account/coins")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                getAccountCoinsRequestWithCurrencies(RECEIVER_1, lovelace))))
        .andExpect(jsonPath("$.code").value(4024))
        .andExpect(jsonPath("$.message").value("Invalid token name"))
        .andExpect(jsonPath("$.details.message").value("Given name is lovelace"))
        .andExpect(jsonPath("$.retriable").value(true));
  }

  @Test
  void accountCoinsTooLongCurrencyNameException_Test() throws Exception {
    String tooLongCurrencyName = Stream
        .generate(() -> "0")
        .limit(Constants.ASSET_NAME_LENGTH + 2)
        .collect(Collectors.joining());

    AccountCoinsRequest request =
        getAccountCoinsRequestWithCurrencies(TEST_ACCOUNT_ADDRESS,
        getCurrency(tooLongCurrencyName, Constants.MULTI_ASSET_DECIMALS));

    mockMvc.perform(MockMvcRequestBuilders.post("/account/coins")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(jsonPath("$.code").value(4024))
        .andExpect(jsonPath("$.message").value("Invalid token name"))
        .andExpect(jsonPath("$.details.message").value("Given name is " + tooLongCurrencyName))
        .andExpect(jsonPath("$.retriable").value(true));
  }

  @Test
  void accountCoinsTooLongPolicyIdException_Test() throws Exception {
    String tooLongPolicyId = Stream.generate(() -> "w").limit(Constants.POLICY_ID_LENGTH + 2)
        .collect(Collectors.joining());
    AccountCoinsRequest request = getAccountCoinsRequestWithCurrencies(TEST_ACCOUNT_ADDRESS,
        getCurrency(TestConstants.CURRENCY_HEX_SYMBOL, Constants.MULTI_ASSET_DECIMALS)
            .metadata(CurrencyMetadataRequest.builder().policyId(tooLongPolicyId).build()));

    mockMvc.perform(MockMvcRequestBuilders.post("/account/coins")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(jsonPath("$.code").value(4023))
        .andExpect(jsonPath("$.message").value("Invalid policy id"))
        .andExpect(jsonPath("$.details.message").value("Given policy id is " + tooLongPolicyId))
        .andExpect(jsonPath("$.retriable").value(true));
  }

  @Test
  void accountCoinsNonHexPolicyIdException_Test() throws Exception {
    AccountCoinsRequest request = getAccountCoinsRequestWithCurrencies(TEST_ACCOUNT_ADDRESS,
        getCurrency(TestConstants.CURRENCY_HEX_SYMBOL, Constants.MULTI_ASSET_DECIMALS)
            .metadata(CurrencyMetadataRequest.builder().policyId("thisIsNonHexPolicyId").build()));

    mockMvc.perform(MockMvcRequestBuilders.post("/account/coins")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(jsonPath("$.code").value(4023))
        .andExpect(jsonPath("$.message").value("Invalid policy id"))
        .andExpect(jsonPath("$.details.message").value("Given policy id is thisIsNonHexPolicyId"))
        .andExpect(jsonPath("$.retriable").value(true));
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
      CurrencyRequest... currencies) {
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

  private CurrencyRequest getCurrency(String symbol, int decimals) {
    return CurrencyRequest.builder()
        .symbol(symbol)
        .decimals(decimals)
        .build();
  }

  private CurrencyRequest getCurrency(String symbol, int decimals, String policyId) {
    return CurrencyRequest.builder()
        .symbol(symbol)
        .decimals(decimals)
        .metadata(CurrencyMetadataRequest.builder().policyId(policyId).build())
        .build();
  }

private AccountCoinsResponse post(AccountCoinsRequest accountBalanceRequest) {
    try {
      var resp = mockMvc.perform(MockMvcRequestBuilders.post("/account/coins")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(accountBalanceRequest)))
          .andDo(print())
          .andExpect(status().isOk()) //200
          .andReturn()
          .getResponse()
          .getContentAsString();
      return objectMapper.readValue(resp, AccountCoinsResponse.class);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

}
