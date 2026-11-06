package org.cardanofoundation.rosetta.api.block.model.repository;

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import org.springframework.data.domain.Page;

import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.search.model.Currency;
import org.cardanofoundation.rosetta.common.spring.OffsetBasedPageRequest;

public interface TxRepositoryCustom {

  List<TxnEntity> findTransactionsByBlockHash(String blockHash);

  Page<TxnEntity> searchTxnEntitiesAND(Set<String> txHashes,
                                       Set<String> addressHashes,
                                       @Nullable String blockHash,
                                       @Nullable Long blockNumber,
                                       @Nullable Long maxBlock,
                                       @Nullable Boolean isSuccess,
                                       @Nullable Currency currency,
                                       OffsetBasedPageRequest offsetBasedPageRequest);

  Page<TxnEntity> searchTxnEntitiesOR(Set<String> txHashes,
                                      Set<String> addressHashes,
                                      @Nullable String blockHash,
                                      @Nullable Long blockNumber,
                                      @Nullable Long maxBlock,
                                      @Nullable Boolean isSuccess,
                                      @Nullable Currency currency,
                                      OffsetBasedPageRequest offsetBasedPageRequest);
}
