package org.cardanofoundation.rosetta.api.account.service.impl;

import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.cardanofoundation.rosetta.common.util.CardanoAddressUtil;
import org.cardanofoundation.rosetta.common.util.ValidationUtil;
import org.springframework.stereotype.Service;
import org.openapitools.client.model.AccountBalanceRequest;
import org.openapitools.client.model.AccountBalanceResponse;
import org.openapitools.client.model.AccountCoinsRequest;
import org.openapitools.client.model.AccountCoinsResponse;
import org.openapitools.client.model.Currency;
import org.openapitools.client.model.PartialBlockIdentifier;

import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.account.service.AccountService;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.service.BlockService;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.common.services.LedgerDataProviderService;


@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

  private final BlockService blockService;
  private final LedgerDataProviderService ledgerDataProviderService;

  @Override
  public AccountBalanceResponse getAccountBalance(AccountBalanceRequest accountBalanceRequest) {
    Long index = null;
    String hash = null;
    String accountAddress = accountBalanceRequest.getAccountIdentifier().getAddress();
    if (Objects.isNull(CardanoAddressUtil.getEraAddressType(accountAddress))) {
      throw ExceptionFactory.invalidAddressError(accountAddress);
    }
    PartialBlockIdentifier blockIdentifier = accountBalanceRequest.getBlockIdentifier();
    log.info("[accountBalance] Looking for block: {} || latest}", blockIdentifier);

    if (Objects.nonNull(blockIdentifier)) {
      index = blockIdentifier.getIndex();
      hash = blockIdentifier.getHash();
    }

    return blockService.findBalanceDataByAddressAndBlock(accountAddress, index, hash);
  }

  @Override
  public AccountCoinsResponse getAccountCoins(AccountCoinsRequest accountCoinsRequest) {
    String accountAddress = accountCoinsRequest.getAccountIdentifier().getAddress();
    List<Currency> currencies = accountCoinsRequest.getCurrencies();
//    accountCoinsRequest.getIncludeMempool(); // TODO

    log.debug("[accountCoins] Request received {}", accountCoinsRequest);
    if (Objects.isNull(CardanoAddressUtil.getEraAddressType(accountAddress))) {
      log.debug("[accountCoins] Address isn't Era");
      throw ExceptionFactory.invalidAddressError(accountAddress);
    }
    log.debug("[accountCoins] Address is Era");
    if (Objects.nonNull(currencies)) {
      ValidationUtil.validateCurrencies(currencies);
    }
    List<Currency> currenciesRequested = ValidationUtil.filterRequestedCurrencies(currencies);
    log.debug("[accountCoins] Filter currency is {}", currenciesRequested);
    Block latestBlock = ledgerDataProviderService.findLatestBlock();
    log.debug("[accountCoins] Latest block is {}", latestBlock);
    List<Utxo> utxos = ledgerDataProviderService.findUtxoByAddressAndCurrency(accountAddress,
        currenciesRequested);
    log.debug("[accountCoins] found {} Utxos for Address {}", utxos.size(), accountAddress);
    return DataMapper.mapToAccountCoinsResponse(latestBlock, utxos);
  }
}
