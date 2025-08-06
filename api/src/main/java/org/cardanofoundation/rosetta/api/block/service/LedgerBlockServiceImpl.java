package org.cardanofoundation.rosetta.api.block.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.ShutdownOnFailure;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jakarta.validation.constraints.NotNull;
import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

@Slf4j
@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Setter
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
  private final Clock clock;

  private BlockIdentifierExtended cachedGenesisBlock;

  @Value("${cardano.rosetta.BLOCK_TRANSACTION_API_TIMEOUT_SECS:5}")
  private int blockTransactionApiTimeoutSecs;

  @Value("${cardano.rosetta.REMOVE_SPENT_UTXOS:false}")
  private boolean isRemovalOfSpentUTxOsEnabled;

  @Value("${cardano.rosetta.REMOVE_SPENT_UTXOS_LAST_BLOCKS_GRACE_COUNT:2160}")
  private int removeSpentUTxOsLastBlocksGraceCount;

  @PostConstruct
  public void init() {
    log.info("LedgerBlockServiceImpl initialized with " +
                    "blockFetchTimeoutInSeconds: {}" +
                    ", isRemovalOfSpentUTxOsEnabled: {}" +
                    ", removeSpentUTxOsLastBlocksGraceCount: {}",

            blockTransactionApiTimeoutSecs, isRemovalOfSpentUTxOsEnabled, removeSpentUTxOsLastBlocksGraceCount);
  }

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

      return List.of();
    }

    List<TxnEntity> txList = txRepository.findTransactionsByBlockHash(blkEntity.get().getHash());
    log.debug("Found {} transactions", txList.size());

    return mapTxnEntitiesToBlockTxList(txList);
  }

  @Override
  public List<BlockTx> mapTxnEntitiesToBlockTxList(List<TxnEntity> txList) {
    List<BlockTx> transactions = txList.stream().map(blockMapper::mapToBlockTx).toList();

    TransactionInfo fetched = findByTxHash(transactions);
    Map<UtxoKey, AddressUtxoEntity> utxoMap = getUtxoMapFromEntities(fetched);

    transactions.forEach(tx -> populateTransaction(tx, fetched, utxoMap));

    return transactions;
  }

  @Override
  public Page<BlockTx> mapTxnEntitiesToBlockTxList(Page<TxnEntity> txList) {
    Page<BlockTx> transactions = txList.map(blockMapper::mapToBlockTx);

    TransactionInfo fetched = findByTxHash(transactions.getContent());
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

  public BlockIdentifierExtended findOldestBlockIdentifier(BlockIdentifierExtended latestBlock) {
    log.debug("About to run findOldestBlock query");

    if (!isRemovalOfSpentUTxOsEnabled) {
      throw ExceptionFactory.oldestBlockNotFound();
    }

    long targetBlockNo = latestBlock.getNumber() - removeSpentUTxOsLastBlocksGraceCount; // this could result in less than 0

    if (targetBlockNo < 0) {
        log.debug("Oldest block number is less than 0, returning genesis block");

        return cachedGenesisBlock;
    }

    BlockIdentifierExtended oldestBlock = blockRepository.findBlockProjectionByNumber(targetBlockNo)
    .map(blockMapper::mapToBlockIdentifierExtended)
    .orElseThrow(ExceptionFactory::oldestBlockNotFound);

    log.debug("Returning oldest block {}", oldestBlock);

    return oldestBlock;
  }

  private TransactionInfo findByTxHash(List<BlockTx> transactions) {
    List<String> txHashes = transactions.stream().map(BlockTx::getHash).toList();
    List<String> utxHashes = transactions.stream()
            .flatMap(t -> Stream.concat(t.getInputs().stream(), t.getOutputs().stream()))
            .map(Utxo::getTxHash)
            .toList();

    try (ShutdownOnFailure scope = new ShutdownOnFailure()) {
      StructuredTaskScope.Subtask<List<AddressUtxoEntity>> utxos = scope.fork(() -> addressUtxoRepository.findByTxHashIn(utxHashes));
      StructuredTaskScope.Subtask<List<StakeRegistrationEntity>> sReg = scope.fork(() -> stakeRegistrationRepository.findByTxHashIn(txHashes));
      StructuredTaskScope.Subtask<List<DelegationEntity>> delegations = scope.fork(() -> delegationRepository.findByTxHashIn(txHashes));
      StructuredTaskScope.Subtask<List<PoolRegistrationEntity>> pReg = scope.fork(() -> poolRegistrationRepository.findByTxHashIn(txHashes));
      StructuredTaskScope.Subtask<List<PoolRetirementEntity>> pRet = scope.fork(() -> poolRetirementRepository.findByTxHashIn(txHashes));
      StructuredTaskScope.Subtask<List<WithdrawalEntity>> withdrawals = scope.fork(() -> withdrawalRepository.findByTxHashIn(txHashes));
      StructuredTaskScope.Subtask<List<InvalidTransactionEntity>> invalidTxs = scope.fork(() -> invalidTransactionRepository.findByTxHashIn(txHashes));

      scope.joinUntil(Instant.now(clock).plusSeconds(blockTransactionApiTimeoutSecs));
      scope.throwIfFailed(); // Propagate any failure

      return new TransactionInfo(
              utxos.get(),
              sReg.get(),
              delegations.get(),
              pReg.get(),
              pRet.get(),
              withdrawals.get(),
              invalidTxs.get()
      );
    } catch (ExecutionException e) {
      log.error("Error fetching transaction data", e);

      throw ExceptionFactory.unspecifiedError("Error fetching transaction data, msg:%s".formatted(e.getMessage()));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Error fetching transaction data", e);

      throw ExceptionFactory.unspecifiedError("Error fetching transaction data, msg:%s".formatted(e.getMessage()));
    } catch (TimeoutException e) {
      log.error("Error fetching transaction data", e);

      throw ExceptionFactory.timeOut("timeout while fetching transaction data from db.");
    }
  }

  void populateTransaction(BlockTx transaction,
                           TransactionInfo fetched,
                           Map<UtxoKey, AddressUtxoEntity> utxoMap) {
    boolean invalid = fetched.invalidTransactions
            .stream()
            .anyMatch(invalidTx -> invalidTx.getTxHash().equals(transaction.getHash()));
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
    // TODO dRep Vote Delegations
    //transaction.setDRepDelegations(fetched.delegations

    // TODO governance votes
    //transaction.setGovernanceVotes(fetched.);

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
                         List<WithdrawalEntity> withdrawals,
                         List<InvalidTransactionEntity> invalidTransactions) {

  }

  record UtxoKey(String txHash, Integer outputIndex) {
  }

}
