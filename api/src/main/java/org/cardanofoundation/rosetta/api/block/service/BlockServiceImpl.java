package org.cardanofoundation.rosetta.api.block.service;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockServiceImpl implements BlockService {

  private final LedgerBlockService ledgerBlockService;
  private final ProtocolParamService protocolParamService;

  @Override
  public Block findBlock(Long index, String hash) {
    log.debug("[block] Looking for block: hash={}, index={}", hash, index);

    Optional<Block> blockOpt = ledgerBlockService.findBlock(index, hash);
    if (blockOpt.isPresent()) {
      var block = blockOpt.get();
      log.debug("Block was found, hash={}", block.getHash());

      block.setPoolDeposit(String.valueOf(protocolParamService.findProtocolParameters().getPoolDeposit()));
      log.debug("full data {}", block);

      return block;
    }

    log.error("Block was not found");
    throw ExceptionFactory.blockNotFoundException();
  }

  @Override
  public BlockTx getBlockTransaction(Long blockId, String blockHash, String txHash) {
    return ledgerBlockService
        .findTransactionsByBlock(blockId, blockHash)
        .stream()
        .filter(tr -> tr.getHash().equals(txHash))
        .findFirst()
        .orElseThrow(ExceptionFactory::transactionNotFound);
  }

}
