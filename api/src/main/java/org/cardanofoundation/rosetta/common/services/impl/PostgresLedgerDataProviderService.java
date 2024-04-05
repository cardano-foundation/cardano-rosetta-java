package org.cardanofoundation.rosetta.common.services.impl;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.apache.commons.lang3.ObjectUtils;
import org.openapitools.client.model.Currency;

import org.cardanofoundation.rosetta.api.account.model.domain.AddressBalance;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.account.model.entity.AddressBalanceEntity;
import org.cardanofoundation.rosetta.api.account.model.entity.AddressUtxoEntity;
import org.cardanofoundation.rosetta.api.account.model.entity.Amt;
import org.cardanofoundation.rosetta.api.account.model.repository.AddressBalanceRepository;
import org.cardanofoundation.rosetta.api.account.model.repository.AddressUtxoRepository;
import org.cardanofoundation.rosetta.api.block.mapper.BlockToEntity;
import org.cardanofoundation.rosetta.api.block.mapper.BlockTxToEntity;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.Delegation;
import org.cardanofoundation.rosetta.api.block.model.domain.GenesisBlock;
import org.cardanofoundation.rosetta.api.block.model.domain.PoolRegistration;
import org.cardanofoundation.rosetta.api.block.model.domain.PoolRetirement;
import org.cardanofoundation.rosetta.api.block.model.domain.StakeAddressBalance;
import org.cardanofoundation.rosetta.api.block.model.domain.StakeRegistration;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.ProtocolParams;
import org.cardanofoundation.rosetta.api.block.model.entity.StakeAddressBalanceEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.UtxoKey;
import org.cardanofoundation.rosetta.api.block.model.repository.BlockRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.DelegationRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.EpochParamRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.PoolRegistrationRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.PoolRetirementRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.StakeAddressRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.StakeRegistrationRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.TxRepository;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
import org.cardanofoundation.rosetta.common.services.CardanoConfigService;
import org.cardanofoundation.rosetta.common.services.LedgerDataProviderService;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostgresLedgerDataProviderService implements LedgerDataProviderService {

  private final BlockRepository blockRepository;
  private final AddressBalanceRepository addressBalanceRepository;
  private final AddressUtxoRepository addressUtxoRepository;
  private final TxRepository txRepository;
  private final StakeRegistrationRepository stakeRegistrationRepository;
  private final DelegationRepository delegationRepository;
  private final PoolRegistrationRepository poolRegistrationRepository;
  private final PoolRetirementRepository poolRetirementRepository;
  private final StakeAddressRepository stakeAddressRepository;
  private final EpochParamRepository epochParamRepository;

  private final CardanoConfigService cardanoConfigService;

  private final BlockToEntity mapperBlock;
  private final BlockTxToEntity mapperTran;

  @Override
  public GenesisBlock findGenesisBlock() {
    log.debug("[findGenesisBlock] About to run findGenesisBlock query");
    List<BlockEntity> blocks = blockRepository.findGenesisBlock();
    if(!blocks.isEmpty()) {
      BlockEntity genesis = blocks.getFirst();
      return GenesisBlock.builder().hash(genesis.getHash())
          .number(genesis.getNumber())
          .build();
    }
    log.debug("[findGenesisBlock] Genesis block was not found");
    return null;
  }

  @Override
  public Block findBlock(Long blockNumber, String blockHash) {
    log.debug(
        "[findBlock] Parameters received for run query blockNumber: {} , blockHash: {}",
        blockNumber, blockHash);
    List<BlockEntity> blocks;
    if(blockHash == null && blockNumber != null) {
      blocks = blockRepository.findByNumber(blockNumber);
    } else if(blockHash != null && blockNumber == null){
      blocks = blockRepository.findByHash(blockHash);
    } else {
      blocks = blockRepository.findByNumberAndHash(blockNumber, blockHash);
    }
    if (!blocks.isEmpty()) {
      log.debug("[findBlock] Block found!");
      BlockEntity block = blocks.getFirst();
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
      // Populating transactions
      Block model = mapperBlock.fromEntity(block);
      populateTransactions(model.getTransactions());
      return model;
    }
    log.debug("[findBlock] No block was found");
    return null;
  }

  private void populateTransactions(List<BlockTx> transactions) {
    for (BlockTx transaction : transactions) {
      populateUtxos(transaction.getInputs());
      populateUtxos(transaction.getOutputs());
      transaction.setStakeRegistrations(
              stakeRegistrationRepository.findByTxHash(transaction.getHash())
                      .stream().map(StakeRegistration::fromEntity).toList()); // TODO Refacotring - do this via JPA
      transaction.setDelegations(delegationRepository.findByTxHash(transaction.getHash())
              .stream().map(Delegation::fromEntity).toList()); // TODO Refacotring - do this via JPA
      transaction.setPoolRegistrations(poolRegistrationRepository.findByTxHash(transaction.getHash())
              .stream().map(PoolRegistration::fromEntity).toList()); // TODO Refacotring - do this via JPA
      transaction.setPoolRetirements(poolRetirementRepository.findByTxHash(transaction.getHash())
              .stream().map(PoolRetirement::fromEntity).toList()); // TODO Refacotring - do this via JPA
    }
  }

  private void populateUtxos(List<Utxo> inputs) {
    for (Utxo utxo : inputs) {
      AddressUtxoEntity first = addressUtxoRepository.findAddressUtxoEntitiesByOutputIndexAndTxHash(utxo.getOutputIndex(), utxo.getTxHash()).getFirst();
      if(first != null) {
        utxo.populateFromUtxoEntity(first);
      }
    }
  }

  @Override
  public List<AddressBalance> findBalanceByAddressAndBlock(String address, Long number) {
    List<AddressBalanceEntity> balances = addressBalanceRepository.findAddressBalanceByAddressAndBlockNumber(address, number);
    return balances.stream().map(AddressBalance::fromEntity).toList();
  }

  @Override
  public List<StakeAddressBalance> findStakeAddressBalanceByAddressAndBlock(String address, Long number) {
    List<StakeAddressBalanceEntity> balances = stakeAddressRepository.findStakeAddressBalanceByAddressAndBlockNumber(address, number);
    return balances.stream().map(StakeAddressBalance::fromEntity).toList();
  }

  @Override
  public Long findLatestBlockNumber() {
        return blockRepository.findLatestBlockNumber();
  }

  @Override
  public List<Utxo> findUtxoByAddressAndCurrency(String address, List<Currency> currencies) {
    List<AddressUtxoEntity> addressUtxoEntities = addressUtxoRepository.findUtxosByAddress(address);
    List<Utxo> utxos = new ArrayList<>();
    for(AddressUtxoEntity entity : addressUtxoEntities) {
      List<Amt> amountsToAdd = new ArrayList<>();
      for(Amt amt : entity.getAmounts()) {
        boolean addToList = currencies.isEmpty() || currencies.stream().anyMatch(currency -> currency.getSymbol().equals(amt.getUnit()));
        if(addToList) {
          amountsToAdd.add(amt);
        }
      }
      Utxo utxoModel = Utxo.fromUtxoKey(UtxoKey.builder().outputIndex(entity.getOutputIndex()).txHash(entity.getTxHash()).build());
      utxoModel.setAmounts(amountsToAdd);
      utxos.add(utxoModel);
    }
    return utxos;
  }

  @Override
  public Block findLatestBlock() {
    log.info("[getLatestBlock] About to look for latest block");
    Long latestBlockNumber = findLatestBlockNumber();
    log.info("[getLatestBlock] Latest block number is {}", latestBlockNumber);
    Block latestBlock = findBlock(latestBlockNumber, null);
    if (Objects.isNull(latestBlock)) {
      log.error("[getLatestBlock] Latest block not found");
      throw ExceptionFactory.blockNotFoundException();
    }
    log.debug("[getLatestBlock] Returning latest block {}", latestBlock);
    return latestBlock;
  }

  @Override
  public List<BlockTx> findTransactionsByBlock(Long blockNumber, String blockHash) {
    log.debug(
        "[findTransactionsByBlock] Parameters received for run query blockNumber: {} blockHash: {}",
        blockNumber, blockHash);

    List<BlockEntity> byNumberAndHash = blockRepository.findByNumberAndHash(blockNumber, blockHash);
    if (byNumberAndHash.isEmpty()) {
      log.debug(
          "[findTransactionsByBlock] No block found for blockNumber: {} blockHash: {}",
          blockNumber, blockHash);
      return Collections.emptyList();
    }
    List<TxnEntity> txList = txRepository.findTransactionsByBlockHash(byNumberAndHash.getFirst().getHash());
    log.debug(
        "[findTransactionsByBlock] Found {} transactions", txList.size());
    if (ObjectUtils.isNotEmpty(txList)) {
      List<BlockTx> transactions = txList.stream().map(mapperTran::fromEntity).toList();
      populateTransactions(transactions);
      return transactions;
    }
    return Collections.emptyList();
  }

  @Override
  public ProtocolParams findProtocolParametersFromIndexer() {
    return epochParamRepository.findLatestProtocolParams();
  }

  @Override
  public ProtocolParams findProtocolParametersFromConfig() {
    try {
      return cardanoConfigService.getProtocolParameters();
    } catch (FileNotFoundException e) {
      log.error("[findProtocolParametersFromConfig] Protocol parameters not found");
      return null;
    }
  }

  @Override
  public ProtocolParams findProtocolParametersFromIndexerAndConfig() {
    ProtocolParams protocolParams = findProtocolParametersFromIndexer();
    protocolParams.merge(findProtocolParametersFromConfig());
    return protocolParams;
  }
}
