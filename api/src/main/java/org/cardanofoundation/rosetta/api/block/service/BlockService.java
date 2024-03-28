package org.cardanofoundation.rosetta.api.block.service;

import java.util.Optional;

import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.Tran;

public interface BlockService {

  Block findBlock(Long index, String hash);

  String getPoolDeposit();

  Optional<Tran> getBlockTransaction(Long blockId, String blockHash, String txHash);

}
