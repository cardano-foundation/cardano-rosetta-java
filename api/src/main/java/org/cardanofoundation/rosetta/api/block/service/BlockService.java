package org.cardanofoundation.rosetta.api.block.service;

import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.Tran;

public interface BlockService {

  Block findBlock(Long index, String hash);

  String getPoolDeposit();

  Tran getBlockTransaction(Long blockId, String blockHash, String txHash);

}
