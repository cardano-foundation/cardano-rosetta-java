package org.cardanofoundation.rosetta.api.block.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.json.JSONObject;
import org.openapitools.client.model.BlockIdentifier;
import org.openapitools.client.model.BlockTransactionRequest;
import org.openapitools.client.model.BlockTransactionResponse;

import org.cardanofoundation.rosetta.api.block.mapper.BlockToBlockResponse;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.Transaction;
import org.cardanofoundation.rosetta.common.exception.ApiException;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.common.services.LedgerDataProviderService;
import org.cardanofoundation.rosetta.common.util.FileUtils;

import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {

  private final LedgerDataProviderService ledgerDataProviderService;
  @Value("${cardano.rosetta.GENESIS_SHELLEY_PATH}")
  private String genesisPath;


  @Override
  public Block findBlock(Long index, String hash) {

    log.info("[block] Looking for block: hash={}, index={}", hash, index);
    Block block = ledgerDataProviderService.findBlock(index, hash);
    if (nonNull(block)) {
      log.info("[block] Block was found, hash={}", block.getHash());

      List<Transaction> transactionsFound = findTransactionsByBlock(block);
      block.setTransactions(transactionsFound);
      block.setPoolDeposit(getPoolDeposit());

      log.debug("[block] full data {}", block);
      return block;
    }
    log.error("[block] Block was not found");
    throw ExceptionFactory.blockNotFoundException();
  }

  private String getPoolDeposit() {
    String content;
    try {
      content = FileUtils.fileReader(genesisPath);
    } catch (IOException e) {
      throw new ApiException("Could not read genesis file path", e);
    }
    JSONObject object = new JSONObject(content);
    JSONObject protocolParams = object.getJSONObject("protocolParams");
    // TODO Check if this is the right way to get poolDeposit
    String poolDeposit = String.valueOf(protocolParams.get("poolDeposit"));
    log.debug("[poolDeposit] poolDeposit is [{}]", poolDeposit);
    return poolDeposit;
  }

  private List<Transaction> findTransactionsByBlock(Block block) {
    boolean blockMightContainTransactions =
        block.getTransactionsCount() != 0 || block.getPreviousBlockHash().equals(block.getHash());
    log.debug("[findTransactionsByBlock] Does requested block contains transactions? : {}"
        , blockMightContainTransactions);
    if (blockMightContainTransactions) {
      return ledgerDataProviderService.findTransactionsByBlock(block.getNumber(), block.getHash());
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public BlockTransactionResponse getBlockTransaction(BlockTransactionRequest request) {
    BlockIdentifier blockIdentifier = request.getBlockIdentifier();
    BlockTransactionResponse response = new BlockTransactionResponse();
    List<Transaction> transactionsByBlock = ledgerDataProviderService.findTransactionsByBlock(
        blockIdentifier.getIndex(), blockIdentifier.getHash());
    if (transactionsByBlock != null) {
      Optional<Transaction> first = transactionsByBlock
          .stream()
          .filter(tr -> tr.getHash()
              .equals(request.getTransactionIdentifier().getHash()))
          .findFirst();
      if (first.isPresent()) {
        String poolDeposit = getPoolDeposit();
        response.setTransaction(
            //TODO saa: add refactor mapper to use the new model
            BlockToBlockResponse.mapToRosettaTransaction(first.get(), poolDeposit)
        );
      }
    }
    return response;
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
