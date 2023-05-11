package org.cardanofoundation.rosetta.consumer.service.impl;

import com.bloxbean.cardano.client.transaction.spec.RedeemerTag;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedAddressBalance;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTxIn;
import org.cardanofoundation.rosetta.common.entity.MaTxOut;
import org.cardanofoundation.rosetta.common.entity.Redeemer;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.entity.TxIn;
import org.cardanofoundation.rosetta.common.entity.TxIn.TxInBuilder;
import org.cardanofoundation.rosetta.common.entity.TxOut;
import org.cardanofoundation.rosetta.consumer.constant.ConsumerConstant;
import org.cardanofoundation.rosetta.consumer.dto.RedeemerReference;
import org.cardanofoundation.rosetta.common.ledgersync.Era;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedMultiAssetTxOutRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedTxInRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedTxOutRepository;
import org.cardanofoundation.rosetta.consumer.service.BlockDataService;
import org.cardanofoundation.rosetta.consumer.service.EpochService;
import org.cardanofoundation.rosetta.consumer.service.TxInService;
import org.cardanofoundation.rosetta.consumer.service.TxOutService;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TxInServiceImpl implements TxInService {

  BlockDataService blockDataService;
  TxOutService txOutService;
  EpochService epochService;

  CachedTxInRepository cachedTxInRepository;
  CachedTxOutRepository cachedTxOutRepository;
  CachedMultiAssetTxOutRepository cachedMultiAssetTxOutRepository;

  @Override
  public void handleTxIns(Collection<AggregatedTx> txs, Map<String, Set<AggregatedTxIn>> txInMap,
      Map<String, Tx> txMap, Map<RedeemerReference<?>, Redeemer> redeemersMap) {
    AtomicBoolean shouldFindAssets = new AtomicBoolean(false);
    Map<Pair<String, Short>, TxOut> txOutMap = getTxOutFromTxInsMap(txInMap);

    // Calculate tx fee or deposit value then update epoch if needed and
    // check if assets should be taken into account in transaction inputs
    txs.parallelStream().forEach(aggregatedTx -> {
      Set<AggregatedTxIn> txIns = txInMap.get(aggregatedTx.getHash());
      if (CollectionUtils.isEmpty(txIns)) {
        return;
      }

      AggregatedBlock aggregatedBlock = blockDataService
          .getAggregatedBlock(aggregatedTx.getBlockHash());
      Tx tx = txMap.get(aggregatedTx.getHash());
      if (aggregatedBlock.getEra() == Era.BYRON) {
        calculateByronFee(tx, txIns, txOutMap);
        epochService.addFee(aggregatedBlock, tx.getFee());
      } else {
        shouldFindAssets.set(true);
        calculateShelleyDeposit(tx, txIns, txOutMap, aggregatedTx.getWithdrawals());
      }
    });

    Queue<TxIn> txInQueue = new ConcurrentLinkedQueue<>();
    txInMap.entrySet().parallelStream().forEach(entry -> {
      Set<AggregatedTxIn> txInSet = entry.getValue();
      String txHash = entry.getKey();
      Tx tx = txMap.get(txHash);

      txInSet.parallelStream().forEach(txIn -> {
        Redeemer redeemer = redeemersMap.get(new RedeemerReference<>(RedeemerTag.Spend, txIn));
        txInQueue.add(handleTxIn(tx, txIn, txOutMap, redeemer));
      });
    });

    handleTxInBalances(txInMap, txOutMap, shouldFindAssets.get());
    cachedTxInRepository.saveAll(txInQueue);
  }

  private Map<Pair<String, Short>, TxOut> getTxOutFromTxInsMap(
      Map<String, Set<AggregatedTxIn>> txInMap) {
    Set<AggregatedTxIn> txIns = txInMap.values().stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());

    // Key is a pair of tx hash and tx out index, value is the target tx out
    return txOutService
        .getTxOutCanUseByAggregatedTxIns(txIns)
        .parallelStream()
        .collect(Collectors.toConcurrentMap(
            txOut -> Pair.of(txOut.getTx().getHash(), txOut.getIndex()),
            Function.identity()
        ));
  }

  private void calculateShelleyDeposit(Tx tx, Set<AggregatedTxIn> txIns,
      Map<Pair<String, Short>, TxOut> txOutMap, Map<String, BigInteger> withdrawalsMap) {
    if (Boolean.FALSE.equals(tx.getValidContract())) {
      return;
    }

    Collection<BigInteger> withdrawalsCoin =
        CollectionUtils.isEmpty(withdrawalsMap) ?
            Collections.emptyList() : withdrawalsMap.values();
    var withdrawalSum = withdrawalsCoin.stream()
        .reduce(BigInteger.ZERO, BigInteger::add)
        .longValue();
    var inSum = getTxInSum(txOutMap, tx, txIns).longValue();
    var outSum = tx.getOutSum().longValue();
    var fees = tx.getFee().longValue();
    var deposit = inSum + withdrawalSum - outSum - fees;
    tx.setDeposit(deposit);
  }

  private void calculateByronFee(Tx tx,
      Set<AggregatedTxIn> txIns, Map<Pair<String, Short>, TxOut> txOutMap) {
    if (CollectionUtils.isEmpty(txIns)) {
      return;
    }

    BigInteger inSum = getTxInSum(txOutMap, tx, txIns);
    var fee = inSum.subtract(tx.getOutSum());
    tx.setFee(fee);
  }

  private static BigInteger getTxInSum(
      Map<Pair<String, Short>, TxOut> txOutMap, Tx tx, Set<AggregatedTxIn> txIns) {
    return txIns.stream()
        .map(txIn -> {
          TxOut txOut = txOutMap.get(Pair.of(txIn.getTxId(), (short) txIn.getIndex()));
          if (Objects.isNull(txOut)) {
            throw new IllegalStateException(String.format(
                "Tx in %s, index %d, of tx %s has no tx_out before",
                txIn.getTxId(), txIn.getIndex(), tx.getHash()));
          }
          return txOut;
        })
        .map(TxOut::getValue)
        .reduce(BigInteger.ZERO, BigInteger::add);
  }

  private TxIn handleTxIn(Tx tx, AggregatedTxIn txInput,
      Map<Pair<String, Short>, TxOut> txOutMap, Redeemer redeemer) {
    TxInBuilder<?, ?> txInBuilder = TxIn.builder();

    txInBuilder.txInput(tx);
    txInBuilder.redeemer(redeemer);

    Pair<String, Short> txOutKey = Pair.of(txInput.getTxId(), (short) txInput.getIndex());
    TxOut txOut = txOutMap.get(txOutKey);
    if (Objects.isNull(txOut)) {
      log.error("Tx in {}, index {}, of tx {}  has no tx_out before",
          txInput.getTxId(), txInput.getIndex(), tx.getHash());
      throw new IllegalStateException();
    }

    txInBuilder.txOut(txOut.getTx());
    txInBuilder.txOutIndex(txOut.getIndex());

    return txInBuilder.build();
  }

  private void handleTxInBalances(Map<String, Set<AggregatedTxIn>> txInMap,
      Map<Pair<String, Short>, TxOut> txOutMap, boolean shouldFindAssets) {
    // Byron does not have assets, hence this step is skipped
    Map<String, List<MaTxOut>> maTxOutMap = !shouldFindAssets
        ? Collections.emptyMap()
        : cachedMultiAssetTxOutRepository
            .findAllByTxOutIn(txOutMap.values())
            .parallelStream()
            .collect(Collectors.groupingByConcurrent(
                maTxOut -> txOutKeyAsString(maTxOut.getTxOut()),
                Collectors.toList()));

    txInMap.entrySet().parallelStream().forEach(entry ->
        entry.getValue().parallelStream().forEach(txIn -> {
          Pair<String, Short> txOutKey = Pair.of(txIn.getTxId(), (short) txIn.getIndex());
          TxOut txOut = txOutMap.get(txOutKey);
          String txHash = entry.getKey();
          String address = txOut.getAddress();
          AggregatedAddressBalance addressBalance = blockDataService
              .getAggregatedAddressBalanceFromAddress(address);

          // Subtract native balance
          addressBalance.subtractNativeBalance(txHash, txOut.getValue());

          // Subtract asset balances
          List<MaTxOut> maTxOuts = maTxOutMap.get(txOutKeyAsString(txOut));
          if (!CollectionUtils.isEmpty(maTxOuts)) {
            maTxOuts.parallelStream().forEach(maTxOut -> addressBalance.subtractAssetBalance(
                txHash, maTxOut.getIdent().getFingerprint(), maTxOut.getQuantity())
            );
          }
        }));
  }

  private String txOutKeyAsString(TxOut txOut) {
    if (cachedTxOutRepository.isNew(txOut)) {
      return String.join(
          ConsumerConstant.UNDERSCORE,
          txOut.getTx().getHash(),
          txOut.getIndex().toString()
      );
    }

    return txOut.getId().toString();
  }
}
