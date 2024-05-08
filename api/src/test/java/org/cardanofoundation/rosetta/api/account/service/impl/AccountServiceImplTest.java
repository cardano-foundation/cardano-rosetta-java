package org.cardanofoundation.rosetta.api.account.service.impl;

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
import org.cardanofoundation.rosetta.api.block.model.domain.StakeAddressBalance;
import org.cardanofoundation.rosetta.api.block.service.LedgerBlockService;
import org.cardanofoundation.rosetta.common.enumeration.StakeAddressPrefix;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.services.LedgerDataProviderService;
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
  LedgerDataProviderService ledgerDataProviderService;
  @Mock
  LedgerBlockService ledgerBlockService;
  @Spy
  @InjectMocks
  AccountServiceImpl accountService;

  private final String HASH = "hash";

  @Test
  void getAccountBalanceNoStakeAddressPositiveTest() {
    String accountAddress = ADDRESS_PREFIX + "1q9g8address_that_pass8";
    PartialBlockIdentifier blockIdentifier = getMockedPartialBlockIdentifier();
    AccountBalanceRequest accountBalanceRequest = Mockito.mock(AccountBalanceRequest.class);
    AccountIdentifier accountIdentifier = getMockedAccountIdentifierAndMockAccountBalanceRequest(
        accountBalanceRequest, blockIdentifier, accountAddress);
    Block block = getMockBlock();
    AddressBalance addressBalance = new AddressBalance(accountAddress, LOVELACE, 1L,
        BigInteger.valueOf(1000L), 1L);
    when(ledgerBlockService.findBlock(1L, HASH)).thenReturn(Optional.of(block));
    when(ledgerDataProviderService.findBalanceByAddressAndBlock(accountAddress, 1L))
        .thenReturn(Collections.singletonList(addressBalance));

    AccountBalanceResponse actual = accountService.getAccountBalance(accountBalanceRequest);

    assertNotNull(actual);
    assertEquals("1000", actual.getBalances().getFirst().getValue());
    assertNotNull(actual.getBalances().getFirst().getCurrency().getSymbol());
    assertEquals(Constants.ADA, actual.getBalances().getFirst().getCurrency().getSymbol());
    assertEquals(Constants.ADA_DECIMALS, actual.getBalances().getFirst().getCurrency().getDecimals());
    assertEquals(blockIdentifier.getIndex(), actual.getBlockIdentifier().getIndex());
    assertEquals(blockIdentifier.getHash(), actual.getBlockIdentifier().getHash());
    verify(ledgerBlockService).findBlock(1L, HASH);
    verify(ledgerDataProviderService).findBalanceByAddressAndBlock(accountAddress, 1L);
    verify(accountBalanceRequest).getAccountIdentifier();
    verify(accountBalanceRequest).getBlockIdentifier();
    verifyNoMoreInteractions(ledgerDataProviderService);
    verifyNoMoreInteractions(accountBalanceRequest);
    verifyNoMoreInteractions(accountIdentifier);
  }

  @Test
  void getAccountBalanceStakeAddressPositiveTest() {
    String accountAddress =
        ADDRESS_PREFIX + StakeAddressPrefix.TEST.getPrefix() + "1q9g8address_that_pass8";
    PartialBlockIdentifier blockIdentifier = getMockedPartialBlockIdentifier();
    AccountBalanceRequest accountBalanceRequest = Mockito.mock(AccountBalanceRequest.class);
    AccountIdentifier accountIdentifier = getMockedAccountIdentifierAndMockAccountBalanceRequest(
        accountBalanceRequest, blockIdentifier, accountAddress);
    StakeAddressBalance addressBalance = Mockito.mock(StakeAddressBalance.class);
    Block block = getMockBlock();
    when(addressBalance.getQuantity()).thenReturn(BigInteger.valueOf(1000L));
    when(ledgerBlockService.findBlock(1L, HASH)).thenReturn(Optional.of(block));
    when(ledgerDataProviderService.findStakeAddressBalanceByAddressAndBlock(accountAddress, 1L))
        .thenReturn(Collections.singletonList(addressBalance));

    AccountBalanceResponse actual = accountService.getAccountBalance(accountBalanceRequest);

    assertNotNull(actual);
    assertEquals("1000", actual.getBalances().getFirst().getValue());
    assertNotNull(actual.getBalances().getFirst().getCurrency().getSymbol());
    assertEquals(blockIdentifier.getIndex(), actual.getBlockIdentifier().getIndex());
    assertEquals(blockIdentifier.getHash(), actual.getBlockIdentifier().getHash());
    verify(ledgerBlockService).findBlock(1L, HASH);
    verify(ledgerDataProviderService).findStakeAddressBalanceByAddressAndBlock(accountAddress, 1L);
    verify(accountBalanceRequest).getAccountIdentifier();
    verify(accountBalanceRequest).getBlockIdentifier();
    verifyNoMoreInteractions(ledgerDataProviderService);
    verifyNoMoreInteractions(accountBalanceRequest);
    verifyNoMoreInteractions(accountIdentifier);
  }

  @Test
  void getAccountBalanceNoStakeAddressNullBlockIdentifierPositiveTest() {
    String accountAddress = ADDRESS_PREFIX + "1q9g8address_that_pass8";
    AccountBalanceRequest accountBalanceRequest = Mockito.mock(AccountBalanceRequest.class);
    AccountIdentifier accountIdentifier = Mockito.mock(AccountIdentifier.class);
    when(accountBalanceRequest.getAccountIdentifier()).thenReturn(accountIdentifier);
    when(accountIdentifier.getAddress()).thenReturn(accountAddress);
    Block block = getMockBlock();
    AddressBalance addressBalance = new AddressBalance(accountAddress, LOVELACE, 1L,
        BigInteger.valueOf(1000L), 1L);
    when(ledgerBlockService.findLatestBlock()).thenReturn(block);
    when(ledgerDataProviderService.findBalanceByAddressAndBlock(accountAddress, 1L))
        .thenReturn(Collections.singletonList(addressBalance));

    AccountBalanceResponse actual = accountService.getAccountBalance(accountBalanceRequest);

    assertNotNull(actual);
    assertEquals("1000", actual.getBalances().getFirst().getValue());
    assertNotNull(actual.getBalances().getFirst().getCurrency().getSymbol());
    assertEquals(Constants.ADA, actual.getBalances().getFirst().getCurrency().getSymbol());
    assertEquals(Constants.ADA_DECIMALS, actual.getBalances().getFirst().getCurrency().getDecimals());
    assertEquals(block.getNumber(), actual.getBlockIdentifier().getIndex());
    assertEquals(block.getHash(), actual.getBlockIdentifier().getHash());
    verify(ledgerBlockService).findLatestBlock();
    verify(ledgerDataProviderService).findBalanceByAddressAndBlock(accountAddress, 1L);
    verify(accountBalanceRequest).getAccountIdentifier();
    verify(accountBalanceRequest).getBlockIdentifier();
    verifyNoMoreInteractions(ledgerDataProviderService);
    verifyNoMoreInteractions(accountBalanceRequest);
    verifyNoMoreInteractions(accountIdentifier);
  }

  @Test
  void getAccountBalanceStakeAddressWithEmptyBalancesThrowTest() {
    String accountAddress =
        ADDRESS_PREFIX + StakeAddressPrefix.TEST.getPrefix() + "1q9g8address_that_pass8";
    PartialBlockIdentifier blockIdentifier = getMockedPartialBlockIdentifier();
    Block block = getMockBlock();
    AccountBalanceRequest accountBalanceRequest = Mockito.mock(AccountBalanceRequest.class);
    AccountIdentifier accountIdentifier = getMockedAccountIdentifierAndMockAccountBalanceRequest(
        accountBalanceRequest, blockIdentifier, accountAddress);
    when(ledgerBlockService.findBlock(1L, HASH)).thenReturn(Optional.of(block));
    when(ledgerDataProviderService.findStakeAddressBalanceByAddressAndBlock(accountAddress, 1L))
        .thenReturn(Collections.emptyList());

    ApiException actualException = assertThrows(ApiException.class,
        () -> accountService.getAccountBalance(accountBalanceRequest));

    assertEquals(RosettaErrorType.INVALID_ADDRESS.getMessage(),
        actualException.getError().getMessage());
    verify(ledgerBlockService).findBlock(1L, HASH);
    verify(ledgerDataProviderService).findStakeAddressBalanceByAddressAndBlock(accountAddress, 1L);
    verify(accountBalanceRequest).getAccountIdentifier();
    verify(accountBalanceRequest).getBlockIdentifier();
    verifyNoMoreInteractions(ledgerDataProviderService);
    verifyNoMoreInteractions(accountBalanceRequest);
    verifyNoMoreInteractions(accountIdentifier);
  }

  @Test
  void getAccountBalanceStakeAddressWithBlockDtoNullThrowTest() {
    String accountAddress =
        ADDRESS_PREFIX + StakeAddressPrefix.TEST.getPrefix() + "1q9g8address_that_pass8";
    PartialBlockIdentifier blockIdentifier = getMockedPartialBlockIdentifier();
    AccountBalanceRequest accountBalanceRequest = Mockito.mock(AccountBalanceRequest.class);
    AccountIdentifier accountIdentifier = getMockedAccountIdentifierAndMockAccountBalanceRequest(
        accountBalanceRequest, blockIdentifier, accountAddress);
    when(ledgerBlockService.findBlock(1L, HASH)).thenReturn(Optional.empty());

    ApiException actualException = assertThrows(ApiException.class,
        () -> accountService.getAccountBalance(accountBalanceRequest));

    assertEquals(RosettaErrorType.BLOCK_NOT_FOUND.getMessage(),
        actualException.getError().getMessage());
    verify(ledgerBlockService).findBlock(1L, HASH);
    verify(accountBalanceRequest).getAccountIdentifier();
    verify(accountBalanceRequest).getBlockIdentifier();
    verifyNoMoreInteractions(ledgerDataProviderService);
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
    String accountAddress =
        ADDRESS_PREFIX + StakeAddressPrefix.TEST.getPrefix() + "1q9g8address_that_pass8";
    PartialBlockIdentifier blockIdentifier = getMockedPartialBlockIdentifier();
    AccountBalanceRequest accountBalanceRequest = Mockito.mock(AccountBalanceRequest.class);
    AccountIdentifier accountIdentifier = getMockedAccountIdentifierAndMockAccountBalanceRequest(
        accountBalanceRequest, blockIdentifier, accountAddress);
    Block block = getMockBlock();
    when(ledgerBlockService.findBlock(1L, HASH)).thenReturn(Optional.of(block));
    when(ledgerDataProviderService.findStakeAddressBalanceByAddressAndBlock(accountAddress, 1L))
        .thenReturn(null);

    ApiException actualException = assertThrows(ApiException.class,
        () -> accountService.getAccountBalance(accountBalanceRequest));

    assertEquals(RosettaErrorType.INVALID_ADDRESS.getMessage(),
        actualException.getError().getMessage());
    verify(ledgerBlockService).findBlock(1L, HASH);
    verify(ledgerDataProviderService).findStakeAddressBalanceByAddressAndBlock(accountAddress, 1L);
    verify(accountBalanceRequest).getAccountIdentifier();
    verify(accountBalanceRequest).getBlockIdentifier();
    verifyNoMoreInteractions(ledgerDataProviderService);
    verifyNoMoreInteractions(accountBalanceRequest);
    verifyNoMoreInteractions(accountIdentifier);
  }

  @Test
  void getAccountCoinsWithCurrenciesPositiveTest() {
    String accountAddress = ADDRESS_PREFIX + "1q9g8address_that_pass8";
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
    when(ledgerDataProviderService.findUtxoByAddressAndCurrency(accountAddress,
        Collections.emptyList())).thenReturn(Collections.singletonList(utxo));

    AccountCoinsResponse actual = accountService.getAccountCoins(accountCoinsRequest);

    verifyPositiveAccountCoinsCase(actual, utxo, block, accountAddress, accountCoinsRequest);
  }

  @Test
  void getAccountCoinsWithNullCurrenciesPositiveTest() {
    String accountAddress = ADDRESS_PREFIX + "1q9g8address_that_pass8";
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
    when(ledgerDataProviderService.findUtxoByAddressAndCurrency(accountAddress,
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

  @NotNull
  private Block getMockBlock() {
    Block block = Mockito.mock(Block.class);
    when(block.getNumber()).thenReturn(1L);
    when(block.getHash()).thenReturn(HASH);
    return block;
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
    verify(ledgerDataProviderService).findUtxoByAddressAndCurrency(accountAddress,
        Collections.emptyList());
    verify(accountCoinsRequest).getAccountIdentifier();
    verify(accountCoinsRequest).getCurrencies();
    verifyNoMoreInteractions(ledgerDataProviderService);
    verifyNoMoreInteractions(accountCoinsRequest);
  }
}
