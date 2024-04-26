package org.cardanofoundation.rosetta.api.block.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;

import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
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
import org.cardanofoundation.rosetta.api.block.model.entity.PoolRegistrationEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.block.model.repository.BlockRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.DelegationRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.PoolRegistrationRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.PoolRetirementRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.StakeRegistrationRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.TxRepository;
import org.cardanofoundation.rosetta.api.block.model.repository.WithdrawalRepository;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;
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
    log.debug("Query blockNumber: {} , blockHash: {}", blockNumber, blockHash);
    Optional<BlockEntity> blockEntity;
    if (blockHash == null && blockNumber != null) {
      blockEntity = blockRepository.findByNumber(blockNumber);
    } else if (blockHash != null && blockNumber == null) {
      blockEntity = blockRepository.findByHash(blockHash);
    } else {
      blockEntity = blockRepository.findByNumberAndHash(blockNumber, blockHash);
    }
    if (blockEntity.isPresent()) {
      log.debug("Block found! {}", blockEntity);
      return toModelFrom(blockEntity.get());
    }
    log.debug("[findBlock] No block was found");
    return null; //TODO saa: replace with optional
  }

  @NotNull
  private Block toModelFrom(BlockEntity blockEntity) {
    Block model = mapperBlock.fromEntity(blockEntity);
    model.getTransactions().forEach(this::populateTransaction);
    return model;
  }

  @Override
  public List<BlockTx> findTransactionsByBlock(Long blk, String blkHash) {
    log.debug("[findTransactionsByBlock]  query blockNumber: {} blockHash: {}", blk, blkHash);
    Optional<BlockEntity> blkEntity = blockRepository.findByNumberAndHash(blk, blkHash);
    if (blkEntity.isEmpty()) {
      log.debug("[findTransactionsByBlock] Block Not found: {} blockHash: {}", blk, blkHash);
      return Collections.emptyList();
    }
    List<TxnEntity> txList = txRepository.findTransactionsByBlockHash(blkEntity.get().getHash());
    log.debug("[findTransactionsByBlock] Found {} transactions", txList.size());
    if (ObjectUtils.isNotEmpty(txList)) {
      List<BlockTx> transactions = txList.stream().map(mapperTran::fromEntity).toList();
      transactions.forEach(this::populateTransaction);
      return transactions;
    }
    return Collections.emptyList();
  }


  @Override
  public Block findLatestBlock() {
    log.info("[getLatestBlock] About to look for latest block");
    BlockEntity latestBlock = blockRepository.findLatestBlock();
    log.debug("[getLatestBlock] Returning latest block {}", latestBlock);
    return toModelFrom(latestBlock);
  }

  @Override
  public GenesisBlock findGenesisBlock() {
    log.debug("[findGenesisBlock] About to run findGenesisBlock query");
    return blockRepository.findGenesisBlock()
        .map(b -> mapper.map(b, GenesisBlock.class))
        .orElseThrow(ExceptionFactory::genesisBlockNotFound);
  }


  private void populateTransaction(BlockTx transaction) {

    Optional.ofNullable(transaction.getInputs())
        .stream()
        .flatMap(List::stream)
        .forEach(this::populateUtxo);

    Optional.ofNullable(transaction.getOutputs())
        .stream()
        .flatMap(List::stream)
        .forEach(this::populateUtxo);

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
        .map(poolReg -> mapper.typeMap(PoolRegistrationEntity.class, PoolRegistration.class)
            .addMappings(mp ->
                mp.map(PoolRegistrationEntity::getPoolOwners, PoolRegistration::setOwners))
            .map(poolReg))
        .collect(Collectors.toList()));

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

    ProtocolParams pps = protocolParamService.findProtocolParametersFromIndexerAndConfig();
    transaction.setSize(calcSize(transaction, pps));
  }

  private static long calcSize(BlockTx tx, ProtocolParams p) {
    return (Long.parseLong(tx.getFee()) - p.getMinFeeB().longValue()) / p.getMinFeeA().longValue();
  }

  private void populateUtxo(Utxo utxo) {
    AddressUtxoEntity first = addressUtxoRepository
        .findAddressUtxoEntitiesByOutputIndexAndTxHash(utxo.getOutputIndex(), utxo.getTxHash())
        .getFirst();
    Optional.ofNullable(first).ifPresent(m -> mapper.map(m, utxo));
  }
}
