package org.cardanofoundation.rosetta.consumer.aggregate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.cardanofoundation.rosetta.common.ledgersync.Amount;
import org.cardanofoundation.rosetta.common.ledgersync.Datum;
import org.cardanofoundation.rosetta.common.ledgersync.TransactionOutput;
import org.cardanofoundation.rosetta.common.ledgersync.byron.ByronTxOut;
import org.cardanofoundation.rosetta.common.ledgersync.constant.Constant;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AggregatedTxOut {
  Integer index;
  AggregatedAddress address;
  BigInteger nativeAmount;
  List<Amount> amounts;
  String datumHash;
  Datum inlineDatum;
  String scriptRef;

  public static AggregatedTxOut from(TransactionOutput transactionOutput) {
    if (Objects.isNull(transactionOutput)) {
      return null;
    }

    BigInteger nativeAmount = calculateOutSum(List.of(transactionOutput));

    return new AggregatedTxOut(
        transactionOutput.getIndex(),
        AggregatedAddress.from(transactionOutput.getAddress()),
        nativeAmount,
        transactionOutput.getAmounts(),
        transactionOutput.getDatumHash(),
        transactionOutput.getInlineDatum(),
        transactionOutput.getScriptRef()
    );
  }

  public static AggregatedTxOut from(ByronTxOut byronTxOut, int idx) {
    if (Objects.isNull(byronTxOut)) {
      return null;
    }

    return new AggregatedTxOut(
        idx,
        AggregatedAddress.from(byronTxOut.getAddress().getBase58Raw()),
        byronTxOut.getAmount(),
        Collections.emptyList(),
        null,
        null,
        null
    );
  }

  public static BigInteger calculateOutSum(List<TransactionOutput> txOuts) {
    var outSum = txOuts.stream()
        .flatMap(transactionOutput -> transactionOutput.getAmounts().stream())
        .filter(amount -> Constant.isLoveLace(amount.getAssetName()))
        .map(Amount::getQuantity)
        .reduce(BigInteger.ZERO, BigInteger::add);

    return outSum;
  }

  public void setIndex(int index) {
    this.index = index;
  }
}
