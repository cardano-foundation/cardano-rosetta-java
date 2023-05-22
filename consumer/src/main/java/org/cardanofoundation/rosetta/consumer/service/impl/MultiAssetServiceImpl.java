package org.cardanofoundation.rosetta.consumer.service.impl;

import com.bloxbean.cardano.client.util.HexUtil;
import org.cardanofoundation.rosetta.common.util.AssetUtil;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTxOut;
import org.cardanofoundation.rosetta.common.entity.BaseEntity;
import org.cardanofoundation.rosetta.common.entity.MaTxMint;
import org.cardanofoundation.rosetta.common.entity.MaTxOut;
import org.cardanofoundation.rosetta.common.entity.MultiAsset;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.entity.TxOut;
import org.cardanofoundation.rosetta.common.ledgersync.Amount;
import org.cardanofoundation.rosetta.common.ledgersync.constant.Constant;
import org.cardanofoundation.rosetta.consumer.projection.MaTxMintProjection;
import org.cardanofoundation.rosetta.consumer.projection.MultiAssetTotalVolumeProjection;
import org.cardanofoundation.rosetta.consumer.projection.MultiAssetTxCountProjection;
import org.cardanofoundation.rosetta.consumer.repository.MaTxMintRepository;
import org.cardanofoundation.rosetta.consumer.repository.MultiAssetRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedMaTxMintRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedMultiAssetRepository;
import org.cardanofoundation.rosetta.consumer.service.BlockDataService;
import org.cardanofoundation.rosetta.consumer.service.MultiAssetService;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Service
public class MultiAssetServiceImpl implements MultiAssetService {

  private static final long INITIAL_TX_COUNT = 0L;
  private static final String EMPTY_STRING = "";

  CachedMultiAssetRepository cachedMultiAssetRepository;
  CachedMaTxMintRepository cachedMaTxMintRepository;

  MaTxMintRepository maTxMintRepository;
  MultiAssetRepository multiAssetRepository;

  BlockDataService blockDataService;

  @Override
  public void handleMultiAssetMint(Collection<AggregatedTx> successTxs, Map<String, Tx> txMap) {
    List<AggregatedTx> txWithMintAssetsList = successTxs.stream()
        .filter(aggregatedTx -> !CollectionUtils.isEmpty(aggregatedTx.getMint()))
        .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(txWithMintAssetsList)) {
      return;
    }

    // Get all asset fingerprints
    Set<String> fingerprints = txWithMintAssetsList.stream()
        .flatMap(aggregatedTx -> aggregatedTx.getMint().stream())
        .map(amount -> AssetUtil.getFingerPrint(amount.getAssetName(), amount.getPolicyId()))
        .collect(Collectors.toSet());

    /*
     * Get all existing minted assets, creating a map with key is a pair of asset's name
     * and policy id, and key is the associated asset entity
     */
    Map<Pair<String, String>, MultiAsset> mintAssetsExists = cachedMultiAssetRepository
        .findMultiAssetsByFingerprintIn(fingerprints)
        .stream()
        .collect(Collectors.toMap(
            multiAsset -> Pair.of(multiAsset.getName(), multiAsset.getPolicy()),
            Function.identity()));

    List<MultiAsset> maNeedSave = new ArrayList<>();
    List<MaTxMint> maTxMints = new ArrayList<>();

    // Iterate between all aggregated txs with mint assets
    txWithMintAssetsList.forEach(txWithMintAssets -> {
      Tx tx = txMap.get(txWithMintAssets.getHash());
      List<Amount> mintAssets = txWithMintAssets.getMint();

      mintAssets.forEach(amount -> {
        String assetName = HexUtil.encodeHexString(amount.getAssetName());

        // Get asset entity from minted existing asset map. If not exists, create a new one
        var ma = getMultiAssetByPolicyAndNameFromList(tx, mintAssetsExists,
            amount.getPolicyId(), Objects.isNull(assetName) ? EMPTY_STRING : assetName);

        // Build a new asset mint entity
        MaTxMint maTxMint = MaTxMint.builder()
            .tx(tx)
            .ident(ma)
            .quantity(amount.getQuantity())
            .build();

        // If this asset is new, add it to existing mint assets map for future searches
        mintAssetsExists.put(Pair.of(ma.getName(), ma.getPolicy()), ma);
        maNeedSave.add(ma);
        maTxMints.add(maTxMint);
      });
    });

