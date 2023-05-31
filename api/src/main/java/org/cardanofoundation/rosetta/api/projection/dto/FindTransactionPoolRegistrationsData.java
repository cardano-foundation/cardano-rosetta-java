package org.cardanofoundation.rosetta.api.projection.dto;


import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindTransactionPoolRegistrationsData implements FindTransactionFieldResult {
  private String txHash;
  protected Long txId;
  private Long updateId;
  private String vrfKeyHash;
  private BigInteger pledge;
  private Double margin;
  private BigInteger cost;
  private String address;
  private String poolHash;
  private String metadataUrl;
  private String metadataHash;
}
