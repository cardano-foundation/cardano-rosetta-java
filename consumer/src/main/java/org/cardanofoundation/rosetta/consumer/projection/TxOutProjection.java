package org.cardanofoundation.rosetta.consumer.projection;

import java.math.BigInteger;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TxOutProjection {
  Long id;
  String txHash;
  Long txId;
  Short index;
  BigInteger value;
  Long stakeAddressId;
  String address;
  Boolean addressHasScript;
  String paymentCred;
}
