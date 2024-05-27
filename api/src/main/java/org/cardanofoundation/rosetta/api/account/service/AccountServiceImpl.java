package org.cardanofoundation.rosetta.api.account.service;

import java.math.BigInteger;
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
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.CurrencyMetadata;
import org.openapitools.client.model.PartialBlockIdentifier;

import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockIdentifierExtended;
import org.cardanofoundation.rosetta.api.block.service.LedgerBlockService;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
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

  @Override
  public AccountBalanceResponse getAccountBalance(AccountBalanceRequest accountBalanceRequest) {
    Long index = null;
    String hash = null;
    String accountAddress = accountBalanceRequest.getAccountIdentifier().getAddress();
    if (Objects.isNull(CardanoAddressUtils.getEraAddressType(accountAddress))) {
      log.error("[findBalanceDataByAddressAndBlock] Provided address is invalid {}", accountAddress);
      throw ExceptionFactory.invalidAddressError(accountAddress);
    }
    PartialBlockIdentifier blockIdentifier = accountBalanceRequest.getBlockIdentifier();
    log.info("[accountBalance] Looking for block: {} || latest}", blockIdentifier);

    if (Objects.nonNull(blockIdentifier)) {
      index = blockIdentifier.getIndex();
      hash = blockIdentifier.getHash();
    }

    return findBalanceDataByAddressAndBlock(accountAddress, index, hash);

  }

  @Override
  public AccountCoinsResponse getAccountCoins(AccountCoinsRequest accountCoinsRequest) {
    String accountAddress = accountCoinsRequest.getAccountIdentifier().getAddress();
    List<Currency> currencies = accountCoinsRequest.getCurrencies();
//    accountCoinsRequest.getIncludeMempool(); // TODO

    log.debug("[accountCoins] Request received {}", accountCoinsRequest);
    if (Objects.isNull(CardanoAddressUtils.getEraAddressType(accountAddress))) {
      log.debug("[accountCoins] Address isn't Era");
      throw ExceptionFactory.invalidAddressError(accountAddress);
    }
    log.debug("[accountCoins] Address is Era");
    if (Objects.nonNull(currencies)) {
      validateCurrencies(currencies);
    }
    List<Currency> currenciesRequested = filterRequestedCurrencies(currencies);
    log.debug("[accountCoins] Filter currency is {}", currenciesRequested);
    Block latestBlock = ledgerBlockService.findLatestBlock();
    log.debug("[accountCoins] Latest block is {}", latestBlock);
    List<Utxo> utxos = ledgerAccountService.findUtxoByAddressAndCurrency(accountAddress,
        currenciesRequested);
    log.debug("[accountCoins] found {} Utxos for Address {}", utxos.size(), accountAddress);
    return DataMapper.mapToAccountCoinsResponse(latestBlock, utxos);
  }

  private AccountBalanceResponse findBalanceDataByAddressAndBlock(String address, Long number,
      String hash) {

    return findBlockOrLast(number, hash)
        .map(blockDto -> {
          log.info("Looking for utxos for address {} and block {}", address, blockDto.getHash());
          if (CardanoAddressUtils.isStakeAddress(address)) {
            log.debug("Address is StakeAddress, get balance for {}", address);
            BigInteger quantity = ledgerAccountService
                .findStakeAddressBalanceQuantityByAddressAndBlock(address, blockDto.getNumber())
                .orElse(BigInteger.ZERO);
            return DataMapper.mapToStakeAddressBalanceResponse(blockDto, quantity);
          } else {
            log.debug("Address isn't StakeAddress");
            List<AddressBalance> balances = ledgerAccountService
                .findBalanceByAddressAndBlock(address, blockDto.getNumber());
            return DataMapper.mapToAccountBalanceResponse(blockDto, balances);
          }
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
    if (currencies != null && currencies.stream().map(Currency::getSymbol)
        .noneMatch(Constants.ADA::equals)) {
      return currencies;
    } else {
      return Collections.emptyList();
    }
  }
}
