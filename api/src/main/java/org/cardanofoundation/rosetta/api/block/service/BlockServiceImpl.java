package org.cardanofoundation.rosetta.api.block.service;

import java.io.IOException;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.Tran;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.services.LedgerDataProviderService;
import org.cardanofoundation.rosetta.common.util.FileUtils;

import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {

  private final LedgerDataProviderService ledgerDataProviderService;
  @Value("${cardano.rosetta.GENESIS_SHELLEY_PATH}")
  @SuppressWarnings("unused") // Used in getPoolDeposit
  private String genesisPath;


  @Override
  public Block findBlock(Long index, String hash) {

    log.info("[block] Looking for block: hash={}, index={}", hash, index);
    Block block = ledgerDataProviderService.findBlock(index, hash);
    if (nonNull(block)) {
      log.info("[block] Block was found, hash={}", block.getHash());

      block.setPoolDeposit(getPoolDeposit());

      log.debug("[block] full data {}", block);
      return block;
    }
    log.error("[block] Block was not found");
    throw ExceptionFactory.blockNotFoundException();
  }

  @Override
  public String getPoolDeposit() {
    String content;
    try {
      content = FileUtils.fileReader(genesisPath);
    } catch (IOException e) {
      throw new ApiException("Could not read genesis file path", e);
    }
    JSONObject object = new JSONObject(content);
    JSONObject protocolParams = object.getJSONObject("protocolParams");
    // TODO saa Check if this is the right way to get poolDeposit
    String poolDeposit = String.valueOf(protocolParams.get("poolDeposit"));
    log.debug("[poolDeposit] poolDeposit is [{}]", poolDeposit);
    return poolDeposit;
  }

  @Override
  public Optional<Tran> getBlockTransaction(Long blockId, String blockHash, String txHash) {
    return ledgerDataProviderService
        .findTransactionsByBlock(blockId, blockHash)
        .stream()
        .filter(tr -> tr.getHash().equals(txHash))
        .findFirst();

  }

//TODO saa why this findBlock?
//  @Override
//  public Block findBlock(Long number, String hash) {
//    boolean searchBlockZero;
//    if (nonNull(number)) {
//      searchBlockZero = (number == 0);
//    } else {
//      searchBlockZero = false;
//    }
//    if (searchBlockZero) {
//      log.info("[findBlock] Looking for genesis block");
//      GenesisBlock genesis = ledgerDataProviderService.findGenesisBlock();
//      boolean isHashInvalidIfGiven = hash != null && !genesis.getHash().equals(hash);
//      if (isHashInvalidIfGiven) {
//        log.error("[findBlock] The requested block has an invalid block hash parameter");
//        throw ExceptionFactory.blockNotFoundException();
//      }
//      if (nonNull(genesis)) {
//        return ledgerDataProviderService.findBlock(null, genesis.getHash());
//      } else {
//        return ledgerDataProviderService.findBlock(null, null);
//      }
//    }
//    boolean searchLatestBlock = (isNull(hash)) && (isNull(number));
//
//    log.info("[findBlock] Do we have to look for latestBlock? {}", searchLatestBlock);
//    Long blockNumber = searchLatestBlock ? ledgerDataProviderService.findLatestBlockNumber() : number;
//    log.info("[findBlock] Looking for block with blockNumber {}", blockNumber);
//    Block response = ledgerDataProviderService.findBlock(blockNumber, hash);
//    if (nonNull(response)) {
//      log.info("[findBlock] Block was found");
//    }
//    log.debug("[findBlock] Returning response: " + response);
//    return response;
//  }


}
