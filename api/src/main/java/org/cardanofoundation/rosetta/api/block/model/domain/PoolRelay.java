package org.cardanofoundation.rosetta.api.block.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PoolRelay {

  private String ipv4;
  private String ipv6;
  private String dnsName;
  private String port;
  private String type;

}