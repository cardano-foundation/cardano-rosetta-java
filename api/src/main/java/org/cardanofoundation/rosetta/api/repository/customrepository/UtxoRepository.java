package org.cardanofoundation.rosetta.api.repository.customrepository;

import java.util.List;
import org.cardanofoundation.rosetta.api.model.rest.Currency;
import org.cardanofoundation.rosetta.api.model.rest.MaBalance;
import org.cardanofoundation.rosetta.api.model.rest.Utxo;

public interface UtxoRepository {

  List<Utxo> findUtxoByAddressAndBlock(String address,
      String blockHash,
      List<Currency> currencies);

  List<MaBalance> findMaBalanceByAddressAndBlock(String address,
      String blockHash);
}
