package org.cardanofoundation.rosetta.consumer.service.impl;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.common.entity.*;
import org.cardanofoundation.rosetta.common.entity.TxOut.TxOutBuilder;
import org.cardanofoundation.rosetta.common.enumeration.TokenType;
import org.cardanofoundation.rosetta.common.ledgersync.constant.Constant;
import org.cardanofoundation.rosetta.common.util.JsonUtil;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedAddress;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTxIn;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTxOut;
import org.cardanofoundation.rosetta.consumer.dto.TransactionOutMultiAssets;
import org.cardanofoundation.rosetta.consumer.repository.MultiAssetTxOutRepository;
import org.cardanofoundation.rosetta.consumer.repository.TxOutRepository;
import org.cardanofoundation.rosetta.consumer.service.*;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.cardanofoundation.rosetta.consumer.constant.ConsumerConstant.TX_OUT_BATCH_QUERY_SIZE;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Service
public class TxOutServiceImpl implements TxOutService {

  DatumService datumService;
  ScriptService scriptService;
  MultiAssetService multiAssetService;
  StakeAddressService stakeAddressService;

  TxOutRepository txOutRepository;
  MultiAssetTxOutRepository multiAssetTxOutRepository;

  @Override
  public Collection<TxOut> getTxOutCanUseByAggregatedTxIns(Collection<AggregatedTxIn> txIns) {
    if (CollectionUtils.isEmpty(txIns)) {
      return Collections.emptySet();
    }
    Queue<TxOut> txOuts = new ConcurrentLinkedQueue<>();

    var txHashIndexPairs = txIns.stream()
        .map(txIn -> {
          String txHash = txIn.getTxId().trim();
          short index = (short) txIn.getIndex();
          return Pair.of(txHash, index);
        }).sorted(Comparator.comparing(Pair::getFirst)).collect(Collectors.toList());

    Lists.partition(txHashIndexPairs, TX_OUT_BATCH_QUERY_SIZE)
        .parallelStream()
        .forEach(txHashIndexPairBatch -> txOutRepository
            .findTxOutsByTxHashInAndTxIndexIn(txHashIndexPairBatch)
            .parallelStream()
            .forEach(txOutProjection -> {
              Tx tx = Tx.builder()
                  .id(txOutProjection.getTxId())
                  .hash(txOutProjection.getTxHash())
                  .build();

              TxOut txOut = TxOut.builder()
                  .id(txOutProjection.getId())
                  .index(txOutProjection.getIndex())
                  .address(txOutProjection.getAddress())
                  .addressHasScript(txOutProjection.getAddressHasScript())
                  .paymentCred(txOutProjection.getPaymentCred())
                  .stakeAddress(
                      StakeAddress.builder().id(txOutProjection.getStakeAddressId()).build())
                  .value(txOutProjection.getValue())
                  .tx(tx)
                  .build();
              txOuts.add(txOut);
            }));
    return txOuts;
  }

  @Override
  public Collection<TxOut> prepareTxOuts(
      Map<String, List<AggregatedTxOut>> aggregatedTxOutMap,
      Map<String, Tx> txMap, Map<String, StakeAddress> stakeAddressMap) {
    if (CollectionUtils.isEmpty(aggregatedTxOutMap)) {
      return new ArrayList<>();
    }

    Set<String> scriptHashes = new ConcurrentSkipListSet<>();
    Set<String> datumHashes = new ConcurrentSkipListSet<>();

    Queue<TransactionOutMultiAssets> txOutAndMas = new ConcurrentLinkedQueue<>();
    aggregatedTxOutMap.entrySet().parallelStream().forEach(entry -> {
      String txHash = entry.getKey();
      Tx tx = txMap.get(txHash);
      var txOutputs = entry.getValue();

      txOutputs.parallelStream().forEach(aggregatedTxOut -> {
        TransactionOutMultiAssets txOutAndMa =
            handleTxOutAndMultiAsset(tx, aggregatedTxOut, stakeAddressMap);
        if (StringUtils.hasText(aggregatedTxOut.getScriptRef())) {
          scriptHashes.add(scriptService.getHashOfReferenceScript(aggregatedTxOut.getScriptRef()));
        }
        if (StringUtils.hasText(aggregatedTxOut.getDatumHash())) {
          datumHashes.add(aggregatedTxOut.getDatumHash());
        }
        if (Objects.nonNull(aggregatedTxOut.getInlineDatum())
            && StringUtils.hasText(aggregatedTxOut.getInlineDatum().getHash())) {
          datumHashes.add(aggregatedTxOut.getInlineDatum().getHash());
        }
        txOutAndMas.add(txOutAndMa);
      });
    });

    //update Tx_out
    Map<String, Script> scriptMap = scriptService.getScriptsByHashes(scriptHashes);
    Map<String, Datum> datumMap = datumService.getDatumsByHashes(datumHashes);
    txOutAndMas.parallelStream().forEach(txOutAndMa -> {
      updateReferScript(scriptMap, txOutAndMa.getTxOut(), txOutAndMa.getScriptRefer());
      updateInlineDatum(datumMap, txOutAndMa.getTxOut(), txOutAndMa.getDatumHash());
    });

    //Save to tx_out and multi asset
    List<TxOut> txOutList = txOutAndMas.stream()
        .map(TransactionOutMultiAssets::getTxOut)
        .collect(Collectors.toList());

    // Convert MaTxOut maps to a single map
    MultiValueMap<String, MaTxOut> pMaTxOuts = txOutAndMas.stream()
        .map(TransactionOutMultiAssets::getPMaTxOuts)
        .reduce(new LinkedMultiValueMap<>(), (m1, m2) -> {
          m1.addAll(m2);
          return m1;
        });

    txOutRepository.saveAll(txOutList);
    var maTxOuts = multiAssetService.updateIdentMaTxOuts(pMaTxOuts);
    multiAssetTxOutRepository.saveAll(maTxOuts);
    return txOutList;
  }

