package org.cardanofoundation.rosetta.api.account.service.impl;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.api.block.model.dto.BlockDto;
import org.cardanofoundation.rosetta.api.account.model.dto.UtxoDto;
import org.cardanofoundation.rosetta.api.account.service.AccountService;
import org.cardanofoundation.rosetta.api.block.service.BlockService;
import org.cardanofoundation.rosetta.common.services.LedgerDataProviderService;
import org.cardanofoundation.rosetta.common.util.CardanoAddressUtil;
import org.cardanofoundation.rosetta.common.util.ValidationUtil;
import org.openapitools.client.model.*;
import org.springframework.stereotype.Service;

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
    log.info(
        "[accountBalance] Looking for block: {} || latest}",
        accountBalanceRequest.getBlockIdentifier());

    if (Objects.nonNull(accountBalanceRequest.getBlockIdentifier())) {
      index = accountBalanceRequest.getBlockIdentifier().getIndex();
      hash = accountBalanceRequest.getBlockIdentifier().getHash();
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
    BlockDto latestBlock = ledgerDataProviderService.findLatestBlock();
    log.debug("[accountCoins] Latest block is {}", latestBlock);
    List<UtxoDto> utxos = ledgerDataProviderService.findUtxoByAddressAndCurrency(accountAddress, currenciesRequested);
    log.debug("[accountCoins] found {} Utxos for Address {}", utxos.size(), accountAddress);
    return DataMapper.mapToAccountCoinsResponse(latestBlock, utxos);
  }
}
