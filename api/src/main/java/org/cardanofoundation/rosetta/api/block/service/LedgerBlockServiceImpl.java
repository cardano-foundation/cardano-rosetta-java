package org.cardanofoundation.rosetta.api.block.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang3.ObjectUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.cardanofoundation.rosetta.api.account.mapper.AddressUtxoEntityToUtxo;
import org.cardanofoundation.rosetta.api.account.model.domain.Utxo;
import org.cardanofoundation.rosetta.api.account.model.entity.AddressUtxoEntity;
import org.cardanofoundation.rosetta.api.account.model.repository.AddressUtxoRepository;
import org.cardanofoundation.rosetta.api.block.mapper.BlockIdentifierProjectionToBlockIdentifierExtended;
import org.cardanofoundation.rosetta.api.block.mapper.BlockToEntity;
import org.cardanofoundation.rosetta.api.block.mapper.BlockTxToEntity;
import org.cardanofoundation.rosetta.api.block.mapper.DelegationEntityToDelegation;
import org.cardanofoundation.rosetta.api.block.mapper.PoolRegistrationEntityToPoolRegistration;
import org.cardanofoundation.rosetta.api.block.mapper.PoolRetirementEntityToPoolRetirement;
import org.cardanofoundation.rosetta.api.block.mapper.StakeRegistrationEntityToStakeRegistration;
import org.cardanofoundation.rosetta.api.block.mapper.WithdrawalEntityToWithdrawal;
import org.cardanofoundation.rosetta.api.block.model.domain.Block;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockIdentifierExtended;
import org.cardanofoundation.rosetta.api.block.model.domain.BlockTx;
import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;
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


  private final BlockToEntity mapperBlock;
  private final BlockIdentifierProjectionToBlockIdentifierExtended blockToGensisBlock;
  private final StakeRegistrationEntityToStakeRegistration stakeRegistrationEntityToStakeRegistration;
  private final DelegationEntityToDelegation delegationEntityToDelegation;
  private final PoolRegistrationEntityToPoolRegistration poolRegistrationEntityToPoolRegistration;
  private final PoolRetirementEntityToPoolRetirement poolRetirementEntityToPoolRetirement;
  private final AddressUtxoEntityToUtxo addressUtxoEntityToUtxo;
  private final BlockTxToEntity mapperTran;
  private final WithdrawalEntityToWithdrawal withdrawalEntityToWithdrawal;

  private BlockIdentifierExtended cachedGenesisBlock;

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
    Entities fetched = findByTxHashWith(transactions);
    transactions.forEach(tx -> populateTransaction(tx, pps, fetched));
    return model;
  }

  private Entities findByTxHashWith(List<BlockTx> transactions) {
    return findByTxHash(transactions); //1
    //return findByTxHashPreview(transactions); //2
    //return findByTxHashMono(transactions); //3
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
      Entities fetched = findByTxHashWith(transactions);
      transactions.forEach(tx -> populateTransaction(tx, pps, fetched));
      return transactions;
    }
    return Collections.emptyList();
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
        .map(blockToGensisBlock::toBlockIdentifierExtended)
        .orElseThrow(ExceptionFactory::genesisBlockNotFound);
    log.debug("Returning latest findLatestBlockIdentifier {}", latestBlock);
    return latestBlock;
  }

  @Override
  public BlockIdentifierExtended findGenesisBlockIdentifier() {
    if (cachedGenesisBlock == null) {
      log.debug("About to run findGenesisBlock query");
      cachedGenesisBlock = blockRepository.findGenesisBlockIdentifier()
          .map(blockToGensisBlock::toBlockIdentifierExtended)
          .orElseThrow(ExceptionFactory::genesisBlockNotFound);
    }
    log.debug("Returning genesis block {}", cachedGenesisBlock);
    return cachedGenesisBlock;
  }

  //use green threads from java 21 - instead findByTxHashPreview
  private Entities findByTxHash(List<BlockTx> transactions) {
    List<String> txHashes = transactions.stream().map(BlockTx::getHash).toList();

    List<String> utxHashes = transactions
        .stream()
        .flatMap(t -> Stream.concat(t.getInputs().stream(), t.getOutputs().stream()))
        .map(Utxo::getTxHash)
        .toList();

    // refactor to use invokeAll
    try (var executorService = Executors.newVirtualThreadPerTaskExecutor()) {
      List<Callable<Object>> tasks = new ArrayList<>();

      tasks.add(() -> addressUtxoRepository.findByTxHashIn(utxHashes));
      tasks.add(() -> stakeRegistrationRepository.findByTxHashIn(txHashes));
      tasks.add(() -> delegationRepository.findByTxHashIn(txHashes));
      tasks.add(() -> poolRegistrationRepository.findByTxHashIn(txHashes));
      tasks.add(() -> poolRetirementRepository.findByTxHashIn(txHashes));
      tasks.add(() -> withdrawalRepository.findByTxHashIn(txHashes));

      List<Future<Object>> futures = executorService.invokeAll(tasks);
      return new Entities(
          (List<AddressUtxoEntity>) futures.get(0).get(),
          (List<StakeRegistrationEntity>) futures.get(1).get(),
          (List<DelegationEntity>) futures.get(2).get(),
          (List<PoolRegistrationEntity>) futures.get(3).get(),
          (List<PoolRetirementEntity>) futures.get(4).get(),
          (List<WithdrawalEntity>) futures.get(5).get());

    } catch (InterruptedException | ExecutionException e) {
      log.error("Error fetching transaction data", e);
      throw ExceptionFactory.unspecifiedError("Error fetching transaction data");
    }
  }

  private Entities findByTxHashMono(List<BlockTx> transactions) {
    List<String> txHashes = transactions.stream().map(BlockTx::getHash).toList();

    List<String> utxHashes = transactions
        .stream()
        .flatMap(t -> Stream.concat(t.getInputs().stream(), t.getOutputs().stream()))
        .map(Utxo::getTxHash)
        .toList();

    Mono<List<AddressUtxoEntity>> utxo = Mono.just(addressUtxoRepository.findByTxHashIn(utxHashes));
    Mono<List<StakeRegistrationEntity>> sReg = Mono.just(stakeRegistrationRepository.findByTxHashIn(txHashes));
    Mono<List<DelegationEntity>> delegations = Mono.just(delegationRepository.findByTxHashIn(txHashes));
    Mono<List<PoolRegistrationEntity>> pReg = Mono.just(poolRegistrationRepository.findByTxHashIn(txHashes));
    Mono<List<PoolRetirementEntity>> pRet = Mono.just(poolRetirementRepository.findByTxHashIn(txHashes));
    Mono<List<WithdrawalEntity>> withdrawals = Mono.just(withdrawalRepository.findByTxHashIn(txHashes));

    AtomicReference<Entities> ent  = new AtomicReference<>();
    Flux.zip(utxo, sReg, delegations, pReg, pRet, withdrawals)
        .parallel()
        .runOn(Schedulers.parallel())
        .subscribe(tuple -> {

          List<AddressUtxoEntity> utxoResult = tuple.getT1();
          List<StakeRegistrationEntity> sRegResult = tuple.getT2();
          List<DelegationEntity> delegationsResult = tuple.getT3();
          List<PoolRegistrationEntity> pRegResult = tuple.getT4();
          List<PoolRetirementEntity> pRetResult = tuple.getT5();
          List<WithdrawalEntity> withdrawalsResult = tuple.getT6();

          ent.set(new Entities(utxoResult, sRegResult, delegationsResult, pRegResult, pRetResult, withdrawalsResult));

          // handle the results
        }).dispose();

    return ent.get();

  }



