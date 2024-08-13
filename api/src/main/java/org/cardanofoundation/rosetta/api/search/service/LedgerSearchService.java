package org.cardanofoundation.rosetta.api.search.service;

import java.util.List;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;
import org.openapitools.client.model.Operator;

public interface LedgerSearchService {

  List<BlockTx> searchTransaction(Operator operator, String txHash, String address, UtxoKey utxoKey,
      String symbol, String blockHash, Long blockIndex, Long maxBlock, int page, int size);
}
