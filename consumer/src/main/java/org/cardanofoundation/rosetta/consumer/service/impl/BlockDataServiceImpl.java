package org.cardanofoundation.rosetta.consumer.service.impl;

import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedAddressBalance;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBatchBlockData;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTx;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedBlockRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedDatumRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedDelegationRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedEpochParamRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedEpochRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedExtraKeyWitnessRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedMaTxMintRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedMultiAssetRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedMultiAssetTxOutRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedParamProposalRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedPoolHashRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedPoolMetadataRefRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedPoolOwnerRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedPoolRelayRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedPoolRetireRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedPoolUpdateRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedPotTransferRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedRedeemerDataRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedRedeemerRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedReferenceInputRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedReserveRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedScriptRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedSlotLeaderRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedStakeAddressRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedStakeDeregistrationRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedStakeRegistrationRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedTreasuryRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedTxInRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedTxMetadataRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedTxOutRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedTxRepository;
import org.cardanofoundation.rosetta.consumer.repository.cached.CachedWithdrawalRepository;
import org.cardanofoundation.rosetta.consumer.service.BlockDataService;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BlockDataServiceImpl implements BlockDataService {

  AggregatedBatchBlockData aggregatedBatchBlockData;

  CachedSlotLeaderRepository cachedSlotLeaderRepository;
  CachedEpochRepository cachedEpochRepository;
  CachedBlockRepository cachedBlockRepository;
  CachedTxRepository cachedTxRepository;
  CachedScriptRepository cachedScriptRepository;
  CachedDatumRepository cachedDatumRepository;
  CachedStakeAddressRepository cachedStakeAddressRepository;
  CachedExtraKeyWitnessRepository cachedExtraKeyWitnessRepository;
  CachedMultiAssetRepository cachedMultiAssetRepository;
  CachedMaTxMintRepository cachedMaTxMintRepository;
  CachedTxOutRepository cachedTxOutRepository;
  CachedMultiAssetTxOutRepository cachedMultiAssetTxOutRepository;
  CachedRedeemerDataRepository cachedRedeemerDataRepository;
  CachedRedeemerRepository cachedRedeemerRepository;
  CachedTxInRepository cachedTxInRepository;
  CachedTxMetadataRepository cachedTxMetadataRepository;
  CachedParamProposalRepository cachedParamProposalRepository;
  CachedReferenceInputRepository cachedReferenceInputRepository;
  CachedPoolHashRepository cachedPoolHashRepository;
  CachedPoolMetadataRefRepository cachedPoolMetadataRefRepository;
  CachedPoolUpdateRepository cachedPoolUpdateRepository;
  CachedPoolOwnerRepository cachedPoolOwnerRepository;
  CachedPoolRelayRepository cachedPoolRelayRepository;
  CachedReserveRepository cachedReserveRepository;
  CachedTreasuryRepository cachedTreasuryRepository;
  CachedPotTransferRepository cachedPotTransferRepository;
  CachedPoolRetireRepository cachedPoolRetireRepository;
  CachedDelegationRepository cachedDelegationRepository;
  CachedStakeDeregistrationRepository cachedStakeDeregistrationRepository;
  CachedStakeRegistrationRepository cachedStakeRegistrationRepository;
  CachedWithdrawalRepository cachedWithdrawalRepository;
  CachedEpochParamRepository cachedEpochParamRepository;

  @Override
  public Map<String, byte[]> getStakeAddressTxHashMap() {
    return aggregatedBatchBlockData.getStakeAddressTxHashMap();
  }

  @Override
  public void saveFirstAppearedTxHashForStakeAddress(String stakeAddress, byte[] txHash) {
    aggregatedBatchBlockData.getStakeAddressTxHashMap().putIfAbsent(stakeAddress, txHash);
  }

  @Override
  public AggregatedAddressBalance getAggregatedAddressBalanceFromAddress(String address) {
    return aggregatedBatchBlockData
        .getAggregatedAddressBalanceMap()
        .computeIfAbsent(address, AggregatedAddressBalance::from);
  }

  @Override
  public Map<String, AggregatedAddressBalance> getAggregatedAddressBalanceMap() {
    return aggregatedBatchBlockData.getAggregatedAddressBalanceMap();
  }

  @Override
  public Pair<Long, Long> getFingerprintFirstAppearedBlockNoAndTxIdx(String fingerprint) {
    return aggregatedBatchBlockData.getFingerprintFirstAppearedMap().get(fingerprint);
  }

  @Override
  public void setFingerprintFirstAppearedBlockNoAndTxIdx(
      String fingerprint, Long blockNo, Long txIdx) {
    Pair<Long, Long> firstAppearedBlockNoAndTxIdx = Pair.of(blockNo, txIdx);
    aggregatedBatchBlockData.getFingerprintFirstAppearedMap()
        .putIfAbsent(fingerprint, firstAppearedBlockNoAndTxIdx);
  }

  @Override
  public AggregatedBlock getAggregatedBlock(byte[] blockHash) {
    return aggregatedBatchBlockData.getAggregatedBlockMap().get(blockHash);
  }

  @Override
  public void saveAggregatedBlock(AggregatedBlock aggregatedBlock) {
    aggregatedBatchBlockData.getAggregatedBlockMap()
        .put(aggregatedBlock.getHash(), aggregatedBlock);
  }

  @Override
  public void forEachAggregatedBlock(Consumer<AggregatedBlock> consumer) {
    aggregatedBatchBlockData.getAggregatedBlockMap().values().forEach(consumer);
  }

  @Override
  public Collection<AggregatedTx> getSuccessTxs() {
    return aggregatedBatchBlockData.getSuccessTxs();
  }

  @Override
  public void saveSuccessTx(AggregatedTx successTx) {
    aggregatedBatchBlockData.getSuccessTxs().add(successTx);
  }

  @Override
  public Collection<AggregatedTx> getFailedTxs() {
    return aggregatedBatchBlockData.getFailedTxs();
  }

  @Override
  public void saveFailedTx(AggregatedTx failedTx) {
    aggregatedBatchBlockData.getFailedTxs().add(failedTx);
  }

  /**
   * Use for log only because method will return new aggregated block if block map is empty *
   *
   * @return
   */
  public Pair<AggregatedBlock, AggregatedBlock> getFirstAndLastBlock() {
    AggregatedBlock first;
    AggregatedBlock last = new AggregatedBlock();
    var mBlock = aggregatedBatchBlockData.getAggregatedBlockMap();
    if (!mBlock.entrySet().iterator().hasNext()) {
      return Pair.of(new AggregatedBlock(), new AggregatedBlock());
    }
    first = mBlock.entrySet().iterator().next().getValue();
    for (Map.Entry<byte[], AggregatedBlock> entry : mBlock.entrySet()) {
      last = entry.getValue();
    }
    return Pair.of(first, last);
  }

  public int getBlockSize() {
    return aggregatedBatchBlockData.getAggregatedBlockMap().size();
  }

  @Transactional
  @Override
  public void saveAll() {
    long startTime = System.currentTimeMillis();
    aggregatedBatchBlockData.clear();
    cachedSlotLeaderRepository.flushToDb();
    cachedEpochRepository.flushToDb();
    cachedBlockRepository.flushToDb();
    cachedTxRepository.flushToDb();
    cachedScriptRepository.flushToDb();
    cachedDatumRepository.flushToDb();
    cachedStakeAddressRepository.flushToDb();
    cachedExtraKeyWitnessRepository.flushToDb();
    cachedMultiAssetRepository.flushToDb();
    cachedMaTxMintRepository.flushToDb();
    cachedTxOutRepository.flushToDb();
    cachedMultiAssetTxOutRepository.flushToDb();
    cachedRedeemerDataRepository.flushToDb();
    cachedRedeemerRepository.flushToDb();
    cachedTxInRepository.flushToDb();
    cachedTxMetadataRepository.flushToDb();
    cachedParamProposalRepository.flushToDb();
    cachedReferenceInputRepository.flushToDb();
    cachedPoolHashRepository.flushToDb();
    cachedPoolMetadataRefRepository.flushToDb();
    cachedPoolUpdateRepository.flushToDb();
    cachedPoolOwnerRepository.flushToDb();
    cachedPoolRelayRepository.flushToDb();
    cachedReserveRepository.flushToDb();
    cachedTreasuryRepository.flushToDb();
    cachedPotTransferRepository.flushToDb();
    cachedPoolRetireRepository.flushToDb();
    cachedDelegationRepository.flushToDb();
    cachedStakeDeregistrationRepository.flushToDb();
    cachedStakeRegistrationRepository.flushToDb();
    cachedWithdrawalRepository.flushToDb();
    cachedEpochParamRepository.flushToDb();
    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;
    log.debug("Insertion time elapsed: {} ms, {} second(s)", totalTime, totalTime / 1000f);
  }
}
