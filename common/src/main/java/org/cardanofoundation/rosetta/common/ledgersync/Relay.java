package org.cardanofoundation.rosetta.common.ledgersync;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class Relay {

  private Integer port;
  private String ipv4;
  private String ipv6;
  private String dnsName;

  //TODO - Should we also add type single host addr, single dns, multi dns ???
}
