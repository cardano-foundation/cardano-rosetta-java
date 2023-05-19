package org.cardanofoundation.rosetta.crawler.projection.dto;


import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindTransactionPoolRegistrationsData implements FindTransactionFieldResult {
  private byte[] txHash;
  protected Long txId;
  private Long updateId;
  private byte[] vrfKeyHash;
  private BigInteger pledge;
  private Double margin;
  private BigInteger cost;
  private String address;
  private byte[] poolHash;
  private String metadataUrl;
  private byte[] metadataHash;
}
