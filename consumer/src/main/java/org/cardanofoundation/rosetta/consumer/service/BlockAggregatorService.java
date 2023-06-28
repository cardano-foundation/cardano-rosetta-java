package org.cardanofoundation.rosetta.consumer.service;


import lombok.RequiredArgsConstructor;
import org.cardanofoundation.rosetta.common.ledgersync.constant.Constant;
import org.cardanofoundation.rosetta.common.ledgersync.kafka.CommonBlock;
import org.cardanofoundation.rosetta.common.util.AssetUtil;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedAddress;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedAddressBalance;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedBlock;
import org.cardanofoundation.rosetta.consumer.aggregate.AggregatedTxOut;

import java.math.BigInteger;
import java.util.List;

@RequiredArgsConstructor
public abstract class BlockAggregatorService<T extends CommonBlock> // NOSONAR
    implements SyncServiceInstance<T> {


  protected final SlotLeaderService slotLeaderService;
  protected final BlockDataService blockDataService;

  /**
   * Convert CDDL block data to aggregated block data
   *
   * @param block CDDL block data
   * @return aggregated block object
   */
  public abstract AggregatedBlock aggregateBlock(T block);

  /**
   * This method iterates between all aggregated tx out and map it to
   * aggregated address balance data
   *
   * @param aggregatedTxOuts all aggregated tx outs within a tx
   * @param txHash           tx hash of tx where the aggregated tx outs associate with
   */
  public void mapAggregatedTxOutsToAddressBalanceMap(
      List<AggregatedTxOut> aggregatedTxOuts, String txHash) {
    // Iterate between all aggregated tx out
    aggregatedTxOuts.forEach(aggregatedTxOut -> {
      // Get aggregated block address
      AggregatedAddress aggregatedAddress = aggregatedTxOut.getAddress();

      // Get address string (Base58 or Bech32) from aggregated address
      String address = aggregatedAddress.getAddress();

      // Get address's native amount
      BigInteger nativeAmount = aggregatedTxOut.getNativeAmount();

      // Get aggregated address balance data
      AggregatedAddressBalance aggregatedAddressBalance =
          blockDataService.getAggregatedAddressBalanceFromAddress(address);

      /*
       * Because the native amount in process is output, it is added to
       * existing balance record
       */
      aggregatedAddressBalance.addNativeBalance(txHash, nativeAmount);

      // Add multi-asset balances
      aggregatedTxOut.getAmounts().stream()
          .filter(amount -> !Constant.isLoveLace(amount.getAssetName()))
          .forEach(amount -> {
            byte[] assetName = amount.getAssetName();
            String policyId = amount.getPolicyId();
            String fingerprint = AssetUtil.getFingerPrint(assetName, policyId);
            BigInteger quantity = amount.getQuantity();
            aggregatedAddressBalance.addAssetBalance(txHash, fingerprint, quantity);
          });
    });
  }
}
