package org.cardanofoundation.rosetta.api.block.model.repository;

import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.search.model.Currency;
import org.cardanofoundation.rosetta.common.spring.OffsetBasedPageRequest;
import org.springframework.data.domain.Page;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

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