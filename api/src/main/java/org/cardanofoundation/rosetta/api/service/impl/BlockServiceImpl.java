package org.cardanofoundation.rosetta.api.service.impl;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.api.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.api.mapper.DataMapper;
import org.cardanofoundation.rosetta.api.model.rest.AccountBalanceResponse;
import org.cardanofoundation.rosetta.api.model.rest.BalanceAtBlock;
import org.cardanofoundation.rosetta.api.model.rest.Currency;
import org.cardanofoundation.rosetta.api.model.rest.MaBalance;
import org.cardanofoundation.rosetta.api.model.rest.Utxo;
import org.cardanofoundation.rosetta.api.projection.dto.BlockDto;
import org.cardanofoundation.rosetta.api.projection.dto.BlockUtxos;
import org.cardanofoundation.rosetta.api.projection.dto.BlockUtxosMultiAssets;
import org.cardanofoundation.rosetta.api.projection.dto.GenesisBlockDto;
import org.cardanofoundation.rosetta.api.service.BlockService;
import org.cardanofoundation.rosetta.api.service.CardanoService;
import org.cardanofoundation.rosetta.api.service.LedgerDataProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {

  private final Integer PAGE_SIZE = 5;

  @Autowired
  LedgerDataProviderService ledgerDataProviderService;
  @Autowired
  @Qualifier("CardanoService1")
  private CardanoService cardanoService;


  @Override
  public AccountBalanceResponse findBalanceDataByAddressAndBlock(String address, Long number,
      String hash) {
    BlockDto blockDto = this.findBlock(number, hash);
    if (Objects.isNull(blockDto)) {
      log.error("[findBalanceDataByAddressAndBlock] Block not found");
      throw ExceptionFactory.blockNotFoundException();
    }
    log.info(
        "[findBalanceDataByAddressAndBlock] Looking for utxos for address" + address + " and block "
            + blockDto.getHash());
    if (cardanoService.isStakeAddress(address)) {
      log.debug("[findBalanceDataByAddressAndBlock] Address is StakeAddress");
      log.debug("[findBalanceDataByAddressAndBlock] About to get balance for " + address);
      Double balance = ledgerDataProviderService.findBalanceByAddressAndBlock(address,
          blockDto.getHash());
      log.debug(
          "[findBalanceDataByAddressAndBlock] Found stake balance of " + balance + " for address "
              + address);
      BalanceAtBlock balanceAtBlock = BalanceAtBlock.builder()
          .block(blockDto)
          .balance(balance.toString())
          .build();
      return DataMapper.mapToAccountBalanceResponse(balanceAtBlock);

    } else {
      log.debug("[findBalanceDataByAddressAndBlock] Address isn't StakeAddress");

      List<Utxo> utxoDetails = ledgerDataProviderService.findUtxoByAddressAndBlock(address,
          blockDto.getHash(),
          null);
      List<MaBalance> maBalances = ledgerDataProviderService.findMaBalanceByAddressAndBlock(address,
          blockDto.getHash());
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
        "[findCoinsDataByAddress] Looking for utxos for address " + address + " and "
            + currencies.size() + "specified currencies"
    );
    List<Utxo> utxoDetails = ledgerDataProviderService.findUtxoByAddressAndBlock(address,
        block.getHash(), currencies);
    log.debug("[findCoinsByAddress] Found " + utxoDetails.size() + " coin details for address "
        + address);

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


}
