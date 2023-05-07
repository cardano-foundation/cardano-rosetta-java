package org.cardanofoundation.rosetta.consumer.service.impl;

import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedAddress;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTxIn;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTxOut;
import org.cardanofoundation.rosetta.common.entity.Datum;
import org.cardanofoundation.rosetta.common.entity.MaTxOut;
import org.cardanofoundation.rosetta.common.entity.Script;
import org.cardanofoundation.rosetta.common.entity.StakeAddress;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.entity.TxOut;
import org.cardanofoundation.rosetta.common.entity.TxOut.TxOutBuilder;
import org.cardanofoundation.rosetta.consumer.dto.TransactionOutMultiAssets;
import org.cardanofoundation.rosetta.common.ledgersync.constant.Constant;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedMultiAssetTxOutRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedTxOutRepository;
import org.cardanofoundation.rosetta.consumer.service.DatumService;
import org.cardanofoundation.rosetta.consumer.service.MultiAssetService;
import org.cardanofoundation.rosetta.consumer.service.ScriptService;
import org.cardanofoundation.rosetta.consumer.service.StakeAddressService;
import org.cardanofoundation.rosetta.consumer.service.TxOutService;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Service
public class TxOutServiceImpl implements TxOutService {

  DatumService datumService;
  ScriptService scriptService;
  MultiAssetService multiAssetService;
  StakeAddressService stakeAddressService;

  CachedTxOutRepository cachedTxOutRepository;
  CachedMultiAssetTxOutRepository cachedMultiAssetTxOutRepository;

  @Override
  public Collection<TxOut> getTxOutCanUseByAggregatedTxIns(Collection<AggregatedTxIn> txIns) {
    if (CollectionUtils.isEmpty(txIns)) {
      return Collections.emptySet();
    }

    var txHashIndexPairs = txIns.stream()
        .map(txIn -> {
          String txHash = txIn.getTxId().trim();
          short index = (short) txIn.getIndex();
          return Pair.of(txHash, index);
        }).sorted(Comparator.comparing(Pair::getFirst)).collect(Collectors.toList());
    return cachedTxOutRepository.findTxOutsByTxHashInAndTxIndexIn(txHashIndexPairs);
  }

  @Override
  public Collection<TxOut> prepareTxOuts(
      Map<byte[], List<AggregatedTxOut>> aggregatedTxOutMap, Map<byte[], Tx> txMap) {
    if (CollectionUtils.isEmpty(aggregatedTxOutMap)) {
      return Collections.emptyList();
    }

    Set<String> scriptHashes = new ConcurrentSkipListSet<>();
    Set<String> datumHashes = new ConcurrentSkipListSet<>();

    Queue<TransactionOutMultiAssets> txOutAndMas = new ConcurrentLinkedQueue<>();
    aggregatedTxOutMap.entrySet().parallelStream().forEach(entry -> {
      byte[] txHash = entry.getKey();
      Tx tx = txMap.get(txHash);
      var txOutputs = entry.getValue();

      txOutputs.parallelStream().forEach(aggregatedTxOut -> {
        TransactionOutMultiAssets txOutAndMa = handleTxOutAndMultiAsset(tx, aggregatedTxOut);
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

    cachedTxOutRepository.saveAll(txOutList);
    var maTxOuts = multiAssetService.updateIdentMaTxOuts(pMaTxOuts);
    cachedMultiAssetTxOutRepository.saveAll(maTxOuts);
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

  private TransactionOutMultiAssets handleTxOutAndMultiAsset(Tx tx, AggregatedTxOut txOutput) {
    TxOutBuilder<?, ?> txOutBuilder = TxOut.builder();

    txOutBuilder.tx(tx);
    txOutBuilder.index(txOutput.getIndex().shortValue());
    txOutBuilder.dataHash(txOutput.getDatumHash());
    txOutBuilder.addressHasScript(false);
    // stake address
    AggregatedAddress aggregatedAddress = txOutput.getAddress();
    StakeAddress stakeAddress = stakeAddressService
        .getStakeAddress(aggregatedAddress.getStakeAddress());
    txOutBuilder.stakeAddress(stakeAddress);

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
          hasMultiAsset.set(true);
        });

    BigInteger nativeAmount = txOutput.getNativeAmount();
    txOutBuilder.value(nativeAmount);
    if (nativeAmount.compareTo(BigInteger.valueOf(0)) > 0 && hasMultiAsset.get()) {
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
