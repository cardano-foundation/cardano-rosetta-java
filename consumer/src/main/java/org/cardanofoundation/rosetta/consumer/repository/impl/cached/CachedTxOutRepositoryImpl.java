package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import static org.cardanofoundation.rosetta.consumer.constant.ConsumerConstant.TX_OUT_BATCH_QUERY_SIZE;

import com.google.common.collect.Lists;
import org.cardanofoundation.rosetta.common.entity.StakeAddress;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.entity.TxOut;
import org.cardanofoundation.rosetta.consumer.repository.TxOutRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedTxOutRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

@Slf4j
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CachedTxOutRepositoryImpl implements CachedTxOutRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  TxOutRepository txOutRepository;

  @Override
  public Collection<TxOut> findTxOutsByTxHashInAndTxIndexIn(
      Collection<Pair<String, Short>> txHashIndexPairs) {
    Queue<Pair<String, Short>> nonExistentTxHashIndexPairs = new ConcurrentLinkedQueue<>();
    Queue<TxOut> txOuts = new ConcurrentLinkedQueue<>();

    txHashIndexPairs.parallelStream().forEach(txHashIndexPair -> {
      TxOut txOut = inMemoryCachedEntities.getTxOutMap().get(txHashIndexPair);
      if (Objects.isNull(txOut)) {
        nonExistentTxHashIndexPairs.add(txHashIndexPair);
        return;
      }

      txOuts.add(txOut);
    });

    int cacheHit = txOuts.size();
    long startTime = System.currentTimeMillis();

    var queryBatches = Lists.partition(
        new ArrayList<>(nonExistentTxHashIndexPairs), TX_OUT_BATCH_QUERY_SIZE);
    queryBatches.parallelStream().forEach(batch ->
        txOutRepository.findTxOutsByTxHashInAndTxIndexIn(batch).parallelStream().forEach(
            txOutProjection -> {
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
            }
        ));

    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    log.trace("Find tx out of tx in: total {}, cache hit {}, query {}, time {} second(s)", txHashIndexPairs.size(),
        cacheHit, nonExistentTxHashIndexPairs.size(), totalTime / 1000f);

    return txOuts;
  }

  @Override
  public Optional<TxOut> findTxOutByTxHashAndTxOutIndex(String txHash, Short index) {
    Pair<String, Short> txHashIndexPair = Pair.of(txHash, index);
    // This set should have only one value
    Collection<TxOut> txOuts = findTxOutsByTxHashInAndTxIndexIn(List.of(txHashIndexPair));

    if (CollectionUtils.isEmpty(txOuts)) {
      return Optional.empty();
    }

    return txOuts.stream().findFirst();
  }

  @Override
  public TxOut save(TxOut entity) {
    Pair<byte[], Short> txHashIndexPair = Pair.of(entity.getTx().getHash(), entity.getIndex());
    inMemoryCachedEntities.getTxOutMap().put(txHashIndexPair, entity);
    return entity;
  }

  @Override
  public List<TxOut> saveAll(Collection<TxOut> entities) {
    entities.forEach(txOut -> {
      Pair<byte[], Short> txHashIndexPair = Pair.of(txOut.getTx().getHash(), txOut.getIndex());
      inMemoryCachedEntities.getTxOutMap().put(txHashIndexPair, txOut);
    });

    return new ArrayList<>(entities);
  }

  @Override
  public void flushToDb() {
    long startTime = System.currentTimeMillis();
    var txOut = inMemoryCachedEntities.getTxOutMap().values();
    var txOutSize = txOut.size();
    txOutRepository.saveAll(txOut);
    inMemoryCachedEntities.getTxOutMap().clear();
    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    log.debug("TxOut {} elapsed: {} ms, {} second(s)", txOutSize, totalTime, totalTime / 1000f);
  }
}
