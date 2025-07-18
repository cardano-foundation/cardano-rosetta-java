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
import org.openapitools.client.model.Operator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  @Override
  public Page<BlockTx> searchTransaction(Operator operator,
                                          String txHash,
                                          String address,
                                          UtxoKey utxoKey,
                                          String symbol,
                                          String blockHash,
                                          Long blockNo,
                                          Long maxBlock,
                                          int page,
                                          int size) {
    Pageable pageable = PageRequest.of(page, size);

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
      case AND -> txRepository.searchTxnEntitiesAND(txHashes.isEmpty() ? null : txHashes, blockHash, blockNo, maxBlock, pageable);
      case OR -> txRepository.searchTxnEntitiesOR(txHashes.isEmpty() ? null : txHashes, blockHash, blockNo, maxBlock, pageable);
    };

    return ledgerBlockService.mapTxnEntitiesToBlockTxList(txnEntities);
  }

}
