package org.cardanofoundation.rosetta.consumer.aggregate;

import org.cardanofoundation.rosetta.common.ledgersync.Amount;
import org.cardanofoundation.rosetta.common.ledgersync.Update;
import org.cardanofoundation.rosetta.common.ledgersync.Witnesses;
import org.cardanofoundation.rosetta.common.ledgersync.certs.Certificate;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AggregatedTx {
  byte[] hash;
  byte[] blockHash;
  long blockIndex;
  BigInteger outSum;
  BigInteger fee;
  boolean validContract;
  long deposit;
  Set<AggregatedTxIn> txInputs;
  Set<AggregatedTxIn> collateralInputs;
  Set<AggregatedTxIn> referenceInputs;
  List<AggregatedTxOut> txOutputs;
  AggregatedTxOut collateralReturn;
  List<Certificate> certificates;
  Map<String, BigInteger> withdrawals;
  Update update;
  List<Amount> mint;
  Set<String> requiredSigners;
  Witnesses witnesses;
}
