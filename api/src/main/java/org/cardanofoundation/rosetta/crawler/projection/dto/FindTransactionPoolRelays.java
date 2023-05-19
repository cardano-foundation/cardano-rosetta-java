package org.cardanofoundation.rosetta.crawler.projection.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindTransactionPoolRelays implements FindTransactionFieldResult {

  private Long updateId;
  private String ipv4;
  private String ipv6;
  private Integer port;
  private String dnsName;
  private byte[] txHash;
  private byte[] vrfKeyHash;

  public FindTransactionPoolRelays(Long updateId, String ipv4, String ipv6, Integer port,
      String dnsName, byte[] txHash) {
    this.updateId = updateId;
    this.ipv4 = ipv4;
    this.ipv6 = ipv6;
    this.port = port;
    this.dnsName = dnsName;
    this.txHash = txHash;
  }
}
