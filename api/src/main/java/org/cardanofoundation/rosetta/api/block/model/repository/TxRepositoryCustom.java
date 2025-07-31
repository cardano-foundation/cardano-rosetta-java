package org.cardanofoundation.rosetta.api.block.model.repository;

import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public interface TxRepositoryCustom {

  List<TxnEntity> findTransactionsByBlockHash(String blockHash);

  Page<TxnEntity> searchTxnEntitiesAND(@Nullable Set<String> txHashes,
                                       @Nullable String blockHash,
                                       @Nullable Long blockNumber,
                                       @Nullable Long maxBlock,
                                       @Nullable Boolean isSuccess,
                                       Pageable pageable);

  Page<TxnEntity> searchTxnEntitiesOR(@Nullable Set<String> txHashes,
                                      @Nullable String blockHash,
                                      @Nullable Long blockNumber,
                                      @Nullable Long maxBlock,
                                      @Nullable Boolean isSuccess,
                                      Pageable pageable);
}