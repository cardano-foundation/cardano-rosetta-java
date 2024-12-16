package org.cardanofoundation.rosetta.api.account.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.openapitools.client.model.AccountBalanceRequest;
import org.openapitools.client.model.AccountBalanceResponse;
import org.openapitools.client.model.AccountCoinsRequest;
import org.openapitools.client.model.AccountCoinsResponse;
import org.openapitools.client.model.Amount;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.CurrencyMetadata;
import org.openapitools.client.model.PartialBlockIdentifier;

import org.cardanofoundation.rosetta.api.account.mapper.AccountMapper;
import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockIdentifierExtended;
import org.cardanofoundation.rosetta.api.block.service.LedgerBlockService;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.util.CardanoAddressUtils;
import org.cardanofoundation.rosetta.common.util.Constants;

import static org.cardanofoundation.rosetta.common.exception.ExceptionFactory.invalidPolicyIdError;
import static org.cardanofoundation.rosetta.common.exception.ExceptionFactory.invalidTokenNameError;
import static org.cardanofoundation.rosetta.common.util.Formatters.isEmptyHexString;


@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

  private static final Pattern TOKEN_NAME_VALIDATION = Pattern.compile(
      "^[0-9a-fA-F]{0," + Constants.ASSET_NAME_LENGTH + "}$");
  private static final Pattern POLICY_ID_VALIDATION = Pattern.compile(
      "^[0-9a-fA-F]{" + Constants.POLICY_ID_LENGTH + "}$");

  private final LedgerAccountService ledgerAccountService;
  private final LedgerBlockService ledgerBlockService;
  private final AccountMapper accountMapper;

  @Override
  public AccountBalanceResponse getAccountBalance(AccountBalanceRequest accountBalanceRequest) {


    Long index = null;
    String hash = null;
    String accountAddress = accountBalanceRequest.getAccountIdentifier().getAddress();
    CardanoAddressUtils.verifyAddress(accountAddress);

    PartialBlockIdentifier blockIdentifier = accountBalanceRequest.getBlockIdentifier();
    log.info("[accountBalance] Looking for block: {} || latest}", blockIdentifier);

    if (Objects.nonNull(blockIdentifier)) {
      index = blockIdentifier.getIndex();
      hash = blockIdentifier.getHash();
    }

    return findBalanceDataByAddressAndBlock(accountAddress, index, hash, accountBalanceRequest.getCurrencies());

  }

  @Override
  public AccountCoinsResponse getAccountCoins(AccountCoinsRequest accountCoinsRequest) {
    CardanoAddressUtils.verifyAddress(accountCoinsRequest.getAccountIdentifier().getAddress());

    String accountAddress = accountCoinsRequest.getAccountIdentifier().getAddress();
    CardanoAddressUtils.verifyAddress(accountAddress);

    List<Currency> currencies = accountCoinsRequest.getCurrencies();
//    accountCoinsRequest.getIncludeMempool(); // TODO
    log.debug("[accountCoins] Request received {}", accountCoinsRequest);

    if (Objects.nonNull(currencies)) {
      validateCurrencies(currencies);
    }
    List<Currency> currenciesRequested = filterRequestedCurrencies(currencies);
    log.debug("[accountCoins] Filter currency is {}", currenciesRequested);
    BlockIdentifierExtended latestBlock = ledgerBlockService.findLatestBlockIdentifier();
    log.debug("[accountCoins] Latest block is {}", latestBlock);
    List<Utxo> utxos = ledgerAccountService.findUtxoByAddressAndCurrency(accountAddress,
        currenciesRequested);
    log.debug("[accountCoins] found {} Utxos for Address {}", utxos.size(), accountAddress);
    return accountMapper.mapToAccountCoinsResponse(latestBlock, utxos);
  }

  private AccountBalanceResponse findBalanceDataByAddressAndBlock(String address, Long number,
      String hash, List<Currency> currencies) {

    return findBlockOrLast(number, hash)
        .map(blockDto -> {
          log.info("Looking for utxos for address {} and block {}",
              address,
              blockDto.getHash());
          List<AddressBalance> balances;
          if(CardanoAddressUtils.isStakeAddress(address)) {
            balances = ledgerAccountService.findBalanceByStakeAddressAndBlock(address, blockDto.getNumber());
          } else {
            balances = ledgerAccountService.findBalanceByAddressAndBlock(address, blockDto.getNumber());
          }
          AccountBalanceResponse accountBalanceResponse = accountMapper.mapToAccountBalanceResponse(
              blockDto, balances);
          if (Objects.nonNull(currencies) && !currencies.isEmpty()) {
            validateCurrencies(currencies);
            List<Amount> accountBalanceResponseAmounts = accountBalanceResponse.getBalances();
            accountBalanceResponseAmounts.removeIf(b -> currencies.stream().noneMatch(c -> c.getSymbol().equals(b.getCurrency().getSymbol())));
            accountBalanceResponse.setBalances(accountBalanceResponseAmounts);
          }
          return accountBalanceResponse;
        })
        .orElseThrow(ExceptionFactory::blockNotFoundException);
  }

  private Optional<BlockIdentifierExtended> findBlockOrLast(Long number, String hash) {
    if (number != null || hash != null) {
      return ledgerBlockService.findBlockIdentifier(number, hash);
    } else {
      return Optional.of(ledgerBlockService.findLatestBlockIdentifier());
    }
  }

  private void validateCurrencies(List<Currency> currencies) {
    for (Currency currency : currencies) {
      String symbol = currency.getSymbol();
      CurrencyMetadata metadata = currency.getMetadata();
      if (!isTokenNameValid(symbol)) {
        throw invalidTokenNameError("Given name is " + symbol);
      }
      if (!symbol.equals(Constants.ADA)
          && (metadata == null || !isPolicyIdValid(String.valueOf(metadata.getPolicyId())))) {
        String policyId = metadata == null ? null : metadata.getPolicyId();
        throw invalidPolicyIdError("Given policy id is " + policyId);
      }
    }
  }

  private boolean isTokenNameValid(String name) {
    return TOKEN_NAME_VALIDATION.matcher(name).matches() || isEmptyHexString(name);
  }

  private boolean isPolicyIdValid(String policyId) {
    return POLICY_ID_VALIDATION.matcher(policyId).matches();
  }

  private List<Currency> filterRequestedCurrencies(List<Currency> currencies) {
    boolean isAdaAbsent = Optional.ofNullable(currencies)
        .map(c -> c.stream().map(Currency::getSymbol).noneMatch(Constants.ADA::equals))
        .orElse(false);
    return isAdaAbsent ? currencies : Collections.emptyList();
  }
}
