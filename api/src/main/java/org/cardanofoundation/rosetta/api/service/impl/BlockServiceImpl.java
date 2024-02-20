package org.cardanofoundation.rosetta.api.service.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.github.fge.jsonschema.core.tree.JsonTree;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.api.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.api.mapper.DataMapper;
import org.cardanofoundation.rosetta.api.model.TransactionIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.*;
import org.cardanofoundation.rosetta.api.model.dto.BlockDto;
import org.cardanofoundation.rosetta.api.model.dto.BlockUtxos;
import org.cardanofoundation.rosetta.api.service.BlockService;
import org.cardanofoundation.rosetta.api.service.LedgerDataProviderService;
import org.cardanofoundation.rosetta.api.util.CardanoAddressUtils;
import org.cardanofoundation.rosetta.api.util.FileUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {

  private final LedgerDataProviderService ledgerDataProviderService;
//  private final CardanoService cardanoService;
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
      String content = null;
      try {
        content = FileUtils.fileReader(genesisPath);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      JSONObject object = new JSONObject(content);
      JSONObject protocolParams = object.getJSONObject("protocolParams");
      String poolDeposit = String.valueOf(protocolParams.get("poolDeposit")); // TODO Check if this is the right way to get poolDeposit
      log.debug("[poolDeposit] poolDeposit is " + poolDeposit);
      if (transactionsFound.size() > pageSize) {
        log.info(
                "[block] Returning only transactions hashes since the number of them is bigger than {}"
                , pageSize);
        return BlockResponse.builder()
                .block(DataMapper.mapToRosettaBlock(block, poolDeposit))
                .otherTransactions(
                        transactionsFound.stream()
                          .map(
                            transactionDto ->
                              new TransactionIdentifier()
                              .hash(transactionDto.getHash()))
                          .toList())
                .build();
      }
      log.info("[block] Looking for blocks transactions full data");
//      List<PopulatedTransaction> transactions = this.fillTransactions(transactionsFound);
//      log.info("[block] transactions already filled {}", transactions);
      return BlockResponse.builder()
              .block(DataMapper.mapToRosettaBlock(block, poolDeposit))
              .build();
    }
    log.error("[block] Block was not found");
    throw ExceptionFactory.blockNotFoundException();
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
    List<TransactionDto> transactionsByBlock = ledgerDataProviderService.findTransactionsByBlock(blockIdentifier.getIndex(), blockIdentifier.getHash());
    Optional<TransactionDto> first = transactionsByBlock.stream().filter(transactionDto -> transactionDto.getHash().equals(blockTransactionRequest.getTransactionIdentifier().getHash())).findFirst();
    BlockTransactionResponse response = new BlockTransactionResponse();
    if(first.isPresent())
      response.setTransaction(DataMapper.mapToRosettaTransaction(first.get()));

    return response;
  }


//  @Override
//  public AccountBalanceResponse findBalanceDataByAddressAndBlock(String address, Long number,
//                                                                 String hash) {
////    return null;
//    BlockDto blockDto = ledgerDataProviderService.findBlock(number, hash);
//    if (Objects.isNull(blockDto)) {
//      log.error("[findBalanceDataByAddressAndBlock] Block not found");
//      throw ExceptionFactory.blockNotFoundException();
//    }
//    log.info(
//            "[findBalanceDataByAddressAndBlock] Looking for utxos for address {} and block {}",
//            address,
//            blockDto.getHash());
//    if (CardanoAddressUtils.isStakeAddress(address)) {
//      log.debug("[findBalanceDataByAddressAndBlock] Address is StakeAddress");
//      log.debug("[findBalanceDataByAddressAndBlock] About to get balance for {}", address);
//      Long balance = ledgerDataProviderService.findBalanceByAddressAndBlock(address, blockDto.getHash());
//      log.debug(
//              "[findBalanceDataByAddressAndBlock] Found stake balance of {} for address {}", balance,
//              address);
//      BalanceAtBlock balanceAtBlock = BalanceAtBlock.builder()
//              .block(blockDto)
//              .balance(balance.toString())
//              .build();
//      return DataMapper.mapToAccountBalanceResponse(balanceAtBlock);
//
//    }


//    else {
//      log.debug("[findBalanceDataByAddressAndBlock] Address isn't StakeAddress");
//
//      List<Utxo> utxoDetails = ledgerDataProviderService
//              .findUtxoByAddressAndBlock(address, blockDto.getHash(), null);
//      List<MaBalance> maBalances = ledgerDataProviderService
//              .findMaBalanceByAddressAndBlock(address, blockDto.getHash());
//      BlockUtxosMultiAssets blockUtxosMultiAssets = BlockUtxosMultiAssets.builder()
//              .maBalances(maBalances)
//              .utxos(utxoDetails)
//              .block(blockDto)
//              .build();
//      return DataMapper.mapToAccountBalanceResponse(blockUtxosMultiAssets);
//    }
  }

}
