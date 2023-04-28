package org.cardanofoundation.rosetta.consumer.aggregate;

import com.sotatek.cardano.ledgersync.common.Amount;
import com.sotatek.cardano.ledgersync.common.Update;
import com.sotatek.cardano.ledgersync.common.Witnesses;
import com.sotatek.cardano.ledgersync.common.certs.Certificate;
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
  String hash;
  String blockHash;
  long blockIndex;
  BigInteger outSum;
  BigInteger fee;
  boolean validContract;
  long deposit;
  List<Certificate> certificates;
  Map<String, BigInteger> withdrawals;
  Update update;
  List<Amount> mint;
  Set<String> requiredSigners;
  Witnesses witnesses;
}
