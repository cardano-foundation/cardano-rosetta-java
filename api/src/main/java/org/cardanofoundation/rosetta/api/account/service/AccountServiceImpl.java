package org.cardanofoundation.rosetta.api.account.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.openapitools.client.model.*;

import org.cardanofoundation.rosetta.api.account.mapper.AccountMapper;
import org.cardanofoundation.rosetta.api.account.mapper.AddressBalanceMapper;
import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.common.model.AssetFingerprint;
import org.cardanofoundation.rosetta.api.common.model.TokenRegistryCurrencyData;
import org.cardanofoundation.rosetta.api.common.service.TokenRegistryService;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockIdentifierExtended;
import org.cardanofoundation.rosetta.api.block.service.LedgerBlockService;
import org.cardanofoundation.rosetta.client.YaciHttpGateway;
import org.cardanofoundation.rosetta.client.model.domain.StakeAccountInfo;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.util.CardanoAddressUtils;
import org.cardanofoundation.rosetta.common.util.Constants;

import static org.cardanofoundation.rosetta.common.exception.ExceptionFactory.invalidPolicyIdError;
import static org.cardanofoundation.rosetta.common.exception.ExceptionFactory.invalidTokenNameError;
import static org.cardanofoundation.rosetta.common.util.CardanoAddressUtils.isStakeAddress;
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
  private final YaciHttpGateway yaciHttpGateway;
  private final AddressBalanceMapper balanceMapper;
  private final TokenRegistryService tokenRegistryService;

  @Override
  public AccountBalanceResponse getAccountBalance(AccountBalanceRequest accountBalanceRequest) {
    Long index = null;
    String hash = null;
    String accountAddress = accountBalanceRequest.getAccountIdentifier().getAddress();
    CardanoAddressUtils.verifyAddress(accountAddress);

    PartialBlockIdentifier blockIdentifier = accountBalanceRequest.getBlockIdentifier();
    log.debug("[accountBalance] Looking for block: {} || latest}", blockIdentifier);

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

    List<CurrencyRequest> currencies = accountCoinsRequest.getCurrencies();
//    accountCoinsRequest.getIncludeMempool(); // TODO
    log.debug("[accountCoins] Request received {}", accountCoinsRequest);

    if (Objects.nonNull(currencies)) {
      validateCurrencies(currencies);
    }
    List<CurrencyRequest> currenciesRequested = filterRequestedCurrencies(currencies);
    log.debug("[accountCoins] Filter currency is {}", currenciesRequested);
    BlockIdentifierExtended latestBlock = ledgerBlockService.findLatestBlockIdentifier();
    log.debug("[accountCoins] Latest block is {}", latestBlock);
    List<Utxo> utxos = ledgerAccountService.findUtxoByAddressAndCurrency(accountAddress,
            currenciesRequested);
    log.debug("[accountCoins] found {} Utxos for Address {}", utxos.size(), accountAddress);

    // Extract assets from UTXOs and fetch metadata in single batch call
    Map<AssetFingerprint, TokenRegistryCurrencyData> metadataMap = tokenRegistryService.fetchMetadataForUtxos(utxos);

    return accountMapper.mapToAccountCoinsResponse(latestBlock, utxos, metadataMap);
  }

  private AccountBalanceResponse findBalanceDataByAddressAndBlock(String address,
                                                                  Long number,
                                                                  String hash,
                                                                  List<CurrencyRequest> currencies) {

    return findBlockOrLast(number, hash)
            .map(blockDto -> {
              log.debug("Looking for utxos for address {} and block {}",
                      address,
                      blockDto.getHash()
              );

              List<AddressBalance> balances;
              if (isStakeAddress(address)) {
                StakeAccountInfo stakeAccountInfo = yaciHttpGateway.getStakeAccountRewards(address);

                balances = List.of(balanceMapper.convertToAdaAddressBalance(stakeAccountInfo, blockDto.getNumber()));
              } else {
                balances = ledgerAccountService.findBalanceByAddressAndBlock(address, blockDto.getNumber());
              }

              // Extract assets from balances and fetch metadata in single batch call
              Map<AssetFingerprint, TokenRegistryCurrencyData> metadataMap = tokenRegistryService.fetchMetadataForAddressBalances(balances);

              AccountBalanceResponse accountBalanceResponse = accountMapper.mapToAccountBalanceResponse(blockDto, balances, metadataMap);

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
    }

      return Optional.of(ledgerBlockService.findLatestBlockIdentifier());
    }

  private void validateCurrencies(List<CurrencyRequest> currencies) {
    for (CurrencyRequest currency : currencies) {
      String symbol = currency.getSymbol();
      CurrencyMetadataRequest metadata = currency.getMetadata();
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

  private List<CurrencyRequest> filterRequestedCurrencies(List<CurrencyRequest> currencies) {
    boolean isAdaAbsent = Optional.ofNullable(currencies)
            .map(c -> c.stream().map(CurrencyRequest::getSymbol).noneMatch(Constants.ADA::equals))
            .orElse(false);

    return isAdaAbsent ? currencies : Collections.emptyList();
  }

}
