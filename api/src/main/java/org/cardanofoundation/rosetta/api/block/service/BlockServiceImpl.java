package org.cardanofoundation.rosetta.api.block.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;

import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {

  private final LedgerBlockService ledgerBlockService;
  private final ProtocolParamService protocolParamService;


  @Override
  public Block findBlock(Long index, String hash) {

    log.info("[block] Looking for block: hash={}, index={}", hash, index);
    Block block = ledgerBlockService.findBlock(index, hash);
    if (nonNull(block)) {
      log.info("[block] Block was found, hash={}", block.getHash());

      block.setPoolDeposit(String.valueOf(protocolParamService.getProtocolParameters().getPoolDeposit()));

      log.debug("[block] full data {}", block);
      return block;
    }
    log.error("[block] Block was not found");
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
