package org.cardanofoundation.rosetta.api.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.account.model.repository.AddressUtxoRepository;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;
import org.cardanofoundation.rosetta.api.block.model.repository.TxInputRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.TxRepository;
import org.cardanofoundation.rosetta.api.block.service.LedgerBlockService;
import org.cardanofoundation.rosetta.api.search.model.Currency;
import org.cardanofoundation.rosetta.common.spring.OffsetBasedPageRequest;
import org.cardanofoundation.rosetta.common.spring.SimpleOffsetBasedPageRequest;
import org.openapitools.client.model.Operator;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LedgerSearchServiceImpl implements LedgerSearchService {

  private final TxRepository txRepository;
  private final LedgerBlockService ledgerBlockService;
  private final TxInputRepository txInputRepository;
  private final AddressUtxoRepository addressUtxoRepository;

  // Note: MAX_UTXO_COUNT limitation has been removed.
  // The TxRepositoryCustomImpl now uses temporary tables for large transaction hash sets,
  // eliminating the need for IN clause parameter limits.

  @Override
  @Transactional  // Override class-level readOnly=true for methods that may use temporary tables
  public Page<BlockTx> searchTransaction(Operator operator,
                                         @Nullable String txHash,
                                         @Nullable String address,
                                         @Nullable UtxoKey utxoKey,
                                         @Nullable Currency currency,
                                         @Nullable String blockHash,
                                         @Nullable Long blockNo,
                                         @Nullable Long maxBlock,
                                         @Nullable Boolean isSuccess,
                                         long offset,
                                         long limit) {
    OffsetBasedPageRequest pageable = new SimpleOffsetBasedPageRequest(offset, (int) limit);

    Set<String> txHashes = new HashSet<>();
    Optional.ofNullable(txHash).ifPresent(txHashes::add);

    Optional<String> addressOptional = Optional.ofNullable(address);
    Set<String> addressTxHashes = new HashSet<>();

    addressOptional.ifPresent(addr -> {
      addressTxHashes.addAll(addressUtxoRepository.findTxHashesByOwnerAddr(addr));
    });

    // If address was set and there weren't any transactions found, return empty list
    if (addressOptional.isPresent() && addressTxHashes.isEmpty()) {
      return Page.empty();
    }

    txHashes.addAll(addressTxHashes);

    Optional.ofNullable(utxoKey).ifPresent(utxo -> {
      txHashes.add(utxo.getTxHash());
      String txHash_ = utxoKey.getTxHash();
      Integer outputIndex = utxoKey.getOutputIndex();

      txHashes.addAll(txInputRepository.findSpentTxHashByUtxoKey(txHash_, outputIndex));
    });

    Page<TxnEntity> txnEntities = switch (operator) {
      case AND -> txRepository.searchTxnEntitiesAND(txHashes, blockHash, blockNo, maxBlock, isSuccess, currency, pageable);
      case OR -> txRepository.searchTxnEntitiesOR(txHashes, blockHash, blockNo, maxBlock, isSuccess, currency, pageable);
    };

    // this mapping is quite expensive, since it involves multiple database queries
    // it enriches data from TxnEntity to BlockTx
    return ledgerBlockService.mapTxnEntitiesToBlockTxList(txnEntities);
  }

}
