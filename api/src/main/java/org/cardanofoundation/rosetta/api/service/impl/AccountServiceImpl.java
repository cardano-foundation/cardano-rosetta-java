package org.cardanofoundation.rosetta.api.service.impl;

import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.api.mapper.DataMapper;
import org.cardanofoundation.rosetta.api.model.rest.AccountBalanceRequest;
import org.cardanofoundation.rosetta.api.model.rest.AccountBalanceResponse;
import org.cardanofoundation.rosetta.api.model.rest.AccountCoinsRequest;
import org.cardanofoundation.rosetta.api.model.rest.AccountCoinsResponse;
import org.cardanofoundation.rosetta.api.model.rest.Currency;
import org.cardanofoundation.rosetta.api.projection.dto.BlockUtxos;
import org.cardanofoundation.rosetta.api.service.AccountService;
import org.cardanofoundation.rosetta.api.service.BlockService;
import org.cardanofoundation.rosetta.api.service.CardanoService;
import org.cardanofoundation.rosetta.api.util.Validations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {

  @Autowired
  CardanoService cardanoService;
  @Autowired
  BlockService blockService;

  @Override
  public AccountBalanceResponse getAccountBalance(AccountBalanceRequest accountBalanceRequest) {
    String accountAddress = accountBalanceRequest.getAccountIdentifier().getAddress();
    log.debug("[accountBalance] Request received: " + accountBalanceRequest);
    if (Objects.isNull(cardanoService.getEraAddressType(accountAddress))) {
      throw ExceptionFactory.invalidAddressError(accountAddress);
    }
    log.info("[accountBalance] Looking for block: "
        + accountBalanceRequest.getBlockIdentifier().toString()
        + "|| 'latest'}");
    AccountBalanceResponse accountBalanceResponse = blockService.findBalanceDataByAddressAndBlock(
        accountAddress,
        accountBalanceRequest.getBlockIdentifier().getIndex(),
        accountBalanceRequest.getBlockIdentifier().getHash());
    log.debug("[accountBalance] About to return " + accountBalanceResponse);
    return accountBalanceResponse;

  }

  @Override
  public AccountCoinsResponse getAccountCoins(AccountCoinsRequest accountCoinsRequest) {
    String accountAddress = accountCoinsRequest.getAccountIdentifier().getAddress();
    List<Currency> currencies = accountCoinsRequest.getCurrencies();

    log.debug("[accountCoins] Request received " + accountCoinsRequest);
    if (Objects.isNull(cardanoService.getEraAddressType(accountAddress))) {
      log.debug("[accountCoins] Address isn't Era");
      throw ExceptionFactory.invalidAddressError(accountAddress);
    }
    log.debug("[accountCoins] Address is Era");
    if (Objects.nonNull(currencies)) {
      Validations.validateCurrencies(currencies);
    }
    List<Currency> currenciesRequested = Validations.filterRequestedCurrencies(currencies);
    log.debug("[accountCoins] Filter currency is " + currenciesRequested);
    BlockUtxos blockUtxos = blockService.findCoinsDataByAddress(accountAddress,
        currenciesRequested);
    log.debug("[accountCoins] blockUtxos is " + blockUtxos);
    AccountCoinsResponse response = DataMapper.mapToAccountCoinsResponse(blockUtxos);
    log.debug("[accountCoins] About to return " + response);

    return response;
  }
}
