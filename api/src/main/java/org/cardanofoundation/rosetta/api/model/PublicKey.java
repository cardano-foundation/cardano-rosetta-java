package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PublicKey contains a public key byte array for a particular CurveType encoded in hex. Note that there is no PrivateKey struct as this is NEVER the concern of an implementation.
 */

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PublicKey {

  @JsonProperty("hex_bytes")
  private String hexBytes;

  @JsonProperty("curve_type")
  private String curveType;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PublicKey {\n");
    sb.append("    hexBytes: ").append(toIndentedString(hexBytes)).append("\n");
    sb.append("    curveType: ").append(toIndentedString(curveType)).append("\n");
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

