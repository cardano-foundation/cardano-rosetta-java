package org.cardanofoundation.rosetta.consumer.service.impl;

/**
 * ; Valid blocks must also satisfy the following two constraints: ; 1) the length of
 * transaction_bodies and transaction_witness_sets ;    must be the same ; 2) every
 * transaction_index must be strictly smaller than the ;  length of transaction_bodies
 */

import com.bloxbean.cardano.client.util.HexUtil;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.common.entity.Datum;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.consumer.dto.DatumDTO;
import org.cardanofoundation.rosetta.common.ledgersync.Witnesses;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedDatumRepository;
import org.cardanofoundation.rosetta.consumer.service.DatumService;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class DatumServiceImpl implements DatumService {

  private final CachedDatumRepository cachedDatumRepository;

  @Override
  public void handleDatum(DatumDTO dto) {
    Map<String, Datum> datums = dto.getDatums();
    var transactionBody = dto.getTransactionBody();
    var transactionWitness = dto.getTransactionWitness();
    var tx = dto.getTx();

    if (Objects.nonNull(transactionBody)) {
      datums.putAll(handleTransactionOutput(transactionBody, tx));
    }
    if (Objects.nonNull(transactionWitness)) {
      datums.putAll(handleTransactionWitness(transactionWitness, tx));
    }
  }


  private Map<String, Datum> handleTransactionWitness(Witnesses transactionWitness, Tx tx) {
    if (Boolean.FALSE.equals(tx.getValidContract())) {
      return Collections.emptyMap();
    }
    Set<String> datumHashes = transactionWitness.getDatums().stream()
        .map(org.cardanofoundation.rosetta.common.ledgersync.Datum::getHash)
        .collect(Collectors.toSet());
    if (CollectionUtils.isEmpty(datumHashes)) {
      return Collections.emptyMap();
    }

    Set<String> datumExist = cachedDatumRepository.getExistHashByHashIn(datumHashes);
    Map<String, Datum> mDatumNeedSave = new HashMap<>();
    transactionWitness.getDatums().forEach(datum -> {
      if (!datumExist.contains(datum.getHash())) {
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


  private Map<String, Datum> handleTransactionOutput(AggregatedTx aggregatedTx, Tx tx) {
    Set<String> datumHashes = new HashSet<>();
    if (Boolean.FALSE.equals(tx.getValidContract())) {
      return Collections.emptyMap();
    }
    aggregatedTx.getTxOutputs().forEach(transactionOutput -> {
      if (Objects.nonNull(transactionOutput.getInlineDatum())) {
        datumHashes.add(transactionOutput.getInlineDatum().getHash());
      }
    });
    if (CollectionUtils.isEmpty(datumHashes)) {
      return Collections.emptyMap();
    }

    Set<String> datumExist = cachedDatumRepository.getExistHashByHashIn(datumHashes);

    Map<String, Datum> datumInlineNeedSave = new HashMap<>();
    aggregatedTx.getTxOutputs()
        .forEach(transactionOutput -> {
          org.cardanofoundation.rosetta.common.ledgersync.Datum inlineDatum = transactionOutput.getInlineDatum();
          if (Objects.nonNull(inlineDatum) && !datumExist.contains(inlineDatum.getHash())) {
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
    return cachedDatumRepository.getDatumByHashes(hashes);
  }
}
