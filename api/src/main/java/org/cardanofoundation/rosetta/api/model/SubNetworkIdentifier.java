package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * In blockchains with sharded state, the SubNetworkIdentifier is required to query some object on a specific shard. This identifier is optional for all non-sharded blockchains.
 */

public class SubNetworkIdentifier {

  @JsonProperty("network")
  private String network;

  @JsonProperty("metadata")
  private Object metadata;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubNetworkIdentifier {\n");
    sb.append("    network: ").append(toIndentedString(network)).append("\n");
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
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

