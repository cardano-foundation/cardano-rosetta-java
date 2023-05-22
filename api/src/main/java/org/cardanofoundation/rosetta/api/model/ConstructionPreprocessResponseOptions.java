package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The options that will be sent directly to &#x60;/construction/metadata&#x60; by the caller.
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ConstructionPreprocessResponseOptions {

  @JsonProperty("relative_ttl")
  private Double relativeTtl;

  @JsonProperty("transaction_size")
  private Double transactionSize;

  public ConstructionPreprocessResponseOptions relativeTtl(Double relativeTtl) {
    this.relativeTtl = relativeTtl;
    return this;
  }

  /**
   * Get relativeTtl
   * @return relativeTtl
  */

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConstructionPreprocessResponseOptions {\n");
    sb.append("    relativeTtl: ").append(toIndentedString(relativeTtl)).append("\n");
    sb.append("    transactionSize: ").append(toIndentedString(transactionSize)).append("\n");
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

