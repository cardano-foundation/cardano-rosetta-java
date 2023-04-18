package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import java.util.Objects;

/**
 * Relay
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
public class Relay1 {

  @JsonProperty("type")
  private String type;

  @JsonProperty("ipv4")
  private String ipv4;

  @JsonProperty("ipv6")
  private String ipv6;

  @JsonProperty("dnsName")
  private String dnsName;

  @JsonProperty("port")
  private String port;

  public Relay1 type(String type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   * @return type
  */
  
  @Schema(name = "type", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Relay1 ipv4(String ipv4) {
    this.ipv4 = ipv4;
    return this;
  }

  /**
   * Get ipv4
   * @return ipv4
  */
  
  @Schema(name = "ipv4", example = "127.0.0.1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public String getIpv4() {
    return ipv4;
  }

  public void setIpv4(String ipv4) {
    this.ipv4 = ipv4;
  }

  public Relay1 ipv6(String ipv6) {
    this.ipv6 = ipv6;
    return this;
  }

  /**
   * Get ipv6
   * @return ipv6
  */
  
  @Schema(name = "ipv6", example = "2345:0425:2ca1:0000:0000:0567:5673:23b5", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public String getIpv6() {
    return ipv6;
  }

  public void setIpv6(String ipv6) {
    this.ipv6 = ipv6;
  }

  public Relay1 dnsName(String dnsName) {
    this.dnsName = dnsName;
    return this;
  }

  /**
   * Get dnsName
   * @return dnsName
  */
  
  @Schema(name = "dnsName", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public String getDnsName() {
    return dnsName;
  }

  public void setDnsName(String dnsName) {
    this.dnsName = dnsName;
  }

  public Relay1 port(String port) {
    this.port = port;
    return this;
  }

  /**
   * Get port
   * @return port
  */
  
  @Schema(name = "port", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Relay1 relay = (Relay1) o;
    return Objects.equals(this.type, relay.type) &&
        Objects.equals(this.ipv4, relay.ipv4) &&
        Objects.equals(this.ipv6, relay.ipv6) &&
        Objects.equals(this.dnsName, relay.dnsName) &&
        Objects.equals(this.port, relay.port);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, ipv4, ipv6, dnsName, port);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Relay {\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    ipv4: ").append(toIndentedString(ipv4)).append("\n");
    sb.append("    ipv6: ").append(toIndentedString(ipv6)).append("\n");
    sb.append("    dnsName: ").append(toIndentedString(dnsName)).append("\n");
    sb.append("    port: ").append(toIndentedString(port)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

