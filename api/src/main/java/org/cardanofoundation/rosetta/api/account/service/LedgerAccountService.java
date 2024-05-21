package org.cardanofoundation.rosetta.api.account.service;


import java.util.List;

import org.openapitools.client.model.Currency;

import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.block.model.domain.StakeAddressBalance;

/**
 * Exposes functions to access chain data that has been indexed according to Rosetta API needs.
 */
public interface LedgerAccountService {

  List<AddressBalance> findBalanceByAddressAndBlock(String address, Long number);

  List<Utxo> findUtxoByAddressAndCurrency(String address, List<Currency> currencies);

  List<StakeAddressBalance> findStakeAddressBalanceByAddressAndBlock(String address, Long number);

}
