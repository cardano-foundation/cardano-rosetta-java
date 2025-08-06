package org.cardanofoundation.rosetta.api.search.service;

import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;
import org.cardanofoundation.rosetta.api.search.model.Currency;
import org.openapitools.client.model.Operator;
import org.springframework.data.domain.Page;

import javax.annotation.Nullable;

public interface LedgerSearchService {

  Page<BlockTx> searchTransaction(Operator operator,
                                  @Nullable String txHash,
                                  @Nullable String address,
                                  @Nullable UtxoKey utxoKey,
                                  @Nullable Currency currency,
                                  @Nullable String blockHash,
                                  @Nullable Long blockIndex,
                                  @Nullable Long maxBlock,
                                  @Nullable Boolean isSuccess,
                                  long offset,
                                  long limit);

}
