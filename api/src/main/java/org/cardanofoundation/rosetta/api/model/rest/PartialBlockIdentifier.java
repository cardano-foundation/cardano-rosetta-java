package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * When fetching data by BlockIdentifier, it may be possible to only specify the index or hash. If neither property is specified, it is assumed that the client is making a request at the current block.
 */

public class PartialBlockIdentifier {

  @JsonProperty("index")
  private Long index;

  @JsonProperty("hash")
  private String hash;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PartialBlockIdentifier {\n");
    sb.append("    index: ").append(toIndentedString(index)).append("\n");
    sb.append("    hash: ").append(toIndentedString(hash)).append("\n");
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

