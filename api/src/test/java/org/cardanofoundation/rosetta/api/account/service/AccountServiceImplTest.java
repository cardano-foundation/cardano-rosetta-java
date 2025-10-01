package org.cardanofoundation.rosetta.api.account.service;

import java.math.BigInteger;
import java.util.*;
import java.util.Optional;
import jakarta.validation.constraints.NotNull;

import org.cardanofoundation.rosetta.api.common.model.AssetFingerprint;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.model.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.cardanofoundation.rosetta.api.account.mapper.*;
import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockIdentifierExtended;
import org.cardanofoundation.rosetta.api.block.service.LedgerBlockService;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.cardanofoundation.rosetta.api.common.service.TokenRegistryService;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.client.YaciHttpGateway;
import org.cardanofoundation.rosetta.client.model.domain.StakeAccountInfo;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.util.Constants;
import org.cardanofoundation.rosetta.common.util.RosettaConstants.RosettaErrorType;

import static org.cardanofoundation.rosetta.common.util.Constants.ADDRESS_PREFIX;
import static org.cardanofoundation.rosetta.common.util.Constants.LOVELACE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

  @Mock
  LedgerAccountService ledgerAccountService;

  @Mock
  LedgerBlockService ledgerBlockService;

  @Mock
  YaciHttpGateway yaciHttpGateway;

  @Mock
  TokenRegistryService tokenRegistryService;

  @Spy
  AddressBalanceMapperImpl addressBalanceMapper;

  AccountMapper accountMapper;
  AccountServiceImpl accountService;
  DataMapper dataMapper;

  private final String HASH = "hash";

  @BeforeEach
  void setUp() {
    // Mock TokenRegistryService to return empty metadata maps
    lenient().when(tokenRegistryService.getTokenMetadataBatch(any())).thenReturn(Collections.emptyMap());
    lenient().when(tokenRegistryService.fetchMetadataForAddressBalances(any())).thenReturn(Collections.emptyMap());
    lenient().when(tokenRegistryService.fetchMetadataForUtxos(any())).thenReturn(Collections.emptyMap());

    // Create real DataMapper instance with its dependency
    org.cardanofoundation.rosetta.api.common.mapper.TokenRegistryMapper tokenRegistryMapper =
        new org.cardanofoundation.rosetta.api.common.mapper.TokenRegistryMapperImpl();
    dataMapper = new DataMapper(tokenRegistryMapper);

    accountMapper = new AccountMapperImpl(new AccountMapperUtil(dataMapper));
    accountService = new AccountServiceImpl(ledgerAccountService, ledgerBlockService, accountMapper, yaciHttpGateway, addressBalanceMapper, tokenRegistryService);
  }

  @Test
  void getAccountBalanceNoStakeAddressPositiveTest() {
    String accountAddress = ADDRESS_PREFIX
            + "1q9ccruvttlfsqwu47ndmapxmk5xa8cc9ngsgj90290tfpysc6gcpmq6ejwgewr49ja0kghws4fdy9t2zecvd7zwqrheqjze0c7";
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
    verify(accountBalanceRequest).getCurrencies();
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
    when(ledgerBlockService.findBlockIdentifier(1L, HASH)).thenReturn(Optional.of(block));

    StakeAccountInfo stakeAccountInfo = StakeAccountInfo.builder()
            .stakeAddress(accountAddress)
            .withdrawableAmount(BigInteger.valueOf(1_000_000L))
            .controlledAmount(BigInteger.valueOf(1_000_000L).add(BigInteger.valueOf(1_000_000L)))
            .build();

    when(yaciHttpGateway.getStakeAccountRewards(eq(accountAddress)))
            .thenReturn(stakeAccountInfo);

    AccountBalanceResponse actual = accountService.getAccountBalance(accountBalanceRequest);

    assertNotNull(actual);
    assertEquals("1000000", actual.getBalances().get(0).getValue());
    assertNotNull(actual.getBalances().getFirst().getCurrency().getSymbol());
    assertEquals("ADA", actual.getBalances().getFirst().getCurrency().getSymbol());
    assertEquals(blockIdentifier.getIndex(), actual.getBlockIdentifier().getIndex());
    assertEquals(blockIdentifier.getHash(), actual.getBlockIdentifier().getHash());
    verify(ledgerBlockService).findBlockIdentifier(1L, HASH);
    verify(yaciHttpGateway).getStakeAccountRewards(accountAddress);
    verify(accountBalanceRequest).getAccountIdentifier();
    verify(accountBalanceRequest).getBlockIdentifier();
    verify(accountBalanceRequest).getCurrencies();
    verifyNoMoreInteractions(ledgerAccountService);
    verifyNoMoreInteractions(accountBalanceRequest);
    verifyNoMoreInteractions(accountIdentifier);
  }

  @Test
  void getFilteredAccountBalance() {
    String address = "addr_test1qz5t8wq55e09usmh07ymxry8atzwxwt2nwwzfngg6esffxvw2pfap6uqmkj3n6zmlrsgz397md2gt7yqs5p255uygaesx608y5";
    when(ledgerAccountService.findBalanceByAddressAndBlock(any(), any()))
            .thenReturn(List.of(AddressBalance.builder().address(address).unit("lovelace").number(10L).quantity(
                            BigInteger.valueOf(10)).build(),
                    AddressBalance.builder().address(address).unit("bd976e131cfc3956b806967b06530e48c20ed5498b46a5eb836b61c2").number(10L).quantity(
                            BigInteger.valueOf(10)).build()));
    BlockIdentifierExtended block = getMockedBlockIdentifierExtended();
    when(ledgerBlockService.findLatestBlockIdentifier()).thenReturn(block);
    when(tokenRegistryService.fetchMetadataForAddressBalances(any())).thenAnswer(invocation -> {
      Map<AssetFingerprint, TokenRegistryCurrencyData> result = new HashMap<>();
      // Create an asset for the native token in the test data
      AssetFingerprint assetFingerprint = AssetFingerprint.of("bd976e131cfc3956b806967b06530e48c20ed5498b46a5eb836b61c2", "");  // Empty asset name
      result.put(assetFingerprint, TokenRegistryCurrencyData.builder()
          .policyId("bd976e131cfc3956b806967b06530e48c20ed5498b46a5eb836b61c2")
          .decimals(0)
          .build());
      return result;
    });
    AccountBalanceRequest accountBalanceRequest = AccountBalanceRequest.builder()
            .accountIdentifier(AccountIdentifier.builder().address(address).build())
            .blockIdentifier(null)
            .currencies(List.of(CurrencyRequest.builder().symbol("ADA").build()))
            .build();
    AccountBalanceResponse accountBalanceResponse = accountService.getAccountBalance(
            accountBalanceRequest);

    assertEquals(1, accountBalanceResponse.getBalances().size());
  }

  @Test
  void getAccountBalanceNoStakeAddressNullBlockIdentifierPositiveTest() {
    // Shelly testnet address
    String accountAddress = "addr_test1vru64wlzn85v7fecg0mz33lh00wlggqtquvzzuhf6vusyes32jz9w";
    AccountBalanceRequest accountBalanceRequest = Mockito.mock(AccountBalanceRequest.class);

    AccountIdentifier accountIdentifier = Mockito.mock(AccountIdentifier.class);
    when(accountBalanceRequest.getAccountIdentifier()).thenReturn(accountIdentifier);
    when(accountBalanceRequest.getBlockIdentifier()).thenReturn(null);
    when(accountBalanceRequest.getCurrencies()).thenReturn(null);
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
    verify(accountBalanceRequest).getCurrencies();
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

    when(ledgerBlockService.findBlockIdentifier(1L, HASH)).thenReturn(Optional.of(block));

    StakeAccountInfo stakeAccountInfo = StakeAccountInfo.builder()
            .stakeAddress(accountAddress)
            .withdrawableAmount(BigInteger.valueOf(1_000_000L))
            .controlledAmount(BigInteger.valueOf(1_000_000L).add(BigInteger.valueOf(1_000_000L)))
            .build();

    when(yaciHttpGateway.getStakeAccountRewards(eq(accountAddress)))
            .thenReturn(stakeAccountInfo);

    AccountBalanceResponse actual = accountService.getAccountBalance(accountBalanceRequest);

    assertEquals(block.getHash(), actual.getBlockIdentifier().getHash());
    assertEquals(block.getNumber(), actual.getBlockIdentifier().getIndex());
    assertEquals("1000000", actual.getBalances().get(0).getValue());
    assertEquals("ADA", actual.getBalances().get(0).getCurrency().getSymbol());

    verify(ledgerBlockService).findBlockIdentifier(1L, HASH);
    verify(yaciHttpGateway).getStakeAccountRewards(accountAddress);
    verify(accountBalanceRequest).getAccountIdentifier();
    verify(accountBalanceRequest).getBlockIdentifier();
    verify(accountBalanceRequest).getCurrencies();
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
    verify(accountBalanceRequest).getCurrencies();
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

    StakeAccountInfo stakeAccountInfo = StakeAccountInfo.builder()
            .stakeAddress(accountAddress)
            .withdrawableAmount(BigInteger.valueOf(1_000_000L))
            .controlledAmount(BigInteger.valueOf(1_000_000L).add(BigInteger.valueOf(1_000_000L)))
            .build();

    when(yaciHttpGateway.getStakeAccountRewards(eq(accountAddress)))
            .thenReturn(stakeAccountInfo);

    when(ledgerBlockService.findBlockIdentifier(1L, HASH)).thenReturn(Optional.of(block));

    AccountBalanceResponse actual = accountService.getAccountBalance(accountBalanceRequest);

    assertEquals(block.getHash(), actual.getBlockIdentifier().getHash());
    assertEquals(block.getNumber(), actual.getBlockIdentifier().getIndex());
    assertEquals("1000000", actual.getBalances().get(0).getValue());
    assertEquals("ADA", actual.getBalances().get(0).getCurrency().getSymbol());
    verify(ledgerBlockService).findBlockIdentifier(1L, HASH);
    verify(yaciHttpGateway).getStakeAccountRewards(accountAddress);
    verify(accountBalanceRequest).getAccountIdentifier();
    verify(accountBalanceRequest).getBlockIdentifier();
    verify(accountBalanceRequest).getCurrencies();
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
    CurrencyRequest currency = Mockito.mock(CurrencyRequest.class);
    BlockIdentifierExtended block = Mockito.mock(BlockIdentifierExtended.class);
    Utxo utxo = Mockito.mock(Utxo.class);
    when(utxo.getTxHash()).thenReturn("txHash");
    when(utxo.getOutputIndex()).thenReturn(1);
    when(utxo.getAmounts()).thenReturn(
            Collections.singletonList(new Amt(LOVELACE, "", LOVELACE, BigInteger.valueOf(1000L))));
    when(accountCoinsRequest.getAccountIdentifier()).thenReturn(accountIdentifier);
    when(accountCoinsRequest.getCurrencies()).thenReturn(Collections.singletonList(currency));
    when(accountIdentifier.getAddress()).thenReturn(accountAddress);
    when(currency.getSymbol()).thenReturn("ADA");
    when(ledgerBlockService.findLatestBlockIdentifier()).thenReturn(block);
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
    BlockIdentifierExtended block = Mockito.mock(BlockIdentifierExtended.class);
    Utxo utxo = Mockito.mock(Utxo.class);
    when(utxo.getTxHash()).thenReturn("txHash");
    when(utxo.getOutputIndex()).thenReturn(1);
    when(utxo.getAmounts()).thenReturn(
            Collections.singletonList(new Amt(LOVELACE, "", LOVELACE, BigInteger.valueOf(1000L))));
    when(accountCoinsRequest.getAccountIdentifier()).thenReturn(accountIdentifier);
    when(accountCoinsRequest.getCurrencies()).thenReturn(null);
    when(accountIdentifier.getAddress()).thenReturn(accountAddress);
    when(ledgerBlockService.findLatestBlockIdentifier()).thenReturn(block);
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
    when(accountBalanceRequest.getCurrencies()).thenReturn(null);
    when(accountIdentifier.getAddress()).thenReturn(accountAddress);
    return accountIdentifier;
  }

  private void verifyPositiveAccountCoinsCase(AccountCoinsResponse actual, Utxo utxo,
                                              BlockIdentifierExtended block,
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
    verify(ledgerBlockService).findLatestBlockIdentifier();
    verify(ledgerAccountService).findUtxoByAddressAndCurrency(accountAddress,
            Collections.emptyList());
    verify(accountCoinsRequest).getCurrencies();
    verifyNoMoreInteractions(ledgerAccountService);
    verifyNoMoreInteractions(accountCoinsRequest);
  }
}
