package org.cardanofoundation.rosetta.api.block.service;

import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;

public interface BlockService {

  Block findBlock(Long index, String hash);

  BlockTx getBlockTransaction(Long blockId, String blockHash, String txHash);

}
