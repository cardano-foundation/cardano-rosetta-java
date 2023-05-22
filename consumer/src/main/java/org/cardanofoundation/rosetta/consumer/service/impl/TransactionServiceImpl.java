package org.cardanofoundation.rosetta.consumer.service.impl;

import com.bloxbean.cardano.client.transaction.spec.RedeemerTag;
import com.google.common.collect.Lists;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTxIn;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTxOut;
import org.cardanofoundation.rosetta.common.entity.Block;
import org.cardanofoundation.rosetta.common.entity.ExtraKeyWitness;
import org.cardanofoundation.rosetta.common.entity.Redeemer;
import org.cardanofoundation.rosetta.common.entity.Script;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.entity.TxOut;
import org.cardanofoundation.rosetta.consumer.constant.ConsumerConstant;
import org.cardanofoundation.rosetta.consumer.dto.DatumDTO;
import org.cardanofoundation.rosetta.consumer.dto.RedeemerReference;
import org.cardanofoundation.rosetta.consumer.factory.CertificateSyncServiceFactory;
import org.cardanofoundation.rosetta.common.ledgersync.Witnesses;
import org.cardanofoundation.rosetta.common.ledgersync.certs.Certificate;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedDatumRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedExtraKeyWitnessRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedTxRepository;
import org.cardanofoundation.rosetta.consumer.service.BlockDataService;
import org.cardanofoundation.rosetta.consumer.service.DatumService;
import org.cardanofoundation.rosetta.consumer.service.EpochParamService;
import org.cardanofoundation.rosetta.consumer.service.MultiAssetService;
import org.cardanofoundation.rosetta.consumer.service.ParamProposalService;
import org.cardanofoundation.rosetta.consumer.service.RedeemerService;
import org.cardanofoundation.rosetta.consumer.service.ReferenceInputService;
import org.cardanofoundation.rosetta.consumer.service.ScriptService;
import org.cardanofoundation.rosetta.consumer.service.StakeAddressService;
import org.cardanofoundation.rosetta.consumer.service.TransactionService;
import org.cardanofoundation.rosetta.consumer.service.TxInService;
import org.cardanofoundation.rosetta.consumer.service.TxMetaDataService;
import org.cardanofoundation.rosetta.consumer.service.TxOutService;
import org.cardanofoundation.rosetta.consumer.service.WithdrawalsService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {

  CachedTxRepository cachedTxRepository;

  CachedDatumRepository cachedDatumRepository;
  CachedExtraKeyWitnessRepository cachedExtraKeyWitnessRepository;

  MultiAssetService multiAssetService;
  StakeAddressService stakeAddressService;
  ParamProposalService paramProposalService;
  EpochParamService epochParamService;
  WithdrawalsService withdrawalsService;
  TxMetaDataService txMetaDataService;
  RedeemerService redeemerService;
  ScriptService scriptService;
  DatumService datumService;

  BlockDataService blockDataService;
  TxOutService txOutService;
  TxInService txInService;

  ReferenceInputService referenceInputService;

  CertificateSyncServiceFactory certificateSyncServiceFactory;

  @Override
  public Map<String, Tx> prepareTxs(Block block, AggregatedBlock aggregatedBlock) {
    List<AggregatedTx> aggregatedTxList = aggregatedBlock.getTxList();

    if (CollectionUtils.isEmpty(aggregatedTxList)) {
      return Collections.emptyMap();
    }

    // Create a script map and datum map for bulk saving
    Map<String, Script> mScripts = new HashMap<>();
    DatumDTO datums = DatumDTO.builder()
        .datums(new HashMap<>())
        .build();

    /*
     * For each aggregated tx, map it to a new tx entity.
     * Because script and datum need to be mapped to their first appeared tx, it is easier
     * to handle them here as the overall processing time for both of them is fast
     *
     * Also check if the currently processing aggregated tx's validity and push it to
     * either a queue of success txs or failed txs
     */
    var txList = aggregatedTxList.stream().map(aggregatedTx -> {
      Tx tx = new Tx();
      tx.setHash(aggregatedTx.getHash());
      tx.setBlock(block);
      tx.setBlockIndex(aggregatedTx.getBlockIndex());
      tx.setOutSum(aggregatedTx.getOutSum());
      tx.setFee(aggregatedTx.getFee());
      tx.setValidContract(aggregatedTx.isValidContract());
      tx.setDeposit(aggregatedTx.getDeposit());

      Witnesses witnesses = aggregatedTx.getWitnesses();
      if (Objects.nonNull(witnesses)) {
        scriptService.getAllScript(witnesses, tx).forEach(mScripts::putIfAbsent);
        datums.setTransactionWitness(witnesses);
        datums.setTransactionBody(aggregatedTx);
        datums.setTx(tx);
        datumService.handleDatum(datums);
      }

      if (aggregatedTx.isValidContract()) {
        blockDataService.saveSuccessTx(aggregatedTx);
      } else {
        blockDataService.saveFailedTx(aggregatedTx);
      }
      return tx;
    }).collect(Collectors.toList());

    cachedTxRepository.saveAll(txList);
    scriptService.saveNonExistsScripts(mScripts.values());

    if (!CollectionUtils.isEmpty(datums.getDatums())) {
      cachedDatumRepository.saveAll(datums.getDatums().values());
    }

    return txList.stream().collect(Collectors.toConcurrentMap(Tx::getHash, Function.identity()));
  }

  @Override
  public void handleTxs(Map<String, Tx> txMap) {
    Collection<AggregatedTx> successTxs = blockDataService.getSuccessTxs();
    Collection<AggregatedTx> failedTxs = blockDataService.getFailedTxs();

    // Handle stake address and its first appeared tx
    stakeAddressService.handleStakeAddressesFromTxs(
        blockDataService.getStakeAddressTxHashMap(), txMap);

    // Handle extra key witnesses from required signers
    handleExtraKeyWitnesses(successTxs, failedTxs, txMap);

    // Handle Tx contents
    handleTxContents(successTxs, failedTxs, txMap);
  }

  private void handleTxContents(Collection<AggregatedTx> successTxs,
      Collection<AggregatedTx> failedTxs, Map<String, Tx> txMap) {
    if (CollectionUtils.isEmpty(successTxs) && CollectionUtils.isEmpty(failedTxs)) {
      return;
    }

    // MUST SET FIRST
    // multi asset mint
    long startTime = System.currentTimeMillis();
    multiAssetService.handleMultiAssetMint(successTxs, txMap);
    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    log.trace("Multi asset mint handling time: {} ms, {} seconds", totalTime, totalTime / 1000f);

    /*
     * Here success txs are split into batches. For each batch, all tx outs are processed and
     * all tx ins are gathered with their respective tx outs are selected from cache or db.
     * Every batch is processed asynchronously
     *
     * Since the amount of failed txs are not significant, there is no need to split them
     * into batches
     */
    var successTxBatches = Lists.partition(
        new ArrayList<>(successTxs), ConsumerConstant.TX_BATCH_SIZE);

    // tx out
    Collection<TxOut> txOutCollection = new ConcurrentLinkedQueue<>();
    successTxBatches.parallelStream().forEach(batch -> txOutCollection.addAll(
        txOutService.prepareTxOuts(buildAggregatedTxOutMap(batch), txMap)));
    startTime = endTime;
    endTime = System.currentTimeMillis();
    totalTime = endTime - startTime;
    log.trace("Prepare tx out handling time: {} ms, {} seconds", totalTime, totalTime / 1000f);

    // handle collateral out as tx out for failed txs
    if (!CollectionUtils.isEmpty(failedTxs)) {
      txOutService.prepareTxOuts(buildCollateralTxOutMap(failedTxs), txMap);
      startTime = endTime;
      endTime = System.currentTimeMillis();
      totalTime = endTime - startTime;
      log.trace("Collateral outs handling time: {} ms, {} seconds", totalTime, totalTime / 1000f);
    }

    // redeemer
    Map<RedeemerReference<?>, Redeemer> redeemersMap =
        redeemerService.handleRedeemers(successTxs, txMap, txOutCollection);
    startTime = endTime;
    endTime = System.currentTimeMillis();
    totalTime = endTime - startTime;
    log.trace("Redeemers handling time: {} ms, {} seconds", totalTime, totalTime / 1000f);

    // tx in
    successTxBatches.parallelStream().forEach(batch ->
        txInService.handleTxIns(batch, buildTxInsMap(batch), txMap, redeemersMap));
    startTime = endTime;
    endTime = System.currentTimeMillis();
    totalTime = endTime - startTime;
    log.trace("Tx ins handling time: {} ms, {} seconds", totalTime, totalTime / 1000f);

    // handle collateral input as tx in
    txInService.handleTxIns(failedTxs,
        buildCollateralTxInsMap(failedTxs), txMap, Collections.emptyMap());
    startTime = endTime;
    endTime = System.currentTimeMillis();
    totalTime = endTime - startTime;
    log.trace("Collateral input handling time: {} ms, {} seconds", totalTime, totalTime / 1000f);

    // auxiliary
    txMetaDataService.handleAuxiliaryDataMaps(txMap);
    startTime = endTime;
    endTime = System.currentTimeMillis();
    totalTime = endTime - startTime;
    log.trace("AuxData handling time: {} ms, {} seconds", totalTime, totalTime / 1000f);

    //param proposal
    paramProposalService.handleParamProposals(successTxs, txMap);
    startTime = endTime;
    endTime = System.currentTimeMillis();
    totalTime = endTime - startTime;
    log.trace("ParamProposal handling time: {} ms, {} seconds", totalTime, totalTime / 1000f);

    // reference inputs
    referenceInputService.handleReferenceInputs(buildReferenceTxInsMap(successTxs), txMap);
    startTime = endTime;
    endTime = System.currentTimeMillis();
    totalTime = endTime - startTime;
    log.trace("Reference inputs handling time: {} ms, {} seconds", totalTime, totalTime / 1000f);

    // certificates
    handleCertificates(successTxs, txMap, redeemersMap);
    startTime = endTime;
    endTime = System.currentTimeMillis();
    totalTime = endTime - startTime;
    log.trace("Certificates handling time: {} ms, {} seconds", totalTime, totalTime / 1000f);

    // Withdrawals
    withdrawalsService.handleWithdrawal(successTxs, txMap, redeemersMap);
    startTime = endTime;
    endTime = System.currentTimeMillis();
    totalTime = endTime - startTime;
    log.trace("Withdrawals handling time: {} ms, {} seconds", totalTime, totalTime / 1000f);

  }

  private Map<String, Set<AggregatedTxIn>> buildTxInsMap(Collection<AggregatedTx> txList) {
    return txList.stream()
        .collect(Collectors.toConcurrentMap(
            AggregatedTx::getHash, AggregatedTx::getTxInputs, (a, b) -> a));
  }

  private Map<String, Set<AggregatedTxIn>> buildCollateralTxInsMap(
      Collection<AggregatedTx> txList) {
    return txList.stream()
        .filter(tx -> !CollectionUtils.isEmpty(tx.getCollateralInputs()))
        .collect(Collectors.toConcurrentMap(
            AggregatedTx::getHash, AggregatedTx::getCollateralInputs, (a, b) -> a));
  }

  private Map<String, Set<AggregatedTxIn>> buildReferenceTxInsMap(
      Collection<AggregatedTx> txList) {
    return txList.stream()
        .filter(tx -> !CollectionUtils.isEmpty(tx.getReferenceInputs()))
        .collect(Collectors.toConcurrentMap(
            AggregatedTx::getHash, AggregatedTx::getReferenceInputs, (a, b) -> a));
  }

  private Map<String, List<AggregatedTxOut>> buildAggregatedTxOutMap(
      Collection<AggregatedTx> txList) {
    return txList.stream()
        .filter(tx -> !CollectionUtils.isEmpty(tx.getTxOutputs()))
        .collect(Collectors.toConcurrentMap(
            AggregatedTx::getHash, AggregatedTx::getTxOutputs, (a, b) -> a));
  }

  private Map<String, List<AggregatedTxOut>> buildCollateralTxOutMap(
      Collection<AggregatedTx> txList) {
    return txList.stream()
        .filter(tx -> Objects.nonNull(tx.getCollateralReturn()))
        .collect(Collectors.toConcurrentMap(
            AggregatedTx::getHash,
            tx -> List.of(tx.getCollateralReturn()),
            (a, b) -> a));
  }

  private void handleCertificates(Collection<AggregatedTx> successTxs,
      Map<String, Tx> txMap, Map<RedeemerReference<?>, Redeemer> redeemersMap) {
    successTxs.forEach(aggregatedTx -> {
      Tx tx = txMap.get(aggregatedTx.getHash());
      if (CollectionUtils.isEmpty(aggregatedTx.getCertificates())) {
        return;
      }

      IntStream.range(0, aggregatedTx.getCertificates().size()).forEach(idx -> {
        Certificate certificate = aggregatedTx.getCertificates().get(idx);

        // Only stake de-registration and stake delegation have redeemers
        RedeemerReference<Certificate> redeemerReference =
            new RedeemerReference<>(RedeemerTag.Cert, certificate);
        Redeemer redeemer = redeemersMap.get(redeemerReference);
        AggregatedBlock aggregatedBlock = blockDataService
            .getAggregatedBlock(aggregatedTx.getBlockHash());
        certificateSyncServiceFactory.handle(aggregatedBlock, certificate, idx, tx, redeemer);
      });
    });
  }

  public void handleExtraKeyWitnesses(Collection<AggregatedTx> successTxs,
      Collection<AggregatedTx> failedTxs, Map<String, Tx> txMap) {

    Map<String, Tx> mWitnessTx = new ConcurrentHashMap<>();
    Set<String> hashCollection = new ConcurrentSkipListSet<>();

    /*
     * Map all extra key witnesses hashes to its respective tx and add them to a set
     * which will be used to find all existing hashes from database. The existing hashes
     * will be opted out
     *
     * This process will be done asynchronously
     */
    Stream.concat(successTxs.parallelStream(), failedTxs.parallelStream())
        .filter(aggregatedTx -> !CollectionUtils.isEmpty(aggregatedTx.getRequiredSigners()))
        .forEach(aggregatedTx -> {
          Tx tx = txMap.get(aggregatedTx.getHash());
          aggregatedTx.getRequiredSigners().parallelStream().forEach(hash -> {
            mWitnessTx.put(hash, tx);
            hashCollection.add(hash);
          });
        });

    if (CollectionUtils.isEmpty(hashCollection)) {
      return;
    }

    Set<String> existsWitnessKeys =
        cachedExtraKeyWitnessRepository.findByHashIn(hashCollection);

    // Opt out all existing hashes
    hashCollection.removeAll(existsWitnessKeys);

    // Create new extra key witnesses records
    List<ExtraKeyWitness> extraKeyWitnesses = hashCollection.stream()
        .map(hash -> ExtraKeyWitness.builder()
            .hash(hash)
            .tx(mWitnessTx.get(hash))
            .build())
        .collect(Collectors.toList());

    if (!CollectionUtils.isEmpty(extraKeyWitnesses)) {
      cachedExtraKeyWitnessRepository.saveAll(extraKeyWitnesses);
    }
  }
}
