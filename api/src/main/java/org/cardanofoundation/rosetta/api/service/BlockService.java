package org.cardanofoundation.rosetta.api.service;

import java.util.List;
import org.cardanofoundation.rosetta.api.model.rest.AccountBalanceResponse;
import org.cardanofoundation.rosetta.api.model.rest.Currency;
import org.cardanofoundation.rosetta.api.projection.dto.BlockDto;
import org.cardanofoundation.rosetta.api.projection.dto.BlockUtxos;


public interface BlockService {

  AccountBalanceResponse findBalanceDataByAddressAndBlock(String address,
      Long number,
      String hash);

  BlockUtxos findCoinsDataByAddress(String accountAddress, List<Currency> currenciesRequested);

  BlockDto findBlock(Long number, String hash);


}
