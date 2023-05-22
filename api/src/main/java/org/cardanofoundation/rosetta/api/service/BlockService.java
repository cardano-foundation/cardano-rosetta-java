package org.cardanofoundation.rosetta.api.service;

import org.cardanofoundation.rosetta.api.projection.BlockDto;
import org.cardanofoundation.rosetta.api.projection.GenesisBlockDto;

public interface BlockService {

    BlockDto findBlock(Long number, String hash);
    BlockDto getLatestBlock();

    GenesisBlockDto getGenesisBlock();
}
