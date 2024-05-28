package org.cardanofoundation.rosetta.api.account.service;

import java.math.BigInteger;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.openapitools.client.model.Currency;

import org.cardanofoundation.rosetta.api.account.mapper.AddressUtxoEntityToUtxo;
import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.api.account.model.domain.Amt;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.account.model.entity.AddressUtxoEntity;
import org.cardanofoundation.rosetta.api.account.model.repository.AddressUtxoRepository;
import org.cardanofoundation.rosetta.common.util.Formatters;

@Slf4j
@RequiredArgsConstructor
@Component
@Transactional(readOnly = true, propagation = Propagation.NEVER)
public class LedgerAccountServiceImpl implements LedgerAccountService {

  private final AddressUtxoRepository addressUtxoRepository;
  private final AddressUtxoEntityToUtxo addressUtxoEntityToUtxo;

  @Override
  public List<AddressBalance> findBalanceByAddressAndBlock(String address, Long number) {
    log.debug("Finding balance for address {} at block {}", address, number);
    List<AddressUtxoEntity> unspendUtxosByAddressAndBlock = addressUtxoRepository.findUnspentUtxosByAddressAndBlock(
        address, number);
    return mapAndGroupAddressUtxoEntityToAddressBalance(unspendUtxosByAddressAndBlock);
  }

  @Override
  public List<AddressBalance> findBalanceByStakeAddressAndBlock(String stakeAddress,
      Long number) {
    log.debug("Finding balance for Stakeaddress {} at block {}", stakeAddress, number);
    List<AddressUtxoEntity> unspendUtxosByAddressAndBlock = addressUtxoRepository.findUnspentUtxosByStakeAddressAndBlock(
        stakeAddress, number);
    return mapAndGroupAddressUtxoEntityToAddressBalance(unspendUtxosByAddressAndBlock);
  }

  private static List<AddressBalance> mapAndGroupAddressUtxoEntityToAddressBalance(
      List<AddressUtxoEntity> unspendUtxosByAddressAndBlock) {
    Map<String, AddressBalance> map = new HashMap<>();
    unspendUtxosByAddressAndBlock.forEach(addressUtxoEntity -> addressUtxoEntity.getAmounts().forEach(amt -> {
      BigInteger quantity = amt.getQuantity();
      if(map.containsKey(amt.getUnit())) {
        quantity = quantity.add(map.get(amt.getUnit()).quantity());
      }
      map.put(amt.getUnit(), AddressBalance.builder()
                      .address(addressUtxoEntity.getOwnerAddr())
                      .unit(amt.getUnit())
                      .number(addressUtxoEntity.getBlockNumber())
                      .quantity(quantity)
              .build());
    }));
    return map.values().stream().toList();
  }

  @Override
  public List<Utxo> findUtxoByAddressAndCurrency(String address, List<Currency> currencies) {
    log.debug("Finding UTXOs for address {} with currencies {}", address, currencies);
    List<AddressUtxoEntity> addressUtxoEntities = addressUtxoRepository.findUtxosByAddress(address);
    return addressUtxoEntities.stream()
        .map(entity -> createUtxoModel(currencies, entity))
        .toList();
  }

  private Utxo createUtxoModel(List<Currency> currencies, AddressUtxoEntity entity) {
    Utxo utxo = addressUtxoEntityToUtxo.toDto(entity);
    utxo.setAmounts(getAmts(currencies, entity));
    return utxo;
  }

  private static List<Amt> getAmts(List<Currency> currencies, AddressUtxoEntity entity) {
    return currencies.isEmpty()
        ? entity.getAmounts().stream().toList()
        : entity.getAmounts().stream()
            .filter(amt -> isAmountMatchesCurrency(currencies, amt))
            .toList();
  }

  private static boolean isAmountMatchesCurrency(List<Currency> currencies, Amt amt) {
    return currencies.stream()
        .anyMatch(currency -> {
          String currencyUnit = Formatters.isEmptyHexString(currency.getSymbol()) ?
              currency.getMetadata().getPolicyId() :
              currency.getMetadata().getPolicyId() + currency.getSymbol();
          return currencyUnit.equals(amt.getUnit());
        });
  }
}
