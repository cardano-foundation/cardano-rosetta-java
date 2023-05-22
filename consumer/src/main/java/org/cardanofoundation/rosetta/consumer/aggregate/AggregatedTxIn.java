package org.cardanofoundation.rosetta.consumer.aggregate;

import org.cardanofoundation.rosetta.common.ledgersync.TransactionInput;
import org.cardanofoundation.rosetta.common.ledgersync.byron.ByronTxIn;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class AggregatedTxIn {
  int index;
  String txId;

  public static AggregatedTxIn of(TransactionInput transactionInput) {
    if (Objects.isNull(transactionInput)) {
      return null;
    }

    return new AggregatedTxIn(transactionInput.getIndex(), transactionInput.getTransactionId());
  }

  public static AggregatedTxIn of(ByronTxIn byronTxIn) {
    if (Objects.isNull(byronTxIn)) {
      return null;
    }

    return new AggregatedTxIn(byronTxIn.getIndex(), byronTxIn.getTxId());
  }
}
