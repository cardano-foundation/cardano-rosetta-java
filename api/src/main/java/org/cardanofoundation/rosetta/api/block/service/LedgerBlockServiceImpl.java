package org.cardanofoundation.rosetta.api.block.service;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import org.apache.commons.lang3.ObjectUtils;
import org.modelmapper.ModelMapper;

import org.cardanofoundation.rosetta.api.account.model.entity.AddressUtxoEntity;
import org.cardanofoundation.rosetta.api.account.model.repository.AddressUtxoRepository;
import org.cardanofoundation.rosetta.api.block.mapper.BlockToEntity;
import org.cardanofoundation.rosetta.api.block.mapper.BlockTxToEntity;
import org.cardanofoundation.rosetta.api.block.mapper.WithdrawalEntityToWithdrawal;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.domain.Delegation;
import org.cardanofoundation.rosetta.api.block.model.domain.GenesisBlock;
import org.cardanofoundation.rosetta.api.block.model.domain.PoolRegistration;
import org.cardanofoundation.rosetta.api.block.model.domain.PoolRetirement;
import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
import org.cardanofoundation.rosetta.api.block.model.domain.StakeRegistration;
import org.cardanofoundation.rosetta.api.block.model.entity.BlockEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.block.model.repository.BlockRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.DelegationRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.PoolRegistrationRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.PoolRetirementRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.StakeRegistrationRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.TxRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.WithdrawalRepository;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;

import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.common.services.ProtocolParamService;


@Slf4j
@Component
@RequiredArgsConstructor
public class LedgerBlockServiceImpl implements LedgerBlockService {

  private final ProtocolParamService protocolParamService;

  private final BlockRepository blockRepository;
  private final TxRepository txRepository;
  private final StakeRegistrationRepository stakeRegistrationRepository;
  private final DelegationRepository delegationRepository;
  private final PoolRegistrationRepository poolRegistrationRepository;
  private final PoolRetirementRepository poolRetirementRepository;
  private final WithdrawalRepository withdrawalRepository;
  private final AddressUtxoRepository addressUtxoRepository;


  private final ModelMapper mapper;
  private final BlockToEntity mapperBlock;
  private final BlockTxToEntity mapperTran;
  private final WithdrawalEntityToWithdrawal withdrawalEntityToWithdrawal;



  @Override
  public Block findBlock(Long blockNumber, String blockHash) {
    log.debug(
        "[findBlock] Parameters received for run query blockNumber: {} , blockHash: {}",
        blockNumber, blockHash);
    List<BlockEntity> blocks;
    if (blockHash == null && blockNumber != null) {
      blocks = blockRepository.findByNumber(blockNumber);
    } else if (blockHash != null && blockNumber == null) {
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
    List<TxnEntity> txList = txRepository.findTransactionsByBlockHash(
        byNumberAndHash.getFirst().getHash());
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
  public Block findLatestBlock() {
    log.info("[getLatestBlock] About to look for latest block");
    Long latestBlockNumber = blockRepository.findLatestBlockNumber();
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
  public GenesisBlock findGenesisBlock() {
    log.debug("[findGenesisBlock] About to run findGenesisBlock query");
    List<BlockEntity> blocks = blockRepository.findGenesisBlock();
    if (!blocks.isEmpty()) {
      BlockEntity genesis = blocks.getFirst();
      return GenesisBlock.builder().hash(genesis.getHash())
          .number(genesis.getNumber())
          .build();
    }
    log.error("[findGenesisBlock] Genesis block was not found");
    throw ExceptionFactory.genesisBlockNotFound();
  }



  private void populateTransactions(List<BlockTx> transactions) {
    for (BlockTx transaction : transactions) {
      populateUtxos(transaction.getInputs());
      populateUtxos(transaction.getOutputs());

      transaction.setStakeRegistrations(
          stakeRegistrationRepository
              .findByTxHash(transaction.getHash())
              .stream()
              .map(m -> mapper.map(m, StakeRegistration.class))
              .collect(Collectors.toList()));

      transaction.setDelegations(
          delegationRepository
              .findByTxHash(transaction.getHash())
              .stream()
              .map(m -> mapper.map(m, Delegation.class))
              .collect(Collectors.toList()));

      transaction.setPoolRegistrations(poolRegistrationRepository
          .findByTxHash(transaction.getHash())
          .stream()
          .map(PoolRegistration::fromEntity)
          .toList()); // TODO Refacotring - do this via JPA

      transaction.setPoolRetirements(poolRetirementRepository
          .findByTxHash(transaction.getHash())
          .stream()
          .map(m -> mapper.map(m, PoolRetirement.class))
          .collect(Collectors.toList()));

      transaction.setWithdrawals(withdrawalRepository
          .findByTxHash(transaction.getHash())
          .stream()
          .map(withdrawalEntityToWithdrawal::fromEntity)
          .toList());

      ProtocolParams protocolParametersFromIndexerAndConfig = protocolParamService.findProtocolParametersFromIndexerAndConfig();
      transaction.setSize((Long.parseLong(transaction.getFee()) - protocolParametersFromIndexerAndConfig.getMinFeeB().longValue()) / protocolParametersFromIndexerAndConfig.getMinFeeA().longValue());
    }
  }

  private void populateUtxos(List<Utxo> inputs) {
    for (Utxo utxo : inputs) {
      AddressUtxoEntity first = addressUtxoRepository.findAddressUtxoEntitiesByOutputIndexAndTxHash(
          utxo.getOutputIndex(), utxo.getTxHash()).getFirst();
      if (first != null) {
        // Populating the values from entity to model
        mapper.map(first, utxo);
      }
    }
  }

}
