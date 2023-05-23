package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ConstructionPayloadsRequestMetadata
 */

@JsonTypeName("ConstructionPayloadsRequest_metadata")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ConstructionPayloadsRequestMetadata {

  @JsonProperty("ttl")
  private String ttl;

  @JsonProperty("protocol_parameters")
  private ProtocolParameters protocolParameters;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConstructionPayloadsRequestMetadata {\n");
    sb.append("    ttl: ").append(toIndentedString(ttl)).append("\n");
    sb.append("    protocolParameters: ").append(toIndentedString(protocolParameters)).append("\n");
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

