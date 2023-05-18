package org.cardanofoundation.rosetta.crawler.service;

import org.cardanofoundation.rosetta.crawler.projection.BlockDto;
import org.cardanofoundation.rosetta.crawler.projection.GenesisBlockDto;

public interface BlockService {

    BlockDto findBlock(Long number, String hash);
    BlockDto getLatestBlock();

    GenesisBlockDto getGenesisBlock();
}
