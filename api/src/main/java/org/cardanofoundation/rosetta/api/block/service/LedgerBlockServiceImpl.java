package org.cardanofoundation.rosetta.api.block.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang3.ObjectUtils;

import org.cardanofoundation.rosetta.api.account.mapper.AddressUtxoEntityToUtxo;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.account.model.entity.AddressUtxoEntity;
import org.cardanofoundation.rosetta.api.account.model.repository.AddressUtxoRepository;
import org.cardanofoundation.rosetta.api.block.mapper.BlockMapper;
import org.cardanofoundation.rosetta.api.block.mapper.TransactionMapper;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockIdentifierExtended;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.entity.*;
import org.cardanofoundation.rosetta.api.block.model.repository.*;
import org.cardanofoundation.rosetta.common.exception.ExceptionFactory;

import static java.util.concurrent.TimeUnit.MINUTES;

@Slf4j
@RequiredArgsConstructor
@Component
@Transactional(readOnly = true)
public class LedgerBlockServiceImpl implements LedgerBlockService {

  private final BlockRepository blockRepository;
  private final TxRepository txRepository;
  private final StakeRegistrationRepository stakeRegistrationRepository;
  private final DelegationRepository delegationRepository;
  private final PoolRegistrationRepository poolRegistrationRepository;
  private final PoolRetirementRepository poolRetirementRepository;
  private final WithdrawalRepository withdrawalRepository;
  private final AddressUtxoRepository addressUtxoRepository;
  private final InvalidTransactionRepository invalidTransactionRepository;

  private final BlockMapper blockMapper;
  private final TransactionMapper transactionMapper;
  private final AddressUtxoEntityToUtxo addressUtxoEntityToUtxo;

  private BlockIdentifierExtended cachedGenesisBlock;

  @Override
  public Optional<Block> findBlock(Long blockNumber, String blockHash) {
    log.debug("Query blockNumber: {} , blockHash: {}", blockNumber, blockHash);
    if (blockHash != null && blockNumber == null) {
      return blockRepository.findByHash(blockHash).map(this::toModelFrom);
    } else if (blockHash == null && blockNumber != null) {
      return blockRepository.findByNumber(blockNumber).map(this::toModelFrom);
    } else if (blockHash != null && blockNumber != null) {
      return blockRepository
          .findByNumberAndHash(blockNumber, blockHash)
          .map(this::toModelFrom);
    } else {
      return blockRepository.findLatestBlock().map(this::toModelFrom);
    }
  }

  @Override
  public Optional<BlockIdentifierExtended> findBlockIdentifier(Long blockNumber, String blockHash) {
    log.debug("Query blockNumber: {} , blockHash: {}", blockNumber, blockHash);
    if (blockHash == null && blockNumber != null) {
      return blockRepository.findBlockIdentifierByNumber(blockNumber)
          .map(blockMapper::mapToBlockIdentifierExtended);
    } else if (blockHash != null && blockNumber == null) {
      return blockRepository.findBlockIdentifierByHash(blockHash)
          .map(blockMapper::mapToBlockIdentifierExtended);
    } else {
      return blockRepository
          .findBlockIdentifierByNumberAndHash(blockNumber, blockHash)
          .map(blockMapper::mapToBlockIdentifierExtended);
    }
  }

  @NotNull
  private Block toModelFrom(BlockEntity blockEntity) {
    Block model = blockMapper.mapToBlock(blockEntity);
    List<BlockTx> transactions = model.getTransactions();
    TransactionInfo fetched = findByTxHash(transactions);
    Map<UtxoKey, AddressUtxoEntity> utxoMap = getUtxoMapFromEntities(fetched);
    transactions.forEach(tx -> populateTransaction(tx, fetched, utxoMap));

    return model;
  }

