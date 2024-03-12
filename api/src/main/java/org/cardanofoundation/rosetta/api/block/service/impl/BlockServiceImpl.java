package org.cardanofoundation.rosetta.api.block.service.impl;

import java.io.IOException;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.account.model.dto.AddressBalanceDTO;
import org.cardanofoundation.rosetta.api.block.model.dto.BlockDto;
import org.cardanofoundation.rosetta.api.block.model.dto.GenesisBlockDto;
import org.cardanofoundation.rosetta.api.block.model.dto.StakeAddressBalanceDTO;
import org.cardanofoundation.rosetta.api.block.model.dto.TransactionDto;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.mapper.DataMapper;
import org.cardanofoundation.rosetta.common.util.CardanoAddressUtil;
import org.cardanofoundation.rosetta.api.block.service.BlockService;
import org.cardanofoundation.rosetta.common.services.LedgerDataProviderService;
import org.cardanofoundation.rosetta.common.util.FileUtils;
import org.json.JSONObject;
import org.openapitools.client.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {

  private final LedgerDataProviderService ledgerDataProviderService;
  @Value("${page-size:5}")
  private Integer pageSize;
  @Value("${cardano.rosetta.GENESIS_SHELLEY_PATH}")
  private String genesisPath;

  @Override
  public BlockResponse getBlockByBlockRequest(BlockRequest blockRequest) {
    PartialBlockIdentifier blockIdentifier = blockRequest.getBlockIdentifier();
    String hash = blockIdentifier.getHash();
    Long index = blockIdentifier.getIndex();
    log.info("[block] Looking for block: hash={}, index={}", hash, index);
    BlockDto block = ledgerDataProviderService.findBlock(index, hash);
    if (Objects.nonNull(block)) {
      log.info("[block] Block was found");
      List<TransactionDto> transactionsFound = this.findTransactionsByBlock(block);

      log.debug("[block] transactionsFound is " + transactionsFound.toString());
      String poolDeposit = getPoolDeposit();
      if (transactionsFound.size() > pageSize) {
        log.info(
                "[block] Returning only transactions hashes since the number of them is bigger than {}"
                , pageSize);
        return BlockResponse.builder()
                .block(DataMapper.mapToRosettaBlock(block, poolDeposit))
//                .otherTransactions(transactionsFound.stream().map(transactionDto -> new TransactionIdentifier().hash(transactionDto.getHash())).toList())
                .build();
      }
      log.info("[block] Looking for blocks transactions full data");


      return BlockResponse.builder()
              .block(DataMapper.mapToRosettaBlock(block, poolDeposit))
              .build();
    }
    log.error("[block] Block was not found");
    throw ExceptionFactory.blockNotFoundException();
  }

  private String getPoolDeposit() {
    String content;
    try {
      content = FileUtils.fileReader(genesisPath);
    } catch (IOException e) {
      throw ExceptionFactory.unspecifiedError("Error reading genesis file");
    }
    JSONObject object = new JSONObject(content);
    JSONObject protocolParams = object.getJSONObject("protocolParams");
    String poolDeposit = String.valueOf(protocolParams.get("poolDeposit")); // TODO Check if this is the right way to get poolDeposit
    log.debug("[poolDeposit] poolDeposit is " + poolDeposit);
    return poolDeposit;
  }

  @Override
  public List<TransactionDto> findTransactionsByBlock(BlockDto block) {
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
  public BlockTransactionResponse getBlockTransaction(BlockTransactionRequest blockTransactionRequest) {
    BlockIdentifier blockIdentifier = blockTransactionRequest.getBlockIdentifier();
    BlockTransactionResponse response = new BlockTransactionResponse();
    List<TransactionDto> transactionsByBlock = ledgerDataProviderService.findTransactionsByBlock(blockIdentifier.getIndex(), blockIdentifier.getHash());
    if(transactionsByBlock != null) {
      Optional<TransactionDto> first = transactionsByBlock.stream().filter(transactionDto -> transactionDto.getHash().equals(blockTransactionRequest.getTransactionIdentifier().getHash())).findFirst();
      if(first.isPresent()) {
        String poolDeposit = getPoolDeposit();
        response.setTransaction(DataMapper.mapToRosettaTransaction(first.get(), poolDeposit));
      }
    }
    return response;
  }

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

    log.info("[findBlock] Do we have to look for latestBlock? {}", searchLatestBlock);
    Long blockNumber = searchLatestBlock ? ledgerDataProviderService.findLatestBlockNumber() : number;
    log.info("[findBlock] Looking for block with blockNumber {}", blockNumber);
    BlockDto response = ledgerDataProviderService.findBlock(blockNumber, hash);
    if (Objects.nonNull(response)) {
      log.info("[findBlock] Block was found");
    }
    log.debug("[findBlock] Returning response: " + response);
    return response;
  }

  @Override
  public AccountBalanceResponse findBalanceDataByAddressAndBlock(String address, Long number, String hash) {
    BlockDto blockDto;
    if(number != null || hash != null) {
      blockDto = ledgerDataProviderService.findBlock(number, hash);
    } else {
      blockDto = ledgerDataProviderService.findLatestBlock();
    }

    if (Objects.isNull(blockDto)) {
      log.error("[findBalanceDataByAddressAndBlock] Block not found");
      throw ExceptionFactory.blockNotFoundException();
    }
    log.info(
            "[findBalanceDataByAddressAndBlock] Looking for utxos for address {} and block {}",
            address,
            blockDto.getHash());
    if (CardanoAddressUtil.isStakeAddress(address)) {
      log.debug("[findBalanceDataByAddressAndBlock] Address is StakeAddress");
      log.debug("[findBalanceDataByAddressAndBlock] About to get balance for {}", address);
      List<StakeAddressBalanceDTO> balances = ledgerDataProviderService.findStakeAddressBalanceByAddressAndBlock(address, blockDto.getNumber());
      if(Objects.isNull(balances) || balances.isEmpty()) {
        log.error("[findBalanceDataByAddressAndBlock] No balance found for {}", address);
        throw ExceptionFactory.invalidAddressError();
      }
    return DataMapper.mapToStakeAddressBalanceResponse(blockDto, balances.getFirst());
    } else {
      log.debug("[findBalanceDataByAddressAndBlock] Address isn't StakeAddress");

      List<AddressBalanceDTO> balances = ledgerDataProviderService.findBalanceByAddressAndBlock(address, blockDto.getNumber());
      return DataMapper.mapToAccountBalanceResponse(blockDto, balances);
    }
  }

}
