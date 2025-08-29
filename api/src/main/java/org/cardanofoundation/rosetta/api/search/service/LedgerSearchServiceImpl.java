package org.cardanofoundation.rosetta.api.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.account.model.repository.AddressUtxoRepository;
import org.cardanofoundation.rosetta.api.account.service.AddressHistoryService;
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
  private final AddressHistoryService addressHistoryService;

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

    // Separate transaction hashes into plain hashes and address-related hashes
    final Set<String> plainTxHashes = new HashSet<>();
    final Set<String> addressRelatedHashes = new HashSet<>();

    // Add direct transaction hash to plainTxHashes
    Optional.ofNullable(txHash).ifPresent(plainTxHashes::add);

    // Process address-related hashes
    Optional<String> addressOptional = Optional.ofNullable(address);
    addressOptional.ifPresent(addr -> {
      addressRelatedHashes.addAll(addressHistoryService.findCompleteTransactionHistoryByAddress(addr));
    });

    // If address was set and there weren't any transactions found, return empty list
    if (addressOptional.isPresent() && addressRelatedHashes.isEmpty()) {
      return Page.empty();
    }

    // Process UTXO-related hashes - these go into plainTxHashes since they're direct lookups
    Optional.ofNullable(utxoKey).ifPresent(utxo -> {
      plainTxHashes.add(utxo.getTxHash());
      String txHash_ = utxoKey.getTxHash();
      Integer outputIndex = utxoKey.getOutputIndex();
      plainTxHashes.addAll(txInputRepository.findSpentTxHashByUtxoKey(txHash_, outputIndex));
    });

    // Use the final sets or empty sets if they're empty
    final Set<String> finalPlainTxHashes = plainTxHashes.isEmpty() ? Set.of() : plainTxHashes;
    final Set<String> finalAddressRelatedHashes = addressRelatedHashes.isEmpty() ? Set.of() : addressRelatedHashes;

    Page<TxnEntity> txnEntities = switch (operator) {
      case AND -> txRepository.searchTxnEntitiesAND(finalPlainTxHashes, finalAddressRelatedHashes, blockHash, blockNo, maxBlock, isSuccess, currency, pageable);
      case OR -> txRepository.searchTxnEntitiesOR(finalPlainTxHashes, finalAddressRelatedHashes, blockHash, blockNo, maxBlock, isSuccess, currency, pageable);
    };

    // this mapping is quite expensive, since it involves multiple database queries
    // it enriches data from TxnEntity to BlockTx
    return ledgerBlockService.mapTxnEntitiesToBlockTxList(txnEntities);
  }

}
