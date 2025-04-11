package org.cardanofoundation.rosetta.api.search.service;

import org.springframework.data.domain.Slice;
import org.openapitools.client.model.Operator;

import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;

public interface LedgerSearchService {

  Slice<BlockTx> searchTransaction(Operator operator,
                                   String txHash,
                                   String address,
                                   UtxoKey utxoKey,
                                   String symbol,
                                   String blockHash,
                                   Long blockIndex,
                                   Long maxBlock,
                                   int page,
                                   int size);

}