  @Override
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
      return mapTxnEntitiesToBlockTxList(txList);
    }

    return Collections.emptyList();
  }

  @Override
  public List<BlockTx> mapTxnEntitiesToBlockTxList(List<TxnEntity> txList) {
    List<BlockTx> transactions = txList.stream().map(blockMapper::mapToBlockTx)
            .toList();

    TransactionInfo fetched = findByTxHash(transactions);
    Map<UtxoKey, AddressUtxoEntity> utxoMap = getUtxoMapFromEntities(fetched);

    transactions.forEach(tx -> populateTransaction(tx, fetched, utxoMap));

    return transactions;
  }

  @Override
  public Block findLatestBlock() {
    log.debug("About to look for latest block");
    BlockEntity latestBlock = blockRepository.findLatestBlock()
        .orElseThrow(ExceptionFactory::genesisBlockNotFound);
    log.debug("Returning latest block {}", latestBlock);
    return toModelFrom(latestBlock);
  }

  @Override
  public BlockIdentifierExtended findLatestBlockIdentifier() {
    log.debug("About to look for latest findLatestBlockIdentifier");
    BlockIdentifierExtended latestBlock = blockRepository.findLatestBlockIdentifier()
        .map(blockMapper::mapToBlockIdentifierExtended)
        .orElseThrow(ExceptionFactory::genesisBlockNotFound);
    log.debug("Returning latest findLatestBlockIdentifier {}", latestBlock);
    return latestBlock;
  }

  @Override
  public BlockIdentifierExtended findGenesisBlockIdentifier() {
    if (cachedGenesisBlock == null) {
      log.debug("About to run findGenesisBlock query");
      cachedGenesisBlock = blockRepository.findGenesisBlockIdentifier()
          .map(blockMapper::mapToBlockIdentifierExtended)
          .orElseThrow(ExceptionFactory::genesisBlockNotFound);
    }
    log.debug("Returning genesis block {}", cachedGenesisBlock);
    return cachedGenesisBlock;
  }

  private TransactionInfo findByTxHash(List<BlockTx> transactions) {
    List<String> txHashes = transactions.stream().map(BlockTx::getHash).toList();
    List<String> utxHashes = transactions.stream()
            .flatMap(t -> Stream.concat(t.getInputs().stream(), t.getOutputs().stream()))
            .map(Utxo::getTxHash)
            .toList();

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      var utxos = executor.submit(() -> addressUtxoRepository.findByTxHashIn(utxHashes));
      var sReg = executor.submit(() -> stakeRegistrationRepository.findByTxHashIn(txHashes));
      var delegations = executor.submit(() -> delegationRepository.findByTxHashIn(txHashes));
      var pReg = executor.submit(() -> poolRegistrationRepository.findByTxHashIn(txHashes));
      var pRet = executor.submit(() -> poolRetirementRepository.findByTxHashIn(txHashes));
      var withdrawals = executor.submit(() -> withdrawalRepository.findByTxHashIn(txHashes));

      return new TransactionInfo(utxos.get(10, MINUTES), sReg.get(10, MINUTES), delegations.get(10, MINUTES), pReg.get(10, MINUTES), pRet.get(10, MINUTES), withdrawals.get(10, MINUTES));
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      log.error("Error fetching transaction data", e);

      Thread.currentThread().interrupt();

      throw ExceptionFactory.unspecifiedError("Error fetching transaction data");
    }
  }

  void populateTransaction(BlockTx transaction,
                           TransactionInfo fetched,
                           Map<UtxoKey, AddressUtxoEntity> utxoMap) {
    boolean invalid = invalidTransactionRepository.findById(transaction.getHash()).isPresent();
    transaction.setInvalid(invalid);

    Optional.ofNullable(transaction.getInputs())
        .stream()
        .flatMap(List::stream)
        .forEach(utxo -> populateUtxo(utxo, utxoMap));

      Optional.ofNullable(transaction.getOutputs())
          .stream()
          .flatMap(List::stream)
          .forEach(utxo -> populateUtxo(utxo, utxoMap));

    transaction.setStakeRegistrations(
        fetched.stakeRegistrations
            .stream()
            .filter(tx -> tx.getTxHash().equals(transaction.getHash()))
            .map(transactionMapper::mapStakeRegistrationEntityToStakeRegistration)
            .toList());

    transaction.setStakePoolDelegations(
        fetched.delegations
            .stream()
            .filter(tx -> tx.getTxHash().equals(transaction.getHash()))
            .map(transactionMapper::mapDelegationEntityToDelegation)
            .toList());

    transaction.setWithdrawals(
        fetched.withdrawals
            .stream()
            .filter(tx -> tx.getTxHash().equals(transaction.getHash()))
            .map(transactionMapper::mapWithdrawalEntityToWithdrawal)
            .toList());

    transaction.setPoolRegistrations(
        fetched.poolRegistrations
            .stream()
            .filter(tx -> tx.getTxHash().equals(transaction.getHash()))
            .map(transactionMapper::mapEntityToPoolRegistration)
            .toList());

    transaction.setPoolRetirements(
        fetched.poolRetirements
            .stream()
            .filter(tx -> tx.getTxHash().equals(transaction.getHash()))
            .map(transactionMapper::mapEntityToPoolRetirement)
            .toList());
  }

  private void populateUtxo(Utxo utxo, Map<UtxoKey, AddressUtxoEntity> utxoMap) {
    AddressUtxoEntity entity = utxoMap.get(
        new UtxoKey(utxo.getTxHash(), utxo.getOutputIndex()));

    Optional.ofNullable(entity)
        .ifPresent(e -> addressUtxoEntityToUtxo.overWriteDto(utxo, e));
  }

  private static Map<UtxoKey, AddressUtxoEntity> getUtxoMapFromEntities(TransactionInfo fetched) {
    return fetched.utxos
        .stream()
        .collect(Collectors.toMap(
            utxo -> new UtxoKey(utxo.getTxHash(), utxo.getOutputIndex()),
            utxo -> utxo
        ));
  }

  record TransactionInfo(List<AddressUtxoEntity> utxos,
                                 List<StakeRegistrationEntity> stakeRegistrations,
                                 List<DelegationEntity> delegations,
                                 List<PoolRegistrationEntity> poolRegistrations,
                                 List<PoolRetirementEntity> poolRetirements,
                                 List<WithdrawalEntity> withdrawals) {

  }

  record UtxoKey(String txHash, Integer outputIndex) {

  }

}
