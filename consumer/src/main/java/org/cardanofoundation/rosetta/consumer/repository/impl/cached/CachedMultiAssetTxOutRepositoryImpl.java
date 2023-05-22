package org.cardanofoundation.rosetta.consumer.repository.impl.cached;

import static org.cardanofoundation.rosetta.consumer.constant.ConsumerConstant.BATCH_QUERY_SIZE;

import com.google.common.collect.Lists;
import org.cardanofoundation.rosetta.common.entity.MaTxOut;
import org.cardanofoundation.rosetta.common.entity.MultiAsset;
import org.cardanofoundation.rosetta.common.entity.TxOut;
import org.cardanofoundation.rosetta.consumer.projection.MaTxOutProjection;
import org.cardanofoundation.rosetta.consumer.repository.MultiAssetTxOutRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedMultiAssetTxOutRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;
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
public class CachedMultiAssetTxOutRepositoryImpl implements CachedMultiAssetTxOutRepository {

  InMemoryCachedEntities inMemoryCachedEntities;
  MultiAssetTxOutRepository multiAssetTxOutRepository;

  @Override
  public Collection<MaTxOut> findAllByTxOutIn(Collection<TxOut> txOuts) {
    Set<Long> nonExistentTxOutIds = new ConcurrentSkipListSet<>();
    Queue<MaTxOut> maTxOuts = new ConcurrentLinkedQueue<>();

    txOuts.parallelStream().forEach(txOut -> {
      Pair<String, Short> txHashIndexPair = txOutToTxHashIndexPair(txOut);
      Collection<MaTxOut> maTxOutList = inMemoryCachedEntities.getMaTxOutMap().get(txHashIndexPair);
      if (!CollectionUtils.isEmpty(maTxOutList)) {
        maTxOuts.addAll(maTxOutList);
        return;
      }

      // If the current TxOut is new, we only find its MaTxOuts in current block
      if (isNew(txOut)) {
        return;
      }

      // We use the same mechanism as HashSet for concurrency
      nonExistentTxOutIds.add(txOut.getId());
    });

    int cacheHit = maTxOuts.size();
    long startTime = System.currentTimeMillis();

    var queryBatches = Lists.partition(new ArrayList<>(nonExistentTxOutIds), BATCH_QUERY_SIZE);
    queryBatches.parallelStream().forEach(batch -> {
      List<MaTxOutProjection> maTxOutProjections =
          multiAssetTxOutRepository.findAllByTxOutIdsIn(batch);
      if (!CollectionUtils.isEmpty(maTxOutProjections)) {
        maTxOutProjections.parallelStream().forEach(maTxOutProjection -> {
          TxOut txOut = TxOut.builder().id(maTxOutProjection.getTxOutId()).build();

          MultiAsset multiAsset = MultiAsset.builder()
              .fingerprint(maTxOutProjection.getFingerprint())
              .build();

          MaTxOut maTxOut = MaTxOut.builder()
              .txOut(txOut)
              .ident(multiAsset)
              .quantity(maTxOutProjection.getQuantity())
              .build();
          maTxOuts.add(maTxOut);
        });
      }
    });

    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    log.trace("Find multi asset tx out: total {}, cache hit {}, query {}, time {} second(s)", maTxOuts.size(),
        cacheHit, nonExistentTxOutIds.size(), totalTime / 1000f);

    return maTxOuts;
  }

  @Override
  public List<MaTxOut> saveAll(Collection<MaTxOut> entities) {
    entities.parallelStream().forEach(maTxOut -> {
      Pair<String, Short> txHashIndexPair = txOutToTxHashIndexPair(maTxOut.getTxOut());
      Queue<MaTxOut> maTxOuts = inMemoryCachedEntities.getMaTxOutMap()
          .computeIfAbsent(txHashIndexPair, p -> new ConcurrentLinkedQueue<>());
      maTxOuts.add(maTxOut);
    });
    return new ArrayList<>(entities);
  }

  private static Pair<String, Short> txOutToTxHashIndexPair(TxOut txOut) {
    return Pair.of(txOut.getTx().getHash(), txOut.getIndex());
  }

  @Override
  public void flushToDb() {
    long startTime = System.currentTimeMillis();
    var maTxOut = inMemoryCachedEntities.getMaTxOutMap()
        .values()
        .stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
    var maTxOutSize = maTxOut.size();
    multiAssetTxOutRepository.saveAll(maTxOut);
    inMemoryCachedEntities.getMaTxOutMap().clear();
    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    log.debug("MultiAssetTxOut {} elapsed: {} ms, {} second(s)", maTxOutSize, totalTime,
        totalTime / 1000f);
  }
}
