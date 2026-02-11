package org.cardanofoundation.rosetta.api.account.controller;

import java.math.BigInteger;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.openapitools.client.model.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.rosetta.api.BaseSpringMvcSetup;
import org.cardanofoundation.rosetta.client.YaciHttpGateway;
import org.cardanofoundation.rosetta.client.model.domain.StakeAccountInfo;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;
import org.cardanofoundation.rosetta.testgenerator.common.TestTransactionNames;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cardanofoundation.rosetta.testgenerator.common.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountBalanceApiTest extends BaseSpringMvcSetup {

  private final String upToBlockHash = generatedDataMap.get(
          TestTransactionNames.SIMPLE_LOVELACE_FIRST_TRANSACTION.getName()).blockHash();
  private final Long upToBlockNumber = generatedDataMap.get(
          TestTransactionNames.SIMPLE_LOVELACE_FIRST_TRANSACTION.getName()).blockNumber();

  private final String currentAdaBalance = "110001462357";
  private final String previousAdaBalance = "110001635910";

  @MockitoBean
  // we want to replace the real implementation with a mock bean since we do not actually want to test full http layer here but only business logic
  private YaciHttpGateway yaciHttpGateway;

  @BeforeEach
  public void beforeEachSetup() {
    StakeAccountInfo stakeAccountInfo = StakeAccountInfo.builder()
            .stakeAddress(TestConstants.STAKE_ADDRESS_WITH_EARNED_REWARDS)
            .withdrawableAmount(BigInteger.valueOf(1_000_000L))
            .controlledAmount(BigInteger.valueOf(1_000_000L).add(BigInteger.valueOf(1_000_000L)))
            .build();

    when(yaciHttpGateway.getStakeAccountRewards(anyString()))
            .thenReturn(stakeAccountInfo);
  }

  @Test
  void accountBalance2Ada_Test() {
    AccountBalanceResponse accountBalanceResponse = post(newAccBalance(TEST_ACCOUNT_ADDRESS));

    assertNotNull(accountBalanceResponse);
    assertEquals(currentAdaBalance,
            accountBalanceResponse.getBalances().getFirst().getValue());
    assertEquals(Constants.ADA,
            accountBalanceResponse.getBalances().getFirst().getCurrency().getSymbol());
  }


  @Test
  void accountBalance2Lovelace_Test() {
    AccountBalanceResponse accountBalanceResponse = post(newAccBalance(RECEIVER_1));

    assertNotNull(accountBalanceResponse);
    assertEquals(1, accountBalanceResponse.getBalances().size());
    assertAdaCurrency(accountBalanceResponse);
    assertEquals("1939500", accountBalanceResponse.getBalances().getFirst().getValue());
  }

  @Test
  void accountBalanceMintedTokenAndEmptyName_TestORG() {

    AccountBalanceResponse accountBalanceResponse = post(newAccBalance(TEST_ACCOUNT_ADDRESS));
    assertNotNull(accountBalanceResponse);
    assertEquals(3, accountBalanceResponse.getBalances().size());
    assertAdaCurrency(accountBalanceResponse);

    assertEquals(TestConstants.ACCOUNT_BALANCE_MINTED_TOKENS_AMOUNT,
            accountBalanceResponse.getBalances().get(2).getValue());

    assertNotEquals(accountBalanceResponse.getBalances().getFirst().getCurrency().getSymbol(),
            accountBalanceResponse.getBalances().get(2).getCurrency().getSymbol());

    assertEquals("4d794173736574", accountBalanceResponse.getBalances().get(2).getCurrency().getSymbol());
  }

  @Test
  void accountBalanceMintedTokenAndEmptyName_Test() {
    AccountBalanceResponse accountBalanceResponse = post(newAccBalance(TEST_ACCOUNT_ADDRESS));
    assertNotNull(accountBalanceResponse);
    assertEquals(3, accountBalanceResponse.getBalances().size());
    assertAdaCurrency(accountBalanceResponse);

    var maybeEmptySymbolAmount = accountBalanceResponse.getBalances().stream()
            .filter(b -> b.getCurrency().getSymbol().isEmpty())
            .findFirst();

    assertThat(maybeEmptySymbolAmount).isPresent();
    var emptySymbolAmount = maybeEmptySymbolAmount.orElseThrow();

    assertEquals(TestConstants.ACCOUNT_BALANCE_MINTED_TOKENS_AMOUNT,
            emptySymbolAmount.getValue());

    assertNotEquals(accountBalanceResponse.getBalances().getFirst().getCurrency().getSymbol(),
            emptySymbolAmount.getCurrency().getSymbol());

    assertEquals("", emptySymbolAmount.getCurrency().getSymbol());
  }

  @Test
  void accountBalanceUntilBlockByHash_Test() {
    String balanceUpToBlock = "969750";
    AccountBalanceRequest accountBalanceRequest =
            newAccBalanceUntilBlock(RECEIVER_1, null, upToBlockHash);
    AccountBalanceResponse accountBalanceResponse = post(accountBalanceRequest);

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
    AccountBalanceRequest accountBalanceRequest = newAccBalanceUntilBlock(
            RECEIVER_1, upToBlockNumber, null);
    AccountBalanceResponse accountBalanceResponse = post(accountBalanceRequest);

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
    AccountBalanceRequest accountBalanceRequest = newAccBalanceUntilBlock(
            RECEIVER_1, upToBlockNumber, upToBlockHash);
    AccountBalanceResponse accountBalanceResponse = post(accountBalanceRequest);

    assertNotNull(accountBalanceResponse);
    assertEquals(1, accountBalanceResponse.getBalances().size());
    assertEquals(accountBalanceResponse.getBlockIdentifier().getHash(), upToBlockHash);
    assertEquals(accountBalanceResponse.getBlockIdentifier().getIndex(), upToBlockNumber);
    assertEquals(balanceUpToBlock, accountBalanceResponse.getBalances().getFirst().getValue());
    assertAdaCurrency(accountBalanceResponse);
  }

  @Test
  void accountBalanceUntilBlockException_Test() throws Exception {
    AccountBalanceRequest accountBalanceRequest =
            newAccBalanceUntilBlock(TEST_ACCOUNT_ADDRESS, upToBlockNumber + 1L, upToBlockHash);

    mockMvc.perform(MockMvcRequestBuilders.post("/account/balance")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(accountBalanceRequest)))
            .andExpect(jsonPath("$.code").value(4001))
            .andExpect(jsonPath("$.message").value("Block not found"))
            .andExpect(jsonPath("$.retriable").value(true));

  }

  @Test
  void accountBalanceException_Test() throws Exception {
    AccountBalanceRequest accountBalanceRequest = newAccBalance(TEST_ACCOUNT_ADDRESS);
    accountBalanceRequest.getAccountIdentifier().setAddress("invalid_address");

    mockMvc.perform(MockMvcRequestBuilders.post("/account/balance")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(accountBalanceRequest)))
            .andExpect(jsonPath("$.code").value(4015))
            .andExpect(jsonPath("$.message").value("Provided address is invalid"))
            .andExpect(jsonPath("$.details.message").value("invalid_address"))
            .andExpect(jsonPath("$.retriable").value(true));
  }

  @Test
  @Disabled("No test setup for stake address with rewards yet implemented")
  // TODO do a test with rewards?
  void accountBalanceStakeAddressWithNoEarnedRewards_Test() {
    AccountBalanceRequest accountBalanceRequest = newAccBalance(
            TestConstants.STAKE_ADDRESS_WITH_NO_EARNED_REWARDS);
    AccountBalanceResponse accountBalanceResponse = post(accountBalanceRequest);

    assertNotNull(accountBalanceResponse);
    assertEquals(1, accountBalanceResponse.getBalances().size());
    assertEquals("0", accountBalanceResponse.getBalances().getFirst().getValue());
    assertAdaCurrency(accountBalanceResponse);
  }

  @Test
  void accountBalanceStakeAddressUntilBlockNumber_Test() {
    String balanceUpToBlock = "969750";
    AccountBalanceRequest accountBalanceRequest = newAccBalanceUntilBlock(
            STAKE_ADDRESS_WITH_EARNED_REWARDS, upToBlockNumber, upToBlockHash);

    AccountBalanceResponse accountBalanceResponse =  post(accountBalanceRequest);

    assertNotNull(accountBalanceResponse);
    assertEquals(1, accountBalanceResponse.getBalances().size());

//    assertEquals(TestConstants.STAKE_ACCOUNT_BALANCE_AMOUNT,
//            accountBalanceResponse.getBalances().getFirst().getValue());
//    assertAdaCurrency(accountBalanceResponse);
  }

  @Test
  @Disabled("No test setup for minted tokens on stake address yet implemented")
  void accountBalanceStakeAddressWithMintedUtxo_Test() {
    AccountBalanceRequest accountBalanceRequest = newAccBalance(
            TestConstants.STAKE_ADDRESS_WITH_MINTED_TOKENS);
    AccountBalanceResponse accountBalanceResponse =  post(accountBalanceRequest);

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

    AccountBalanceRequest accountBalanceRequest = newAccBalanceUntilBlock(
            TEST_ACCOUNT_ADDRESS, upToBlockNumberTestAccount, null);

    AccountBalanceResponse accountBalanceResponseWith3Tokens =  post(accountBalanceRequest);

    accountBalanceRequest = newAccBalanceUntilBlock(
            TEST_ACCOUNT_ADDRESS, upToBlockNumberTestAccount - 1L, null);
    AccountBalanceResponse accountBalanceResponseWith2Tokens = post(accountBalanceRequest);

    // check the balance on the current block
    assertNotNull(accountBalanceResponseWith3Tokens);
    assertEquals(3, accountBalanceResponseWith3Tokens.getBalances().size());
    assertEquals(previousAdaBalance,
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

  private AccountBalanceRequest newAccBalance(String accountAddress) {
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

  private AccountBalanceRequest newAccBalanceUntilBlock(String accountAddress,
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

  private AccountBalanceResponse post(AccountBalanceRequest accountBalanceRequest) {
    try {
      var resp = mockMvc.perform(MockMvcRequestBuilders.post("/account/balance")
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(accountBalanceRequest)))
              .andDo(print())
              .andExpect(status().isOk()) //200
              .andReturn()
              .getResponse()
              .getContentAsString();
      return objectMapper.readValue(resp, AccountBalanceResponse.class);
    } catch (Exception e) {
      throw new AssertionError(e);
    }

  }
}
