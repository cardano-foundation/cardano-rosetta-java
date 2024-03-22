package org.cardanofoundation.rosetta.api.data.account;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.cardanofoundation.rosetta.api.IntegrationTest;
import org.cardanofoundation.rosetta.api.account.service.AccountService;
import org.cardanofoundation.rosetta.testgenerator.common.TestConstants;
import org.junit.jupiter.api.Test;
import org.openapitools.client.model.AccountBalanceRequest;
import org.openapitools.client.model.AccountBalanceResponse;
import org.openapitools.client.model.AccountCoinsRequest;
import org.openapitools.client.model.AccountCoinsResponse;
import org.openapitools.client.model.AccountIdentifier;
import org.springframework.beans.factory.annotation.Autowired;

public class AccountApiTest extends IntegrationTest {

  @Autowired
  private AccountService accountService;

  @Test
  public void accountBalance2Ada_Test() {
    AccountBalanceResponse accountBalance = accountService.getAccountBalance(
        AccountBalanceRequest.builder()
            .accountIdentifier(AccountIdentifier.builder()
                .address(TestConstants.TEST_ACCOUNT_ADDRESS)
                .build())
            .build());

    assertEquals(TestConstants.ACCOUNT_BALANCE_ADA_AMOUNT, accountBalance.getBalances().get(0).getValue());
  }

  @Test
  public void accountCoins2Ada_Test() {
    AccountCoinsResponse accountCoins = accountService.getAccountCoins(AccountCoinsRequest.builder()
        .accountIdentifier(AccountIdentifier.builder()
            .address(TestConstants.TEST_ACCOUNT_ADDRESS)
            .build())
        .build());
    assertEquals(1, accountCoins.getCoins().size());
    assertEquals(
        TestConstants.ACCOUNT_BALANCE_ADA_AMOUNT, accountCoins.getCoins().get(0).getAmount().getValue());
  }

}
