package org.cardanofoundation.rosetta.api.model;

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

//  @Override
//  public String toString() {
//    StringBuilder sb = new StringBuilder();
//    sb.append("class Relay {\n");
//    sb.append("    type: ").append(toIndentedString(type)).append("\n");
//    sb.append("    ipv4: ").append(toIndentedString(ipv4)).append("\n");
//    sb.append("    ipv6: ").append(toIndentedString(ipv6)).append("\n");
//    sb.append("    dnsName: ").append(toIndentedString(dnsName)).append("\n");
//    sb.append("    port: ").append(toIndentedString(port)).append("\n");
//    sb.append("}");
//    return sb.toString();
//  }
//
//  /**
//   * Convert the given object to string with each line indented by 4 spaces
//   * (except the first line).
//   */
//  private String toIndentedString(Object o) {
//    if (o == null) {
//      return "null";
//    }
//    return o.toString().replace("\n", "\n    ");
//  }
}

