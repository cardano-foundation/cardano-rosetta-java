package org.cardanofoundation.rosetta.common.model.cardano;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Relay
 */

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Relay {

  @JsonProperty("type")
  private String type;

  @JsonProperty("ipv4")
  private String ipv4;

  @JsonProperty("ipv6")
  private String ipv6;

  @JsonProperty("dnsName")
  private String dnsName;

  public Relay(String type, String dnsName) {
    this.type = type;
    this.dnsName = dnsName;
  }

  @JsonProperty("port")
  private String port;


  public Relay(String type, String dnsName, String port) {
    this.type = type;
    this.dnsName = dnsName;
    this.port = port;
  }

}

