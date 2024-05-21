package org.cardanofoundation.rosetta.api.block.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang3.ObjectUtils;
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
import org.cardanofoundation.rosetta.api.block.model.entity.DelegationEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.PoolRegistrationEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.PoolRetirementEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.StakeRegistrationEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.TxnEntity;
import org.cardanofoundation.rosetta.api.block.model.entity.WithdrawalEntity;
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
  public Optional<Block> findBlock(Long blockNumber, String blockHash) {
    log.debug("Query blockNumber: {} , blockHash: {}", blockNumber, blockHash);
    if (blockHash == null && blockNumber != null) {
      return blockRepository.findByNumber(blockNumber).map(this::toModelFrom);
    } else if (blockHash != null && blockNumber == null) {
      return blockRepository.findByHash(blockHash).map(this::toModelFrom);
    } else {
      return blockRepository
          .findByNumberAndHash(blockNumber, blockHash)
          .map(this::toModelFrom);
    }
  }

  @NotNull
  private Block toModelFrom(BlockEntity blockEntity) {
    Block model = mapperBlock.fromEntity(blockEntity);
    ProtocolParams pps = protocolParamService.findProtocolParametersFromIndexer();
    List<BlockTx> transactions = model.getTransactions();
    Entities fetched = getEntities(transactions);
    transactions.forEach(tx -> populateTransaction(tx, pps, fetched));
    return model;
  }

  private Entities getEntities(List<BlockTx> transactions) {
    List<String> txHashes = transactions.stream().map(BlockTx::getHash).toList();
    List<AddressUtxoEntity> byTxHashIn = addressUtxoRepository.findByTxHashIn(txHashes);
    List<StakeRegistrationEntity> stakeRegistrations = stakeRegistrationRepository.findByTxHashIn(
        txHashes);
    List<DelegationEntity> delegations = delegationRepository.findByTxHashIn(txHashes);
    List<PoolRegistrationEntity> poolRegistrations = poolRegistrationRepository.findByTxHashIn(
        txHashes);
    List<PoolRetirementEntity> poolRetirements = poolRetirementRepository.findByTxHashIn(txHashes);
    List<WithdrawalEntity> withdrawals = withdrawalRepository.findByTxHashIn(txHashes);
    return new Entities(byTxHashIn, stakeRegistrations, delegations, poolRegistrations,
        poolRetirements,
        withdrawals);
  }

  private record Entities(List<AddressUtxoEntity> utxos,
                          List<StakeRegistrationEntity> stakeRegistrations,
                          List<DelegationEntity> delegations,
                          List<PoolRegistrationEntity> poolRegistrations,
                          List<PoolRetirementEntity> poolRetirements,
                          List<WithdrawalEntity> withdrawals) {

  }

  @Override
  @Transactional(readOnly = true)
  public List<BlockTx> findTransactionsByBlock(Long blk, String blkHash) {
    log.debug("query blockNumber: {} blockHash: {}", blk, blkHash);
    Optional<BlockEntity> blkEntity = blockRepository.findByNumberAndHash(blk, blkHash);
    if (blkEntity.isEmpty()) {
      log.debug("Block Not found: {} blockHash: {}", blk, blkHash);
      return Collections.emptyList();
    }
    List<TxnEntity> txList = txRepository.findTransactionsByBlockHash(blkEntity.get().getHash());
    log.debug("Found {} transactions", txList.size());
    if (ObjectUtils.isNotEmpty(txList)) {
      ProtocolParams pps = protocolParamService.findProtocolParametersFromIndexer();
      List<BlockTx> transactions = txList.stream().map(mapperTran::fromEntity).toList();
      Entities fetched = getEntities(transactions);
      transactions.forEach(tx -> populateTransaction(tx, pps, fetched));
      return transactions;
    }
    return Collections.emptyList();
  }

  @Override
  public Block findLatestBlock() {
    log.debug("About to look for latest block");
    BlockEntity latestBlock = blockRepository.findLatestBlock();
    log.debug("Returning latest block {}", latestBlock);
    return toModelFrom(latestBlock);
  }

  @Override
  public GenesisBlock findGenesisBlock() {
    log.debug("About to run findGenesisBlock query");
    return blockRepository.findGenesisBlock()
        .map(b -> mapper.map(b, GenesisBlock.class))
        .orElseThrow(ExceptionFactory::genesisBlockNotFound);
  }

  private void populateTransaction(BlockTx transaction, ProtocolParams pps, Entities fetched) {

    Optional.ofNullable(transaction.getInputs())
        .stream()
        .flatMap(List::stream)
        .forEach(t -> populateUtxo(t, fetched.utxos));

    Optional.ofNullable(transaction.getOutputs())
        .stream()
        .flatMap(List::stream)
        .forEach(utxo -> populateUtxo(utxo, fetched.utxos));

    transaction.setStakeRegistrations(
        fetched.stakeRegistrations
            .stream()
            .filter(tx -> tx.getTxHash().equals(transaction.getHash()))
            .map(m -> mapper.map(m, StakeRegistration.class))
            .toList());

    transaction.setDelegations(
        fetched.delegations
            .stream()
            .filter(tx -> tx.getTxHash().equals(transaction.getHash()))
            .map(m -> mapper.map(m, Delegation.class))
            .toList());

    transaction.setPoolRegistrations(
        fetched.poolRegistrations
            .stream()
            .filter(tx -> tx.getTxHash().equals(transaction.getHash()))
            .map(poolReg -> mapper.typeMap(PoolRegistrationEntity.class, PoolRegistration.class)
                .addMappings(mp ->
                    mp.map(PoolRegistrationEntity::getPoolOwners, PoolRegistration::setOwners))
                .map(poolReg))
            .toList());

    transaction.setPoolRetirements(
        fetched.poolRetirements
            .stream()
            .filter(tx -> tx.getTxHash().equals(transaction.getHash()))
            .map(m -> mapper.map(m, PoolRetirement.class))
            .toList());

    transaction.setWithdrawals(
        fetched.withdrawals
            .stream()
            .filter(tx -> tx.getTxHash().equals(transaction.getHash()))
            .map(withdrawalEntityToWithdrawal::fromEntity)
            .toList());

    transaction.setSize(calcSize(transaction, pps));
  }

  private static long calcSize(BlockTx tx, ProtocolParams p) {
    return (Long.parseLong(tx.getFee()) - p.getMinFeeB().longValue()) / p.getMinFeeA().longValue();
  }

  private void populateUtxo(Utxo utxo, List<AddressUtxoEntity> utxos) {
    utxos
        .stream()
        .filter(t ->
            Objects.equals(t.getOutputIndex(), utxo.getOutputIndex())
                && t.getTxHash().equals(utxo.getTxHash()))
        .findAny()
        .ifPresent(m -> mapper.map(m, utxo));
  }
}
