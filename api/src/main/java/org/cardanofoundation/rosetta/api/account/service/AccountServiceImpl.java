package org.cardanofoundation.rosetta.api.account.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.service.LedgerBlockService;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.common.util.CardanoAddressUtils;
import org.cardanofoundation.rosetta.common.util.ValidationUtil;


@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

  private final LedgerAccountService ledgerAccountService;
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
    List<Utxo> utxos = ledgerAccountService.findUtxoByAddressAndCurrency(accountAddress,
        currenciesRequested);
    log.debug("[accountCoins] found {} Utxos for Address {}", utxos.size(), accountAddress);
    return DataMapper.mapToAccountCoinsResponse(latestBlock, utxos);
  }

  private AccountBalanceResponse findBalanceDataByAddressAndBlock(String address, Long number,
      String hash) {

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
          return DataMapper.mapToAccountBalanceResponse(blockDto, balances);
        })
        .orElseThrow(ExceptionFactory::blockNotFoundException);
  }

  private Optional<Block> findBlockOrLast(Long number, String hash) {
    if (number != null || hash != null) {
      return ledgerBlockService.findBlock(number, hash);
    } else {
      return Optional.of(ledgerBlockService.findLatestBlock());
    }
  }
}
