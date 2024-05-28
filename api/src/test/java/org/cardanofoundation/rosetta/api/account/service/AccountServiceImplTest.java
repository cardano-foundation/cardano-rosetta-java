package org.cardanofoundation.rosetta.api.account.service;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.AccountBalanceRequest;
import org.openapitools.client.model.AccountBalanceResponse;
import org.openapitools.client.model.AccountCoinsRequest;
import org.openapitools.client.model.AccountCoinsResponse;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.PartialBlockIdentifier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockIdentifierExtended;
import org.cardanofoundation.rosetta.api.block.service.LedgerBlockService;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType;

import static org.cardanofoundation.rosetta.common.util.Constants.ADDRESS_PREFIX;
import static org.cardanofoundation.rosetta.common.util.Constants.LOVELACE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

  @Mock
  LedgerAccountService ledgerAccountService;
  @Mock
  LedgerBlockService ledgerBlockService;
  @Spy
  @InjectMocks
  AccountServiceImpl accountService;

  private final String HASH = "hash";

  @Test
  void getAccountBalanceNoStakeAddressPositiveTest() {
    String accountAddress = ADDRESS_PREFIX + "1q9ccruvttlfsqwu47ndmapxmk5xa8cc9ngsgj90290tfpysc6gcpmq6ejwgewr49ja0kghws4fdy9t2zecvd7zwqrheqjze0c7";
    PartialBlockIdentifier blockIdentifier = getMockedPartialBlockIdentifier();
    AccountBalanceRequest accountBalanceRequest = Mockito.mock(AccountBalanceRequest.class);
    AccountIdentifier accountIdentifier = getMockedAccountIdentifierAndMockAccountBalanceRequest(
        accountBalanceRequest, blockIdentifier, accountAddress);
    BlockIdentifierExtended block = getMockedBlockIdentifierExtended();
    AddressBalance addressBalance = new AddressBalance(accountAddress, LOVELACE, 1L,
        BigInteger.valueOf(1000L), 1L);
    when(ledgerBlockService.findBlockIdentifier(1L, HASH)).thenReturn(Optional.of(block));
    when(ledgerAccountService.findBalanceByAddressAndBlock(accountAddress, 1L))
        .thenReturn(Collections.singletonList(addressBalance));

    AccountBalanceResponse actual = accountService.getAccountBalance(accountBalanceRequest);

    assertNotNull(actual);
    assertEquals("1000", actual.getBalances().getFirst().getValue());
    assertNotNull(actual.getBalances().getFirst().getCurrency().getSymbol());
    assertEquals(Constants.ADA, actual.getBalances().getFirst().getCurrency().getSymbol());
    assertEquals(Constants.ADA_DECIMALS,
        actual.getBalances().getFirst().getCurrency().getDecimals());
    assertEquals(blockIdentifier.getIndex(), actual.getBlockIdentifier().getIndex());
    assertEquals(blockIdentifier.getHash(), actual.getBlockIdentifier().getHash());
    verify(ledgerBlockService).findBlockIdentifier(1L, HASH);
    verify(ledgerAccountService).findBalanceByAddressAndBlock(accountAddress, 1L);
    verify(accountBalanceRequest).getAccountIdentifier();
    verify(accountBalanceRequest).getBlockIdentifier();
    verifyNoMoreInteractions(ledgerAccountService);
    verifyNoMoreInteractions(accountBalanceRequest);
    verifyNoMoreInteractions(accountIdentifier);
  }

  @Test
  void getAccountBalanceStakeAddressPositiveTest() {
    String accountAddress = "stake_test1urpal63jnzqjkhn7ld5dfjzvesjs07u4pn2y88yk2a8250qgvnqhw";
    PartialBlockIdentifier blockIdentifier = getMockedPartialBlockIdentifier();
    AccountBalanceRequest accountBalanceRequest = Mockito.mock(AccountBalanceRequest.class);
    AccountIdentifier accountIdentifier = getMockedAccountIdentifierAndMockAccountBalanceRequest(
        accountBalanceRequest, blockIdentifier, accountAddress);
    BlockIdentifierExtended block = getMockedBlockIdentifierExtended();
    AddressBalance mock = getMockedAddressBalance();
    when(ledgerBlockService.findBlockIdentifier(1L, HASH)).thenReturn(Optional.of(block));
    when(ledgerAccountService.findBalanceByStakeAddressAndBlock(accountAddress, 1L))
        .thenReturn(Collections.singletonList(mock));

    AccountBalanceResponse actual = accountService.getAccountBalance(accountBalanceRequest);

    assertNotNull(actual);
    assertEquals("1000", actual.getBalances().getFirst().getValue());
    assertNotNull(actual.getBalances().getFirst().getCurrency().getSymbol());
    assertEquals(blockIdentifier.getIndex(), actual.getBlockIdentifier().getIndex());
    assertEquals(blockIdentifier.getHash(), actual.getBlockIdentifier().getHash());
    verify(ledgerBlockService).findBlockIdentifier(1L, HASH);
    verify(ledgerAccountService)
        .findBalanceByStakeAddressAndBlock(accountAddress, 1L);
    verify(accountBalanceRequest).getAccountIdentifier();
    verify(accountBalanceRequest).getBlockIdentifier();
    verifyNoMoreInteractions(ledgerAccountService);
    verifyNoMoreInteractions(accountBalanceRequest);
    verifyNoMoreInteractions(accountIdentifier);
  }

  @NotNull
  private static AddressBalance getMockedAddressBalance() {
    AddressBalance mock = Mockito.mock(AddressBalance.class);
    when(mock.unit()).thenReturn("089eb57344dcfa1d2d82749566f27aa5c072194d11a261d6e66f33cc4c4943454e5345");
    when(mock.quantity()).thenReturn(BigInteger.valueOf(1000L));
    return mock;
  }

  @Test
  void getAccountBalanceNoStakeAddressNullBlockIdentifierPositiveTest() {
    // Shelly testnet address
    String accountAddress = "addr_test1vru64wlzn85v7fecg0mz33lh00wlggqtquvzzuhf6vusyes32jz9w";
    AccountBalanceRequest accountBalanceRequest = Mockito.mock(AccountBalanceRequest.class);
    AccountIdentifier accountIdentifier = Mockito.mock(AccountIdentifier.class);
    when(accountBalanceRequest.getAccountIdentifier()).thenReturn(accountIdentifier);
    when(accountIdentifier.getAddress()).thenReturn(accountAddress);
    BlockIdentifierExtended block = getMockedBlockIdentifierExtended();
    AddressBalance addressBalance = new AddressBalance(accountAddress, LOVELACE, 1L,
        BigInteger.valueOf(1000L), 1L);
    when(ledgerBlockService.findLatestBlockIdentifier()).thenReturn(block);
    when(ledgerAccountService.findBalanceByAddressAndBlock(accountAddress, 1L))
        .thenReturn(Collections.singletonList(addressBalance));

    AccountBalanceResponse actual = accountService.getAccountBalance(accountBalanceRequest);

    assertNotNull(actual);
    assertEquals("1000", actual.getBalances().getFirst().getValue());
    assertNotNull(actual.getBalances().getFirst().getCurrency().getSymbol());
    assertEquals(Constants.ADA, actual.getBalances().getFirst().getCurrency().getSymbol());
    assertEquals(Constants.ADA_DECIMALS,
        actual.getBalances().getFirst().getCurrency().getDecimals());
    assertEquals(block.getNumber(), actual.getBlockIdentifier().getIndex());
    assertEquals(block.getHash(), actual.getBlockIdentifier().getHash());
    verify(ledgerBlockService).findLatestBlockIdentifier();
    verify(ledgerAccountService).findBalanceByAddressAndBlock(accountAddress, 1L);
    verify(accountBalanceRequest).getAccountIdentifier();
    verify(accountBalanceRequest).getBlockIdentifier();
    verifyNoMoreInteractions(ledgerAccountService);
    verifyNoMoreInteractions(accountBalanceRequest);
    verifyNoMoreInteractions(accountIdentifier);
  }

  @Test
  void getAccountBalanceStakeAddressWithEmptyBalancesThrowTest() {
    String accountAddress = "stake_test1uzjspr9ux2w3jwevcxx954n7al0z3wnj2zlyff68mdwqmlglx3p5m";
    PartialBlockIdentifier blockIdentifier = getMockedPartialBlockIdentifier();
    BlockIdentifierExtended block = getMockedBlockIdentifierExtended();
    AccountBalanceRequest accountBalanceRequest = Mockito.mock(AccountBalanceRequest.class);
    AccountIdentifier accountIdentifier = getMockedAccountIdentifierAndMockAccountBalanceRequest(
        accountBalanceRequest, blockIdentifier, accountAddress);
    AddressBalance mock = getMockedAddressBalance();
    when(ledgerBlockService.findBlockIdentifier(1L, HASH)).thenReturn(Optional.of(block));
    when(ledgerAccountService.findBalanceByStakeAddressAndBlock(accountAddress, 1L))
        .thenReturn(Collections.singletonList(mock));

    AccountBalanceResponse actual = accountService.getAccountBalance(accountBalanceRequest);

    assertEquals(block.getHash(), actual.getBlockIdentifier().getHash());
    assertEquals(block.getNumber(), actual.getBlockIdentifier().getIndex());
    assertEquals("1000", actual.getBalances().getFirst().getValue());
    assertEquals("4c4943454e5345", actual.getBalances().getFirst().getCurrency().getSymbol());
    verify(ledgerBlockService).findBlockIdentifier(1L, HASH);
    verify(ledgerAccountService)
        .findBalanceByStakeAddressAndBlock(accountAddress, 1L);
    verify(accountBalanceRequest).getAccountIdentifier();
    verify(accountBalanceRequest).getBlockIdentifier();
    verifyNoMoreInteractions(ledgerAccountService);
    verifyNoMoreInteractions(accountBalanceRequest);
    verifyNoMoreInteractions(accountIdentifier);
  }

  @Test
  void getAccountBalanceStakeAddressWithBlockDtoNullThrowTest() {
    String accountAddress = "stake_test1upg95j7372g4xhalw69xum09nvumrrpazkxug02vlsy8w8ckxuqmh";
    PartialBlockIdentifier blockIdentifier = getMockedPartialBlockIdentifier();
    AccountBalanceRequest accountBalanceRequest = Mockito.mock(AccountBalanceRequest.class);
    AccountIdentifier accountIdentifier = getMockedAccountIdentifierAndMockAccountBalanceRequest(
        accountBalanceRequest, blockIdentifier, accountAddress);
    when(ledgerBlockService.findBlockIdentifier(1L, HASH)).thenReturn(Optional.empty());

    ApiException actualException = assertThrows(ApiException.class,
        () -> accountService.getAccountBalance(accountBalanceRequest));

    assertEquals(RosettaErrorType.BLOCK_NOT_FOUND.getMessage(),
        actualException.getError().getMessage());
    verify(ledgerBlockService).findBlockIdentifier(1L, HASH);
    verify(accountBalanceRequest).getAccountIdentifier();
    verify(accountBalanceRequest).getBlockIdentifier();
    verifyNoMoreInteractions(ledgerAccountService);
    verifyNoMoreInteractions(accountBalanceRequest);
    verifyNoMoreInteractions(accountIdentifier);
  }

  @Test
  void getAccountBalanceInvalidAddressThrowTest() {
    String accountAddress = "invalidAddress";
    AccountBalanceRequest accountBalanceRequest = Mockito.mock(AccountBalanceRequest.class);
    AccountIdentifier accountIdentifier = Mockito.mock(AccountIdentifier.class);
    when(accountBalanceRequest.getAccountIdentifier()).thenReturn(accountIdentifier);
    when(accountIdentifier.getAddress()).thenReturn(accountAddress);

    ApiException actualException = assertThrows(ApiException.class,
        () -> accountService.getAccountBalance(accountBalanceRequest));

    assertEquals(RosettaErrorType.INVALID_ADDRESS.getMessage(),
        actualException.getError().getMessage());
    verify(accountBalanceRequest).getAccountIdentifier();
    verifyNoMoreInteractions(accountBalanceRequest);
    verifyNoMoreInteractions(accountIdentifier);
  }

  @Test
  void getAccountBalanceWithStakeAddressAndNullBalanceThrowTest() {
    String accountAddress = "stake1ux4dtdmhnj9xu7txtwsn2zdxzc6gmef5na875rcumvzq4ws4gd8qj";
    PartialBlockIdentifier blockIdentifier = getMockedPartialBlockIdentifier();
    AccountBalanceRequest accountBalanceRequest = Mockito.mock(AccountBalanceRequest.class);
    AccountIdentifier accountIdentifier = getMockedAccountIdentifierAndMockAccountBalanceRequest(
        accountBalanceRequest, blockIdentifier, accountAddress);
    BlockIdentifierExtended block = getMockedBlockIdentifierExtended();
    AddressBalance mock = getMockedAddressBalance();
    when(ledgerBlockService.findBlockIdentifier(1L, HASH)).thenReturn(Optional.of(block));
    when(ledgerAccountService.findBalanceByStakeAddressAndBlock(accountAddress, 1L))
        .thenReturn(Collections.singletonList(mock));

    AccountBalanceResponse actual = accountService.getAccountBalance(accountBalanceRequest);

    assertEquals(block.getHash(), actual.getBlockIdentifier().getHash());
    assertEquals(block.getNumber(), actual.getBlockIdentifier().getIndex());
    assertEquals("1000", actual.getBalances().getFirst().getValue());
    assertEquals("4c4943454e5345", actual.getBalances().getFirst().getCurrency().getSymbol());
    verify(ledgerBlockService).findBlockIdentifier(1L, HASH);
    verify(ledgerAccountService)
        .findBalanceByStakeAddressAndBlock(accountAddress, 1L);
    verify(accountBalanceRequest).getAccountIdentifier();
    verify(accountBalanceRequest).getBlockIdentifier();
    verifyNoMoreInteractions(ledgerAccountService);
    verifyNoMoreInteractions(accountBalanceRequest);
    verifyNoMoreInteractions(accountIdentifier);
  }

  @Test
  void getAccountCoinsWithCurrenciesPositiveTest() {
    // Icarus address
    String accountAddress = "Ae2tdPwUPEZGvXJ3ebp4LDgBhbxekAH2oKZgfahKq896fehv8oCJxmGJgLt";
    AccountCoinsRequest accountCoinsRequest = Mockito.mock(AccountCoinsRequest.class);
    AccountIdentifier accountIdentifier = Mockito.mock(AccountIdentifier.class);
    Currency currency = Mockito.mock(Currency.class);
    Block block = Mockito.mock(Block.class);
    Utxo utxo = Mockito.mock(Utxo.class);
    when(utxo.getTxHash()).thenReturn("txHash");
    when(utxo.getOutputIndex()).thenReturn(1);
    when(utxo.getAmounts()).thenReturn(
        Collections.singletonList(new Amt(LOVELACE, "", LOVELACE, BigInteger.valueOf(1000L))));
    when(accountCoinsRequest.getAccountIdentifier()).thenReturn(accountIdentifier);
    when(accountCoinsRequest.getCurrencies()).thenReturn(Collections.singletonList(currency));
    when(accountIdentifier.getAddress()).thenReturn(accountAddress);
    when(currency.getSymbol()).thenReturn("ADA");
    when(ledgerBlockService.findLatestBlock()).thenReturn(block);
    when(ledgerAccountService.findUtxoByAddressAndCurrency(accountAddress,
        Collections.emptyList())).thenReturn(Collections.singletonList(utxo));

    AccountCoinsResponse actual = accountService.getAccountCoins(accountCoinsRequest);

    verifyPositiveAccountCoinsCase(actual, utxo, block, accountAddress, accountCoinsRequest);
  }

  @Test
  void getAccountCoinsWithNullCurrenciesPositiveTest() {
    // Byron address
    String accountAddress = "DdzFFzCqrht9fvu17fiXwiuP82kKEhiGsDByRE7PWfMktrd8Jc1jWqKxubpz21mWjUMh8bWsKuP5JUF9CgUefyABDBsq326ybHrEhB7M";
    AccountCoinsRequest accountCoinsRequest = Mockito.mock(AccountCoinsRequest.class);
    AccountIdentifier accountIdentifier = Mockito.mock(AccountIdentifier.class);
    Block block = Mockito.mock(Block.class);
    Utxo utxo = Mockito.mock(Utxo.class);
    when(utxo.getTxHash()).thenReturn("txHash");
    when(utxo.getOutputIndex()).thenReturn(1);
    when(utxo.getAmounts()).thenReturn(
        Collections.singletonList(new Amt(LOVELACE, "", LOVELACE, BigInteger.valueOf(1000L))));
    when(accountCoinsRequest.getAccountIdentifier()).thenReturn(accountIdentifier);
    when(accountCoinsRequest.getCurrencies()).thenReturn(null);
    when(accountIdentifier.getAddress()).thenReturn(accountAddress);
    when(ledgerBlockService.findLatestBlock()).thenReturn(block);
    when(ledgerAccountService.findUtxoByAddressAndCurrency(accountAddress,
        Collections.emptyList())).thenReturn(Collections.singletonList(utxo));

    AccountCoinsResponse actual = accountService.getAccountCoins(accountCoinsRequest);

    verifyPositiveAccountCoinsCase(actual, utxo, block, accountAddress, accountCoinsRequest);
  }

  @Test
  void getAccountCoinsInvalidAddressThrowTest() {
    String accountAddress = "invalidAddress";
    AccountCoinsRequest accountCoinsRequest = Mockito.mock(AccountCoinsRequest.class);
    AccountIdentifier accountIdentifier = Mockito.mock(AccountIdentifier.class);
    when(accountCoinsRequest.getAccountIdentifier()).thenReturn(accountIdentifier);
    when(accountIdentifier.getAddress()).thenReturn(accountAddress);

    ApiException actualException = assertThrows(ApiException.class,
        () -> accountService.getAccountCoins(accountCoinsRequest));

    assertEquals(RosettaErrorType.INVALID_ADDRESS.getMessage(),
        actualException.getError().getMessage());
    verify(accountCoinsRequest).getAccountIdentifier();
    verify(accountCoinsRequest).getCurrencies();
    verifyNoMoreInteractions(accountCoinsRequest);
    verifyNoMoreInteractions(accountIdentifier);
  }

  private BlockIdentifierExtended getMockedBlockIdentifierExtended() {
    BlockIdentifierExtended blockIdentifier = Mockito.mock(BlockIdentifierExtended.class);
    when(blockIdentifier.getNumber()).thenReturn(1L);
    when(blockIdentifier.getHash()).thenReturn(HASH);
    return blockIdentifier;
  }

  @NotNull
  private PartialBlockIdentifier getMockedPartialBlockIdentifier() {
    PartialBlockIdentifier blockIdentifier = Mockito.mock(PartialBlockIdentifier.class);
    when(blockIdentifier.getIndex()).thenReturn(1L);
    when(blockIdentifier.getHash()).thenReturn(HASH);
    return blockIdentifier;
  }

  @NotNull
  private static AccountIdentifier getMockedAccountIdentifierAndMockAccountBalanceRequest(
      AccountBalanceRequest accountBalanceRequest,
      PartialBlockIdentifier blockIdentifier, String accountAddress) {
    AccountIdentifier accountIdentifier = Mockito.mock(AccountIdentifier.class);
    when(accountBalanceRequest.getAccountIdentifier()).thenReturn(accountIdentifier);
    when(accountBalanceRequest.getBlockIdentifier()).thenReturn(blockIdentifier);
    when(accountIdentifier.getAddress()).thenReturn(accountAddress);
    return accountIdentifier;
  }

  private void verifyPositiveAccountCoinsCase(AccountCoinsResponse actual, Utxo utxo, Block block,
      String accountAddress,
      AccountCoinsRequest accountCoinsRequest) {
    assertNotNull(actual);
    assertEquals(1, actual.getCoins().size());
    assertEquals(utxo.getTxHash() + ":" + utxo.getOutputIndex(),
        actual.getCoins().getFirst().getCoinIdentifier().getIdentifier());
    assertEquals(utxo.getAmounts().getFirst().getQuantity().toString(),
        actual.getCoins().getFirst().getAmount().getValue());
    assertEquals(Constants.ADA, actual.getCoins().getFirst().getAmount().getCurrency().getSymbol());
    assertEquals(block.getHash(), actual.getBlockIdentifier().getHash());
    assertEquals(block.getNumber(), actual.getBlockIdentifier().getIndex());
    verify(ledgerBlockService).findLatestBlock();
    verify(ledgerAccountService).findUtxoByAddressAndCurrency(accountAddress,
        Collections.emptyList());
    verify(accountCoinsRequest).getAccountIdentifier();
    verify(accountCoinsRequest).getCurrencies();
    verifyNoMoreInteractions(ledgerAccountService);
    verifyNoMoreInteractions(accountCoinsRequest);
  }
}