    cachedMultiAssetRepository.saveAll(maNeedSave);
    cachedMaTxMintRepository.saveAll(maTxMints);
  }

  private MultiAsset getMultiAssetByPolicyAndNameFromList(Tx tx,
      Map<Pair<String, String>, MultiAsset> multiAssetMap, String policy, String name) {
    MultiAsset multiAsset = multiAssetMap.get(Pair.of(name, policy));
    if (Objects.isNull(multiAsset)) {
      /*
       * This asset has not been minted before so mark its first appearance at current tx's
       * index and block number
       */
      String fingerPrint = AssetUtil.getFingerPrint(HexUtil.decodeHexString(name), policy);
      blockDataService.setFingerprintFirstAppearedBlockNoAndTxIdx(
          fingerPrint, tx.getBlock().getBlockNo(), tx.getBlockIndex());

      return MultiAsset.builder()
          .policy(policy)
          .name(name)
          .fingerprint(fingerPrint)
          .build();
    }

    return multiAsset;
  }

  @Override
  public MultiValueMap<String, MaTxOut> buildMaTxOut(AggregatedTxOut txOutput, TxOut txOut) {
    MultiValueMap<String, MaTxOut> maTxOutMap = new LinkedMultiValueMap<>();

    txOutput.getAmounts().stream()
        .filter(amount -> !Constant.isLoveLace(amount.getAssetName()))
        .forEach(amount -> {
          String fingerprint = AssetUtil
              .getFingerPrint(amount.getAssetName(), amount.getPolicyId());
          MaTxOut maTxOut = MaTxOut.builder()
              .txOut(txOut)
              .quantity(amount.getQuantity())
              .build();
          maTxOutMap.add(fingerprint, maTxOut);
        });

    return maTxOutMap;
  }

  @Override
  public Collection<MaTxOut> updateIdentMaTxOuts(MultiValueMap<String, MaTxOut> maTxOuts) {
    if (CollectionUtils.isEmpty(maTxOuts)) {
      return Collections.emptyList();
    }

    Set<String> fingerPrints = maTxOuts.keySet();
    Map<String, MultiAsset> fingerprintMaMap = cachedMultiAssetRepository
        .findMultiAssetsByFingerprintIn(fingerPrints)
        .parallelStream()
        .collect(Collectors.toConcurrentMap(MultiAsset::getFingerprint, Function.identity()));

    Queue<MaTxOut> result = new ConcurrentLinkedQueue<>();
    maTxOuts.entrySet().parallelStream().forEach(entry -> {
      String fingerprint = entry.getKey();
      Pair<Long, Long> firstAppearedBlockNoAndTxIdx = blockDataService
          .getFingerprintFirstAppearedBlockNoAndTxIdx(fingerprint);
      MultiAsset ident = fingerprintMaMap.get(fingerprint);
      List<MaTxOut> maTxOutList = entry.getValue();

      maTxOutList.parallelStream().forEach(maTxOut -> {
        Tx tx = maTxOut.getTxOut().getTx();
        /*
         * If the asset's first appeared block no and tx index is null, it had been minted in other
         * transaction not in current block batch
         *
         * If the first appeared block no and tx index is not null, check if the current block
         * number is the higher than this asset's first appeared block no, or block no equals
         * and tx index is higher than the asset's first appeared tx index. If both conditions
         * do not meet, skip this asset output
         */
        boolean assetHasBeenMintedBefore = Objects.nonNull(ident)
            && (Objects.isNull(firstAppearedBlockNoAndTxIdx)
            || firstAppearedBlockNoAndTxIdx.getFirst() < tx.getBlock().getBlockNo()
            || (firstAppearedBlockNoAndTxIdx.getFirst().equals(tx.getBlock().getBlockNo())
            && firstAppearedBlockNoAndTxIdx.getSecond() <= tx.getBlockIndex()));
        if (!assetHasBeenMintedBefore) {
          log.warn(
              "TxHash {}, Index {}, Finger print {} multi asset has not been minted before",
              tx.getHash(), maTxOut.getTxOut().getIndex(), fingerprint);
          //System.exit(1);//TODO dev check only
        } else {
          maTxOut.setIdent(ident);
          result.add(maTxOut);
        }
      });
    });

    return result;
  }
}