/*
  @SuppressWarnings("preview")
  private Entities findByTxHashPreview(List<BlockTx> transactions) {
    List<String> txHashes = transactions.stream().map(BlockTx::getHash).toList();

    List<String> utxHashes = transactions
        .stream()
        .flatMap(t -> Stream.concat(t.getInputs().stream(), t.getOutputs().stream()))
        .map(Utxo::getTxHash)
        .toList();

    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

      Subtask<List<AddressUtxoEntity>> utxos = scope.fork(() -> addressUtxoRepository.findByTxHashIn(utxHashes));
      Subtask<List<StakeRegistrationEntity>> sReg = scope.fork(() -> stakeRegistrationRepository.findByTxHashIn(txHashes));
      Subtask<List<DelegationEntity>> delegations = scope.fork(() -> delegationRepository.findByTxHashIn(txHashes));
      Subtask<List<PoolRegistrationEntity>> pReg = scope.fork(() -> poolRegistrationRepository.findByTxHashIn(txHashes));
      Subtask<List<PoolRetirementEntity>> pRet = scope.fork(() -> poolRetirementRepository.findByTxHashIn(txHashes));
      Subtask<List<WithdrawalEntity>> withdrawals = scope.fork(() -> withdrawalRepository.findByTxHashIn(txHashes));

      scope.join();
      scope.throwIfFailed();

      return new Entities(utxos.get(), sReg.get(), delegations.get(), pReg.get(), pRet.get(), withdrawals.get());

    } catch (InterruptedException | ExecutionException e) {
      log.error("Error fetching transaction data", e);
      throw ExceptionFactory.unspecifiedError("Error fetching transaction data");
    }
  }

 */

  private record Entities(List<AddressUtxoEntity> utxos,
                          List<StakeRegistrationEntity> stakeRegistrations,
                          List<DelegationEntity> delegations,
                          List<PoolRegistrationEntity> poolRegistrations,
                          List<PoolRetirementEntity> poolRetirements,
                          List<WithdrawalEntity> withdrawals) {

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
            .map(stakeRegistrationEntityToStakeRegistration::toDto)
            .toList());

    transaction.setDelegations(
        fetched.delegations
            .stream()
            .filter(tx -> tx.getTxHash().equals(transaction.getHash()))
            .map(delegationEntityToDelegation::toDto)
            .toList());

    transaction.setPoolRegistrations(
        fetched.poolRegistrations
            .stream()
            .map(poolRegistrationEntityToPoolRegistration::toDto)
            .toList());

    transaction.setPoolRetirements(
        fetched.poolRetirements
            .stream()
            .map(poolRetirementEntityToPoolRetirement::toDto)
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
        .ifPresent(m -> addressUtxoEntityToUtxo.overWriteDto(utxo,m));
  }
}
