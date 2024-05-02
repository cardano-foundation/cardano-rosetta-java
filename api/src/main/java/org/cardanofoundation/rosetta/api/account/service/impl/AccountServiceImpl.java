package org.cardanofoundation.rosetta.api.account.service.impl;

import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.openapitools.client.model.AccountBalanceRequest;
import org.openapitools.client.model.AccountBalanceResponse;
import org.openapitools.client.model.AccountCoinsRequest;
import org.openapitools.client.model.AccountCoinsResponse;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.PartialBlockIdentifier;

import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.account.service.AccountService;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.StakeAddressBalance;
import org.cardanofoundation.rosetta.api.block.service.LedgerBlockService;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.common.services.LedgerDataProviderService;
import org.cardanofoundation.rosetta.common.util.CardanoAddressUtils;
import org.cardanofoundation.rosetta.common.util.ValidationUtil;


@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

  private final LedgerDataProviderService ledgerDataProviderService;
  private final LedgerBlockService ledgerBlockService;

  @Override
  public AccountBalanceResponse getAccountBalance(AccountBalanceRequest accountBalanceRequest) {
    Long index = null;
    String hash = null;
    String accountAddress = accountBalanceRequest.getAccountIdentifier().getAddress();
    if (Objects.isNull(CardanoAddressUtils.getEraAddressType(accountAddress))) {
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
      ValidationUtil.validateCurrencies(currencies);
    }
    List<Currency> currenciesRequested = ValidationUtil.filterRequestedCurrencies(currencies);
    log.debug("[accountCoins] Filter currency is {}", currenciesRequested);
    Block latestBlock = ledgerBlockService.findLatestBlock();
    log.debug("[accountCoins] Latest block is {}", latestBlock);
    List<Utxo> utxos = ledgerDataProviderService.findUtxoByAddressAndCurrency(accountAddress,
        currenciesRequested);
    log.debug("[accountCoins] found {} Utxos for Address {}", utxos.size(), accountAddress);
    return DataMapper.mapToAccountCoinsResponse(latestBlock, utxos);
  }

  private AccountBalanceResponse findBalanceDataByAddressAndBlock(String address, Long number,
      String hash) {
    Block blockDto;
    if (number != null || hash != null) {
      blockDto = ledgerBlockService.findBlock(number, hash);
    } else {
      blockDto = ledgerBlockService.findLatestBlock();
    }

    if (Objects.isNull(blockDto)) {
      log.error("[findBalanceDataByAddressAndBlock] Block not found");
      throw ExceptionFactory.blockNotFoundException();
    }
    log.info(
        "[findBalanceDataByAddressAndBlock] Looking for utxos for address {} and block {}",
        address,
        blockDto.getHash());
    if (CardanoAddressUtils.isStakeAddress(address)) {
      log.debug("[findBalanceDataByAddressAndBlock] Address is StakeAddress");
      log.debug("[findBalanceDataByAddressAndBlock] About to get balance for {}", address);
      List<StakeAddressBalance> balances = ledgerDataProviderService.findStakeAddressBalanceByAddressAndBlock(
          address, blockDto.getNumber());
      if (Objects.isNull(balances) || balances.isEmpty()) {
        log.error("[findBalanceDataByAddressAndBlock] No balance found for {}", address);
        throw ExceptionFactory.invalidAddressError();
      }
      return DataMapper.mapToStakeAddressBalanceResponse(blockDto, balances.getFirst());
    } else {
      log.debug("[findBalanceDataByAddressAndBlock] Address isn't StakeAddress");

      List<AddressBalance> balances = ledgerDataProviderService.findBalanceByAddressAndBlock(
          address, blockDto.getNumber());
      return DataMapper.mapToAccountBalanceResponse(blockDto, balances);
    }
  }
}
