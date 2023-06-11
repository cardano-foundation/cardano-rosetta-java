package org.cardanofoundation.rosetta.consumer.service.impl;

import com.bloxbean.cardano.client.transaction.spec.ExUnits;
import com.bloxbean.cardano.client.transaction.spec.RedeemerTag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.common.entity.Redeemer;
import org.cardanofoundation.rosetta.common.entity.Redeemer.RedeemerBuilder;
import org.cardanofoundation.rosetta.common.entity.RedeemerData;
import org.cardanofoundation.rosetta.common.entity.RedeemerData.RedeemerDataBuilder;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.entity.TxOut;
import org.cardanofoundation.rosetta.common.enumeration.ScriptPurposeType;
import org.cardanofoundation.rosetta.common.ledgersync.Amount;
import org.cardanofoundation.rosetta.common.ledgersync.Datum;
import org.cardanofoundation.rosetta.common.ledgersync.certs.CertType;
import org.cardanofoundation.rosetta.common.ledgersync.certs.Certificate;
import org.cardanofoundation.rosetta.common.ledgersync.certs.StakeDelegation;
import org.cardanofoundation.rosetta.common.ledgersync.certs.StakeDeregistration;
import org.cardanofoundation.rosetta.common.util.HexUtil;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTxIn;
import org.cardanofoundation.rosetta.consumer.dto.RedeemerPointer;
import org.cardanofoundation.rosetta.consumer.dto.RedeemerReference;
import org.cardanofoundation.rosetta.consumer.projection.TxOutProjection;
import org.cardanofoundation.rosetta.consumer.repository.RedeemerDataRepository;
import org.cardanofoundation.rosetta.consumer.repository.RedeemerRepository;
import org.cardanofoundation.rosetta.consumer.repository.TxOutRepository;
import org.cardanofoundation.rosetta.consumer.service.RedeemerService;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RedeemerServiceImpl implements RedeemerService {

  TxOutRepository txOutRepository;
  RedeemerRepository redeemerRepository;
  RedeemerDataRepository redeemerDataRepository;

  @Override
  public Map<RedeemerReference<?>, Redeemer> handleRedeemers(
      Collection<AggregatedTx> txs, Map<String, Tx> txMap,
      Map<Pair<String, Short>, TxOut> newTxOutMap) {
    List<AggregatedTx> txsWithRedeemers = txs.stream()
        .filter(tx -> Objects.nonNull(tx.getWitnesses()))
        .filter(tx -> !CollectionUtils.isEmpty(tx.getWitnesses().getRedeemers()))
        .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(txsWithRedeemers)) {
      return Collections.emptyMap();
    }

    Set<String> redeemerDataHash = txsWithRedeemers.stream()
        .flatMap(tx -> tx.getWitnesses().getRedeemers().stream())
        .map(redeemer -> redeemer.getPlutusData().getHash())
        .collect(Collectors.toSet());
    Map<String, RedeemerData> existingRedeemerDataMap = redeemerDataRepository
        .findAllByHashIn(redeemerDataHash)
        .stream()
        .collect(Collectors.toMap(RedeemerData::getHash, Function.identity()));
    Map<RedeemerReference<?>, Redeemer> redeemersMap = new HashMap<>();
    Map<String, RedeemerData> redeemerDataMap = new HashMap<>();

    txsWithRedeemers.forEach(aggregatedTx -> {
      Tx tx = txMap.get(aggregatedTx.getHash());
      var redeemers = aggregatedTx.getWitnesses().getRedeemers();

      IntStream.range(0, redeemers.size()).forEach(redeemerIndex -> {
        org.cardanofoundation.rosetta.common.ledgersync.Redeemer redeemerObj = redeemers.get(
            redeemerIndex);

        Datum plutusData = redeemerObj.getPlutusData();
        RedeemerData redeemerData = Optional
            .ofNullable(existingRedeemerDataMap.get(plutusData.getHash()))
            .orElseGet(() -> {
              // Redeemer data might have the same hash. Since we do batch insert,
              // we save previously created redeemer data from redeemer list into a map for
              // re-usability (the data hash is unique so saving new redeemer data entities
              // with the same hash will violate that constraint)
              RedeemerData previousRedeemerData = redeemerDataMap.get(plutusData.getHash());
              if (Objects.isNull(previousRedeemerData)) {
                previousRedeemerData = buildRedeemerData(plutusData, tx);
                redeemerDataMap.put(plutusData.getHash(), previousRedeemerData);
              }
              return previousRedeemerData;
            });

        Pair<RedeemerReference<?>, Redeemer> redeemerPair = buildRedeemer(
            redeemerObj, redeemerData, tx, aggregatedTx, newTxOutMap);
        redeemersMap.put(redeemerPair.getFirst(), redeemerPair.getSecond());
      });
    });

    redeemerDataRepository.saveAll(redeemerDataMap.values());
    redeemerRepository.saveAll(redeemersMap.values());

    return redeemersMap;
  }

  /**
   * Creates a pair of redeemer and a wrapper consists of referenced object and redeemer tag
   * (purpose)
   *
   * @param redeemerObj  redeemer object
   * @param redeemerData redeemer data
   * @param tx           transaction entity
   * @param aggregatedTx current redeemer's transaction body
   * @param newTxOutMap  a map of newly created txOut entities that are not inserted yet
   * @return
   */
  private Pair<RedeemerReference<?>, Redeemer> buildRedeemer(
      org.cardanofoundation.rosetta.common.ledgersync.Redeemer redeemerObj,
      RedeemerData redeemerData, Tx tx, AggregatedTx aggregatedTx,
      Map<Pair<String, Short>, TxOut> newTxOutMap) {
    RedeemerBuilder<?, ?> redeemerBuilder = Redeemer.builder();

    // This part should never be null
    RedeemerPointer<?> redeemerPointer = handleRedeemer(aggregatedTx, newTxOutMap, redeemerObj);

    ExUnits exUnits = redeemerObj.getExUnits();
    redeemerBuilder.unitMem(exUnits.getMem().longValue());
    redeemerBuilder.unitSteps(exUnits.getSteps().longValue());
    redeemerBuilder.purpose(ScriptPurposeType.fromValue(redeemerObj.getTag().name().toLowerCase()));
    redeemerBuilder.index(redeemerObj.getIndex().intValue());
    redeemerBuilder.scriptHash(redeemerPointer.getScriptHash());
    redeemerBuilder.redeemerData(redeemerData);
    redeemerBuilder.tx(tx);
    // TODO - fee (requires getting script fee data from node)

    RedeemerReference<?> redeemerReference =
        new RedeemerReference<>(redeemerObj.getTag(), redeemerPointer.getTargetReference());
    return Pair.of(redeemerReference, redeemerBuilder.build());
  }

  /**
   * Get redeemer referenced object from redeemer pointer The pointer object's parameter can be one
   * of the following types:
   * <ul>
   *   <li>
   * - Transaction input (TransactionInput)
   *   </li>
   *   <li>
   * - Certificate
   *   </li>
   *   <li>
   * - Reward account or policy ID (String)
   *   </li>
   * </ul>
   *
   * @param aggregatedTx transaction body
   * @param newTxOutMap  a map of newly created txOut entities that are not inserted yet
   * @param redeemerObj  redeemer object
   * @return a new pointer with referenced object and script hash
   */
  private RedeemerPointer<?> handleRedeemer(
      AggregatedTx aggregatedTx, Map<Pair<String, Short>, TxOut> newTxOutMap,
      org.cardanofoundation.rosetta.common.ledgersync.Redeemer redeemerObj) {
    RedeemerTag tag = redeemerObj.getTag();
    int pointerIndex = redeemerObj.getIndex().intValue();

    switch (tag) {
      case Spend:
        List<AggregatedTxIn> sortedTxInputs = aggregatedTx.getTxInputs().stream()
            .sorted((txIn1, txIn2) -> {
              String txHash1 = txIn1.getTxId();
              String txHash2 = txIn2.getTxId();
              int txHashComparison = txHash1.compareTo(txHash2);

              // Sort TxIn by its Tx hash. If equals, sort by TxOut index
              return txHashComparison != 0
                  ? txHashComparison
                  : Integer.compare(txIn1.getIndex(), txIn2.getIndex());
            }).collect(Collectors.toList());
        return handleTxInPtr(sortedTxInputs, pointerIndex, newTxOutMap);
      case Mint:
        return handleMintingPtr(aggregatedTx.getMint(), pointerIndex);
      case Cert:
        return handleCertPtr(aggregatedTx.getCertificates(), pointerIndex);
      default: // Reward pointer
        return handleRewardPtr(new ArrayList<>(aggregatedTx.getWithdrawals().keySet()),
            pointerIndex);
    }
  }

  private RedeemerPointer<AggregatedTxIn> handleTxInPtr(
      List<AggregatedTxIn> txInputs, int pointerIndex,
      Map<Pair<String, Short>, TxOut> newTxOutMap) {
    AggregatedTxIn txIn = txInputs.get(pointerIndex);

    // Find target TxOut from DB
    Optional<TxOutProjection> txOutOptional = txOutRepository
        .findTxOutByTxHashAndTxOutIndex(txIn.getTxId(), (short) txIn.getIndex());
    if (txOutOptional.isPresent()) {
      TxOutProjection txOut = txOutOptional.get();
      if (txOut.getAddressHasScript().equals(Boolean.TRUE)) {
        return new RedeemerPointer<>(txOut.getPaymentCred(), txIn);
      }
    }

    // Fallback to provided TxOuts
    Pair<String, Short> txOutKey = Pair.of(txIn.getTxId(), (short) txIn.getIndex());
    TxOut txOut = newTxOutMap.get(txOutKey);
    if (Objects.isNull(txOut)) {
      log.error("Can not find payment cred tx id {}, index {}",
          txIn.getTxId(), txIn.getIndex());
      throw new IllegalStateException();
    }

    return new RedeemerPointer<>(txOut.getPaymentCred(), txIn);
  }

  private RedeemerPointer<String> handleMintingPtr(List<Amount> mints, int pointerIndex) {
    Amount minting = mints.get(pointerIndex);
    String policyId = minting.getPolicyId();
    return new RedeemerPointer<>(policyId, policyId);
  }

  private RedeemerPointer<Certificate> handleCertPtr(
      List<Certificate> certificates, int pointerIndex) {
    Certificate certificate = certificates.get(pointerIndex);

    // Stake delegation
    if (certificate.getCertType() == CertType.STAKE_DELEGATION) {
      StakeDelegation delegation = (StakeDelegation) certificate;
      return new RedeemerPointer<>(delegation.getStakeCredential().getHash(), delegation);
    }

    // Stake de-registration
    StakeDeregistration stakeDeregistration = (StakeDeregistration) certificate;
    String scriptHash = stakeDeregistration.getStakeCredential().getHash();
    return new RedeemerPointer<>(scriptHash, stakeDeregistration);
  }

  private RedeemerPointer<String> handleRewardPtr(List<String> rewardAccounts, int pointerIndex) {
    String rewardAccount = rewardAccounts.get(pointerIndex);
    // Trim network tag
    String scriptHash = rewardAccount.substring(2);
    return new RedeemerPointer<>(scriptHash, rewardAccount);
  }

  private RedeemerData buildRedeemerData(Datum plutusData, Tx tx) {
    RedeemerDataBuilder<?, ?> redeemerDataBuilder = RedeemerData.builder();

    redeemerDataBuilder.hash(plutusData.getHash());
    redeemerDataBuilder.value(plutusData.getJson());
    redeemerDataBuilder.bytes(HexUtil.decodeHexString(plutusData.getCbor()));
    redeemerDataBuilder.tx(tx);

    return redeemerDataBuilder.build();
  }
}
