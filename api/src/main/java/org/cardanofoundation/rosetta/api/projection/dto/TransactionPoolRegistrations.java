package org.cardanofoundation.rosetta.api.projection.dto;

import java.math.BigInteger;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionPoolRegistrations implements FindTransactionFieldResult {

  private String txHash;
  private String vrfKeyHash;
  private String pledge;
  private Double margin;
  private BigInteger cost;
  private String address;
  private String poolHash;
  private List<String> owners;
  private List<PoolRelay> relays;
  private String metadataUrl;
  private String metadataHash;
}