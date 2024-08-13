package org.cardanofoundation.rosetta.api.search.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.account.model.repository.AddressUtxoRepository;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;
import org.cardanofoundation.rosetta.api.block.model.repository.TxInputRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.TxRepository;
import org.cardanofoundation.rosetta.api.block.service.LedgerBlockService;
import org.openapitools.client.model.Operator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class LedgerSearchServiceImpl implements LedgerSearchService {

  private final TxRepository txRepository;
  private final LedgerBlockService ledgerBlockService;
  private final TxInputRepository txInputRepository;
  private final AddressUtxoRepository addressUtxoRepository;
  @SneakyThrows
  @Override
  public List<BlockTx> searchTransaction(Operator operator, String txHash, String address, UtxoKey utxoKey,
      String symbol, String blockHash, Long blockIndex, Long maxBlock, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    List<TxnEntity> txnEntities;
    List<String> txHashes = new ArrayList<>();
    if(txHash == null) {
      txHash = utxoKey.getTxHash();
    }
    if(txHash != null) {
      txHashes.add(txHash);
    }
    if(utxoKey != null) {
      txHashes.addAll(txInputRepository.findSpentTxHashByUtxoKey(utxoKey.getTxHash(), utxoKey.getOutputIndex()));
    }
    if(address != null) {
      txHashes.addAll(addressUtxoRepository.findTxHashesByOwnerAddr(address));
    }
    if(operator == Operator.AND) {
      txnEntities = txRepository.searchTxnEntitiesAND(txHashes, blockHash, blockIndex, maxBlock);
    } else {
      txnEntities = txRepository.searchTxnEntitiesOR(txHash, address, blockHash, blockIndex,
          maxBlock,
          pageable);
    }
    return ledgerBlockService.mapTxnEntitiesToBlockTxList(txnEntities);
  }
}
