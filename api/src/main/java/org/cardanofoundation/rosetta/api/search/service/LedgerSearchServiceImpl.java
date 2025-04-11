package org.cardanofoundation.rosetta.api.search.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.openapitools.client.model.Operator;

import org.cardanofoundation.rosetta.api.account.model.repository.AddressUtxoRepository;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;
import org.cardanofoundation.rosetta.api.block.model.repository.TxInputRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.TxRepository;
import org.cardanofoundation.rosetta.api.block.service.LedgerBlockService;

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
  public List<BlockTx> searchTransaction(Operator operator,
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
    List<TxnEntity> txnEntities;
    Set<String> txHashes = new HashSet<>();

    Optional.ofNullable(txHash).ifPresent(txHashes::add);

    Optional<String> addressOptional = Optional.ofNullable(address);
    Set<String> addressTxHashes = new HashSet<>();
    addressOptional.ifPresent(addr -> addressTxHashes.addAll(addressUtxoRepository.findTxHashesByOwnerAddr(addr)));
    // If Address was set and there weren't any transactions found, return empty list

    if (addressOptional.isPresent() && addressTxHashes.isEmpty()) {
      return List.of();
    }

    txHashes.addAll(addressTxHashes);

    Optional.ofNullable(utxoKey).ifPresent(utxo -> {
      txHashes.add(utxo.getTxHash());
      txHashes.addAll(txInputRepository.findSpentTxHashByUtxoKey(utxoKey.getTxHash(), utxoKey.getOutputIndex()));
    });

    if (operator == Operator.AND) {
      txnEntities = txRepository.searchTxnEntitiesAND(txHashes.isEmpty() ? null : txHashes, blockHash, blockNo, maxBlock, pageable);
    } else {
      txnEntities = txRepository.searchTxnEntitiesOR(txHashes.isEmpty() ? null : txHashes, blockHash, blockNo, maxBlock, pageable);
    }

    return ledgerBlockService.mapTxnEntitiesToBlockTxList(txnEntities);
  }

}
