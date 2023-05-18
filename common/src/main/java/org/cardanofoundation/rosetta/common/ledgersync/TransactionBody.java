package org.cardanofoundation.rosetta.common.ledgersync;

import org.cardanofoundation.rosetta.common.ledgersync.certs.Certificate;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class TransactionBody {

  //Derived
  private byte[] txHash;

  private Set<TransactionInput> inputs;
  private List<TransactionOutput> outputs;
  private BigInteger fee;
  private BigInteger ttl;

  @Builder.Default
  private List<Certificate> certificates = new ArrayList<>();
  private Map<String, BigInteger> withdrawals;
  private Update update;
  private String auxiliaryDataHash;
  private long validityIntervalStart;

  @Builder.Default
  private List<Amount> mint = new ArrayList<>();
  private String scriptDataHash;

  private Set<TransactionInput> collateralInputs;
  private Set<String> requiredSigners;

  private int networkId;
  private TransactionOutput collateralReturn;
  private BigInteger totalCollateral;
  private Set<TransactionInput> referenceInputs;

  public void setTxHash(byte[] txHash) {
    this.txHash = txHash;
  }
}
