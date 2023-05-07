package org.cardanofoundation.rosetta.consumer.service.impl;

import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTxIn;
import org.cardanofoundation.rosetta.common.entity.ReferenceTxIn;
import org.cardanofoundation.rosetta.common.entity.ReferenceTxIn.ReferenceTxInBuilder;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.entity.TxOut;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedReferenceInputRepository;
import org.cardanofoundation.rosetta.consumer.service.ReferenceInputService;
import org.cardanofoundation.rosetta.consumer.service.TxOutService;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Service
public class ReferenceInputServiceImpl implements ReferenceInputService {

  CachedReferenceInputRepository cachedReferenceInputRepository;
  TxOutService txOutService;

  @Override
  public List<ReferenceTxIn> handleReferenceInputs(
      Map<byte[], Set<AggregatedTxIn>> referenceTxInMap, Map<byte[], Tx> txMap) {
    Set<AggregatedTxIn> allReferenceTxIns = referenceTxInMap.values()
        .stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
    Map<Pair<byte[], Integer>, TxOut> txOutMap = txOutService
        .getTxOutCanUseByAggregatedTxIns(allReferenceTxIns)
        .stream()
        .collect(Collectors.toMap(this::getTxOutKey, Function.identity()));
    return cachedReferenceInputRepository.saveAll(
        referenceTxInMap.entrySet().stream().flatMap(txHashReferenceTxInsEntry -> {
          Set<AggregatedTxIn> referenceTxIns = txHashReferenceTxInsEntry.getValue();
          byte[] txHash = txHashReferenceTxInsEntry.getKey();
          Tx tx = txMap.get(txHash);
          return referenceTxIns.stream()
              .map(referInput -> handleReferenceInput(tx, referInput, txOutMap));
        }).collect(Collectors.toList())
    );
  }

  private Pair<byte[], Integer> getTxOutKey(TxOut txOut) {
    return Pair.of(txOut.getTx().getHash(), (int) txOut.getIndex());
  }

  public ReferenceTxIn handleReferenceInput(Tx tx,
      AggregatedTxIn referenceInput, Map<Pair<byte[], Integer>, TxOut> txOutMap) {
    ReferenceTxInBuilder<?, ?> referenceTxInBuilder = ReferenceTxIn.builder();
    referenceTxInBuilder.txIn(tx);

    Pair<String, Integer> txOutKey = Pair.of(referenceInput.getTxId(), referenceInput.getIndex());
    TxOut txOut = txOutMap.get(txOutKey);
    referenceTxInBuilder.txOut(txOut.getTx());
    referenceTxInBuilder.txOutIndex(txOut.getIndex());
    return referenceTxInBuilder.build();
  }
}
