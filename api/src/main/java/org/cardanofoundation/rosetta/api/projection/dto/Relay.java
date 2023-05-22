package org.cardanofoundation.rosetta.api.projection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Relay {

  private String type;
  private String ipv4;
  private String ipv6;
  private String dnsName;
  private String port;
}