  //update reference_script_id
  private void updateReferScript(Map<String, Script> scriptMap, TxOut txOut, String scriptHash) {
    if (Objects.isNull(scriptHash)) {
      return;
    }

    Script script = scriptMap.get(scriptHash);
    if (Objects.nonNull(script)) {
      txOut.setReferenceScript(script);
    } else {
      Tx tx = txOut.getTx();
      log.debug("Script not found tx {}, valid {}, index {}, script hash {}", tx.getHash(),
          tx.getValidContract(), txOut.getIndex(), scriptHash);
      txOut.setReferenceScript(null);
    }
  }

  private void updateInlineDatum(Map<String, Datum> datumMap, TxOut txOut, String datumHash) {
    if (Objects.isNull(datumHash)) {
      return;
    }

    Datum datum = datumMap.get(datumHash);
    if (Objects.nonNull(datum)) {
      txOut.setInlineDatum(datum);
    } else {
      Tx tx = txOut.getTx();
      log.trace("Datum not found tx {}, valid {}, index {}, datum hash {}", tx.getHash(),
          tx.getValidContract(), txOut.getIndex(), datumHash);
      txOut.setReferenceScript(null);
    }
  }

  private TransactionOutMultiAssets handleTxOutAndMultiAsset(
      Tx tx, AggregatedTxOut txOutput, Map<String, StakeAddress> stakeAddressMap) {
    TxOutBuilder<?, ?> txOutBuilder = TxOut.builder();

    txOutBuilder.tx(tx);
    txOutBuilder.index(txOutput.getIndex().shortValue());
    txOutBuilder.dataHash(txOutput.getDatumHash());
    txOutBuilder.tokenType(TokenType.NATIVE_TOKEN);
    txOutBuilder.addressHasScript(false);
    // stake address
    AggregatedAddress aggregatedAddress = txOutput.getAddress();
    String rawStakeAddress = aggregatedAddress.getStakeAddress();
    if (StringUtils.hasText(rawStakeAddress)) {
      StakeAddress stakeAddress = stakeAddressMap.get(rawStakeAddress);
      txOutBuilder.stakeAddress(stakeAddress);
    }

    // payment cred
    txOutBuilder.paymentCred(aggregatedAddress.getPaymentCred());

    // address has script
    txOutBuilder.addressHasScript(aggregatedAddress.isAddressHasScript());

    txOutBuilder.address(aggregatedAddress.getAddress());
    txOutBuilder.addressRaw(aggregatedAddress.getAddressRaw());

    AtomicBoolean hasMultiAsset = new AtomicBoolean(false);
    txOutput.getAmounts().stream()
        .filter(amount -> !Constant.isLoveLace(amount.getAssetName()))
        .findAny()
        .ifPresent(amount -> {
          txOutBuilder.tokenType(TokenType.TOKEN);
          hasMultiAsset.set(true);
        });

    BigInteger nativeAmount = txOutput.getNativeAmount();
    txOutBuilder.value(nativeAmount);
    if (nativeAmount.compareTo(BigInteger.valueOf(0)) > 0 && hasMultiAsset.get()) {
      txOutBuilder.tokenType(TokenType.ALL_TOKEN_TYPE);
    }

    //Script
    String scriptHash = null;
    if (StringUtils.hasText(txOutput.getScriptRef())) {
      scriptHash = scriptService.getHashOfReferenceScript(txOutput.getScriptRef());
    }

    //Datum
    String datumHash = null;
    if (StringUtils.hasText(txOutput.getDatumHash())) {
      datumHash = txOutput.getDatumHash();
    }

    if (Objects.nonNull(txOutput.getInlineDatum())) {
      datumHash = txOutput.getInlineDatum().getHash();
    }

    TxOut txOut = txOutBuilder.build();
    MultiValueMap<String, MaTxOut> pMaTxOuts = multiAssetService.buildMaTxOut(txOutput, txOut);
    return TransactionOutMultiAssets.builder()
        .pMaTxOuts(pMaTxOuts)
        .scriptRefer(scriptHash)
        .datumHash(datumHash)
        .txOut(txOut)
        .build();
  }
}
