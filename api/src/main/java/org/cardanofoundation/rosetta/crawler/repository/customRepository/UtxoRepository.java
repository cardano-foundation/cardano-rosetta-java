package org.cardanofoundation.rosetta.crawler.repository.customRepository;

import java.util.List;
import org.cardanofoundation.rosetta.crawler.model.rest.Currency;
import org.cardanofoundation.rosetta.crawler.model.rest.MaBalance;
import org.cardanofoundation.rosetta.crawler.model.rest.Utxo;

public interface UtxoRepository {

  List<Utxo> findUtxoByAddressAndBlock(String address,
      String blockHash,
      List<Currency> currencies);

  List<MaBalance> findMaBalanceByAddressAndBlock(String address,
      String blockHash);
}
