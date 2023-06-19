package org.cardanofoundation.rosetta.api.service.impl;

import static org.cardanofoundation.rosetta.api.mapper.DataMapper.mapToRosettaBlock;
import static org.cardanofoundation.rosetta.api.mapper.DataMapper.mapToRosettaTransaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.cardanofoundation.rosetta.api.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.api.mapper.DataMapper;
import org.cardanofoundation.rosetta.api.model.rest.AccountBalanceResponse;
import org.cardanofoundation.rosetta.api.model.rest.BalanceAtBlock;
import org.cardanofoundation.rosetta.api.model.rest.BlockRequest;
import org.cardanofoundation.rosetta.api.model.rest.BlockResponse;
import org.cardanofoundation.rosetta.api.model.rest.BlockTransactionRequest;
import org.cardanofoundation.rosetta.api.model.rest.BlockTransactionResponse;
import org.cardanofoundation.rosetta.api.model.rest.Currency;
import org.cardanofoundation.rosetta.api.model.rest.MaBalance;
import org.cardanofoundation.rosetta.api.model.rest.PartialBlockIdentifier;
import org.cardanofoundation.rosetta.api.model.rest.TransactionDto;
import org.cardanofoundation.rosetta.api.model.rest.Utxo;
import org.cardanofoundation.rosetta.api.projection.dto.BlockDto;
import org.cardanofoundation.rosetta.api.projection.dto.BlockUtxos;
import org.cardanofoundation.rosetta.api.projection.dto.BlockUtxosMultiAssets;
import org.cardanofoundation.rosetta.api.projection.dto.GenesisBlockDto;
import org.cardanofoundation.rosetta.api.projection.dto.PopulatedTransaction;
import org.cardanofoundation.rosetta.api.service.BlockService;
import org.cardanofoundation.rosetta.api.service.CardanoService;
import org.cardanofoundation.rosetta.api.service.LedgerDataProviderService;
import org.openapitools.client.model.Transaction;
import org.openapitools.client.model.TransactionIdentifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {

  private final LedgerDataProviderService ledgerDataProviderService;
  private final CardanoService cardanoService;
  @Value("${page-size:5}")
  private Integer pageSize;

  @Override
  public AccountBalanceResponse findBalanceDataByAddressAndBlock(String address, Long number,
      String hash) {
    BlockDto blockDto = this.findBlock(number, hash);
    if (Objects.isNull(blockDto)) {
      log.error("[findBalanceDataByAddressAndBlock] Block not found");
      throw ExceptionFactory.blockNotFoundException();
    }
    log.info(
        "[findBalanceDataByAddressAndBlock] Looking for utxos for address {} and block {}",
        address,
        blockDto.getHash());
    if (cardanoService.isStakeAddress(address)) {
      log.debug("[findBalanceDataByAddressAndBlock] Address is StakeAddress");
      log.debug("[findBalanceDataByAddressAndBlock] About to get balance for {}", address);
      Long balance = ledgerDataProviderService
          .findBalanceByAddressAndBlock(address, blockDto.getHash());
      log.debug(
          "[findBalanceDataByAddressAndBlock] Found stake balance of {} for address {}", balance,
          address);
      BalanceAtBlock balanceAtBlock = BalanceAtBlock.builder()
          .block(blockDto)
          .balance(balance.toString())
          .build();
      return DataMapper.mapToAccountBalanceResponse(balanceAtBlock);

    } else {
      log.debug("[findBalanceDataByAddressAndBlock] Address isn't StakeAddress");

      List<Utxo> utxoDetails = ledgerDataProviderService
          .findUtxoByAddressAndBlock(address, blockDto.getHash(), null);
      List<MaBalance> maBalances = ledgerDataProviderService
          .findMaBalanceByAddressAndBlock(address, blockDto.getHash());
      BlockUtxosMultiAssets blockUtxosMultiAssets = BlockUtxosMultiAssets.builder()
          .maBalances(maBalances)
          .utxos(utxoDetails)
          .block(blockDto)
          .build();
      return DataMapper.mapToAccountBalanceResponse(blockUtxosMultiAssets);
    }
  }

  @Override
  public BlockUtxos findCoinsDataByAddress(String address,
      List<Currency> currencies) {
    BlockDto block = this.findBlock(null, null);
    if (Objects.isNull(block)) {
      log.error("[findCoinsDataByAddress] Block not found");
      throw ExceptionFactory.blockNotFoundException();
    }
    log.info(
        "[findCoinsDataByAddress] Looking for utxos for address {} and {} specified currencies",
        address,
        currencies.size()
    );
    List<Utxo> utxoDetails = ledgerDataProviderService.findUtxoByAddressAndBlock(address,
        block.getHash(), currencies);
    log.debug("[findCoinsByAddress] Found {} coin details for address {}", utxoDetails.size(),
        address);

    return BlockUtxos.builder()
        .block(block)
        .utxos(utxoDetails)
        .build();
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
    Long blockNumber =
        searchLatestBlock ? ledgerDataProviderService.findLatestBlockNumber() : number;
    log.info("[findBlock] Looking for block with blockNumber {}", blockNumber);
    BlockDto response = ledgerDataProviderService.findBlock(blockNumber, hash);
    if (Objects.nonNull(response)) {
      log.info("[findBlock] Block was found");
    }
    log.debug("[findBlock] Returning response: " + response);
    return response;
  }

  @Override
  public BlockResponse getBlockByBlockRequest(BlockRequest blockRequest) {
    PartialBlockIdentifier blockIdentifier = blockRequest.getBlockIdentifier();
    String hash = blockIdentifier.getHash();
    Long index = blockIdentifier.getIndex();
    log.info("[block] Looking for block: hash={}, index={}", hash, index);
    BlockDto block = this.findBlock(index, hash);
    if (Objects.nonNull(block)) {
      log.info("[block] Block was found");
      List<TransactionDto> transactionsFound = this.findTransactionsByBlock(block);
      log.debug("[block] transactionsFound is " + transactionsFound.toString());
      String poolDeposit = cardanoService.getProtocolParameters().getPoolDeposit();
      log.debug("[poolDeposit] poolDeposit is " + poolDeposit);
      if (transactionsFound.size() > pageSize) {
        log.info(
            "[block] Returning only transactions hashes since the number of them is bigger than {}"
            , pageSize);
        return BlockResponse.builder()
            .block(mapToRosettaBlock(block, new ArrayList<>(), poolDeposit))
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
      List<PopulatedTransaction> transactions = this.fillTransactions(transactionsFound);
      log.info("[block] transactions already filled {}", transactions);
      return BlockResponse.builder()
          .block(mapToRosettaBlock(block, transactions, poolDeposit))
          .build();
    }
    log.error("[block] Block was not found");
    throw ExceptionFactory.blockNotFoundException();
  }

  @Override
  public List<PopulatedTransaction> fillTransactions(List<TransactionDto> transactions) {
    if (ObjectUtils.isEmpty(transactions)) {
      return null;
    } else {
      return ledgerDataProviderService.fillTransaction(transactions);
    }
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
  public BlockTransactionResponse getBlockTransaction(
      BlockTransactionRequest blockTransactionRequest) {
    String transactionHash = blockTransactionRequest.getTransactionIdentifier().getHash();
    log.info("[blockTransaction] Looking for transaction for hash {} and block {}",
        transactionHash,
        blockTransactionRequest.getBlockIdentifier());
    PopulatedTransaction transaction = this.
        findTransaction(blockTransactionRequest.getTransactionIdentifier().getHash(),
            blockTransactionRequest.getBlockIdentifier().getIndex(),
            blockTransactionRequest.getBlockIdentifier().getHash());
    if (Objects.isNull(transaction)) {
      log.error("[blockTransaction] No transaction found");
      throw ExceptionFactory.transactionNotFound();
    }
    String poolDeposit = cardanoService.getProtocolParameters().getPoolDeposit();
    Transaction transactionResponse = mapToRosettaTransaction(transaction, poolDeposit);

    return BlockTransactionResponse.builder()
        .transaction(transactionResponse)
        .build();
  }

  public PopulatedTransaction findTransaction(String transactionHash, Long blockNumber,
      String blockHash) {
    return ledgerDataProviderService
        .findTransactionByHashAndBlock(transactionHash, blockNumber, blockHash);
  }


}
