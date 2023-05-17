package org.cardanofoundation.rosetta.crawler.service.impl;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.crawler.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.crawler.projection.BlockDto;
import org.cardanofoundation.rosetta.crawler.projection.GenesisBlockDto;
import org.cardanofoundation.rosetta.crawler.repository.BlockRepository;
import org.cardanofoundation.rosetta.crawler.service.BlockService;
import org.cardanofoundation.rosetta.crawler.service.LedgerDataProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {

  private final BlockRepository blockRepository;

  @Autowired
  LedgerDataProviderService ledgerDataProviderService;


  @Override
  public BlockDto findBlock(Long number, String hash) {
    boolean searchBlockZero;
    if (Objects.nonNull(number)) {
      searchBlockZero = (number == 0);
    } else {
      searchBlockZero = false;
    }
    if (searchBlockZero) {
      log.info("[findBlock] Looking for genesis block");
      GenesisBlockDto genesis = ledgerDataProviderService.findGenesisBlock();
      boolean isHashInvalidIfGiven = hash != null && !genesis.getHash().equals(hash);
      if (isHashInvalidIfGiven) {
        log.error("[findBlock] The requested block has an invalid block hash parameter");
        throw ExceptionFactory.blockNotFoundException();
      }
      if (Objects.nonNull(genesis)) {
        return ledgerDataProviderService.findBlock(null, genesis.getHash());
      } else {
        return ledgerDataProviderService.findBlock(null, null);
      }
    }
    boolean searchLatestBlock = (Objects.isNull(hash)) && (Objects.isNull(number));

    log.info("[findBlock] Do we have to look for latestBlock? " + searchLatestBlock);
    long blockNumber =
        searchLatestBlock ? ledgerDataProviderService.findLatestBlockNumber() : number;
    log.info("[findBlock] Looking for block with blockNumber " + blockNumber);
    BlockDto response = ledgerDataProviderService.findBlock(blockNumber, hash);
    if (Objects.nonNull(response)) {
      log.info("[findBlock] Block was found");
    }
    log.debug("[findBlock] Returning response: " + response);
    return response;
  }

  @Override
  public BlockDto getLatestBlock() {
    log.info("[getLatestBlock] About to look for latest block");
    Long latestBlockNumber = ledgerDataProviderService.findLatestBlockNumber();
    log.info("[getLatestBlock] Latest block number is " + latestBlockNumber);
    BlockDto latestBlock = ledgerDataProviderService.findBlock(latestBlockNumber, null);
    if(latestBlock == null){
      log.error("[getLatestBlock] Latest block not found");
      throw ExceptionFactory.blockNotFoundException();
    }
    log.debug("[getLatestBlock] Returning latest block " + latestBlock);
    return latestBlock;
  }

  @Override
  public GenesisBlockDto getGenesisBlock() {
    log.info("[getGenesisBlock] About to look for genesis block");
    GenesisBlockDto genesisBlock = ledgerDataProviderService.findGenesisBlock();
    if(genesisBlock == null){
      log.error("[getGenesisBlock] Genesis block not found");
      throw ExceptionFactory.genesisBlockNotFound();
    }
    log.debug("[getGenesisBlock] Returning genesis block " + genesisBlock);
    return genesisBlock;
  }


}
