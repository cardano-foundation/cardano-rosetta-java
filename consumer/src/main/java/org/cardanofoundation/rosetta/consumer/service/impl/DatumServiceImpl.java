package org.cardanofoundation.rosetta.consumer.service.impl;

/**
 * ; Valid blocks must also satisfy the following two constraints: ; 1) the length of
 * transaction_bodies and transaction_witness_sets ;    must be the same ; 2) every
 * transaction_index must be strictly smaller than the ;  length of transaction_bodies
 */

import com.bloxbean.cardano.client.util.HexUtil;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.cardanofoundation.rosetta.common.entity.Datum;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.ledgersync.Witnesses;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.consumer.repository.DatumRepository;
import org.cardanofoundation.rosetta.consumer.service.DatumService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import static org.cardanofoundation.rosetta.consumer.constant.ConsumerConstant.BATCH_QUERY_SIZE;

@Service
@RequiredArgsConstructor
public class DatumServiceImpl implements DatumService {

  private final DatumRepository datumRepository;

  @Override
  public void handleDatum(Collection<AggregatedTx> aggregatedTxs, Map<String, Tx> txMap) {
    Map<String, Datum> datumMap = new HashMap<>();
    Set<String> datumHashes = getDatumHashes(aggregatedTxs);
    Set<String> existingDatumHashesFromDb = getExistingDatumHashByHashIn(datumHashes);

    aggregatedTxs.forEach(aggregatedTx -> {
      var transactionWitness = aggregatedTx.getWitnesses();
      var tx = txMap.get(aggregatedTx.getHash());

      datumMap.putAll(
          handleTransactionOutput(aggregatedTx, tx, datumMap, existingDatumHashesFromDb));

      if (Objects.nonNull(transactionWitness)) {
        datumMap.putAll(
            handleTransactionWitness(transactionWitness, tx, datumMap, existingDatumHashesFromDb));
      }
    });

    datumRepository.saveAll(datumMap.values());
  }

  private Set<String> getDatumHashes(Collection<AggregatedTx> aggregatedTxs) {
    Set<String> datumHashes = new HashSet<>();

    aggregatedTxs.forEach(aggregatedTx -> {
      Witnesses txWitnesses = aggregatedTx.getWitnesses();
      if (Objects.nonNull(txWitnesses) && !CollectionUtils.isEmpty(txWitnesses.getDatums())) {
        datumHashes.addAll(txWitnesses
            .getDatums()
            .stream()
            .map(org.cardanofoundation.rosetta.common.ledgersync.Datum::getHash)
            .collect(Collectors.toSet()));
      }

      aggregatedTx.getTxOutputs().forEach(transactionOutput -> {
        if (Objects.nonNull(transactionOutput.getInlineDatum())) {
          datumHashes.add(transactionOutput.getInlineDatum().getHash());
        }
      });
    });

    return datumHashes;
  }

  private Map<String, Datum> handleTransactionWitness(
      Witnesses transactionWitness, Tx tx,
      Map<String, Datum> existingDatum,
      Set<String> existingDatumHashesFromDb) {
    if (Boolean.FALSE.equals(tx.getValidContract())) {
      return Collections.emptyMap();
    }

    Map<String, Datum> mDatumNeedSave = new HashMap<>();
    transactionWitness.getDatums().forEach(datum -> {
      boolean datumExist = existingDatumHashesFromDb.contains(datum.getHash())
          || existingDatum.containsKey(datum.getHash());

      if (!datumExist) {
        mDatumNeedSave.put(datum.getHash(),
            Datum.builder().
                hash(datum.getHash()).
                value(datum.getJson()).
                tx(tx).
                bytes(HexUtil.decodeHexString(datum.getCbor())).
                build());
      }
    });
    return mDatumNeedSave;
  }


  private Map<String, Datum> handleTransactionOutput(
      AggregatedTx aggregatedTx, Tx tx, Map<String, Datum> existingDatum,
      Set<String> existingDatumHashesFromDb) {
    if (Boolean.FALSE.equals(tx.getValidContract())) {
      return Collections.emptyMap();
    }

    Map<String, Datum> datumInlineNeedSave = new HashMap<>();
    aggregatedTx.getTxOutputs()
        .forEach(transactionOutput -> {
          org.cardanofoundation.rosetta.common.ledgersync.Datum inlineDatum = transactionOutput.getInlineDatum();
          if (Objects.nonNull(inlineDatum) &&
              !existingDatumHashesFromDb.contains(inlineDatum.getHash()) &&
              !existingDatum.containsKey(inlineDatum.getHash())) {
            datumInlineNeedSave.put(inlineDatum.getHash(),
                Datum.builder().
                    hash(inlineDatum.getHash()).
                    bytes(HexUtil.decodeHexString(inlineDatum.getCbor())).
                    tx(tx).
                    value(inlineDatum.getJson()).
                    build());
          }
        });
    return datumInlineNeedSave;
  }

  @Override
  public Map<String, Datum> getDatumsByHashes(Set<String> hashes) {
    Map<String, Datum> datumMap = new ConcurrentHashMap<>();

    var queryBatches = Lists.partition(new ArrayList<>(hashes), BATCH_QUERY_SIZE);
    queryBatches.parallelStream()
        .forEach(datumHashBatch ->
            datumRepository.getDatumByHashes(new HashSet<>(datumHashBatch))
                .parallelStream()
                .forEach(datumProjection -> {
                  Datum datum = Datum.builder()
                      .id(datumProjection.getId())
                      .hash(datumProjection.getHash())
                      .build();
                  datumMap.put(datumProjection.getHash(), datum);
                }));

    return datumMap;
  }

  private Set<String> getExistingDatumHashByHashIn(Collection<String> datumHashes) {
    Set<String> existingDatumHashes = new ConcurrentSkipListSet<>();
    var queryBatches = Lists.partition(new ArrayList<>(datumHashes), BATCH_QUERY_SIZE);

    queryBatches.parallelStream().forEach(datumHashBatch ->
        datumRepository.getExistHashByHashIn(new HashSet<>(datumHashBatch))
            .parallelStream()
            .forEach(existingDatumHashes::add));

    return existingDatumHashes;
  }
}
