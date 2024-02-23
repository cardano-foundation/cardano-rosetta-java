package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * The operation_identifier uniquely identifies an operation within a transaction.
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OperationIdentifier {

  @JsonProperty("index")
  private Long index;

  @JsonProperty("network_index")
  private Long networkIndex;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OperationIdentifier {\n");
    sb.append("    index: ").append(toIndentedString(index)).append("\n");
    sb.append("    networkIndex: ").append(toIndentedString(networkIndex)).append("\n");
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

