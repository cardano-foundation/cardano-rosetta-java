package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.annotation.Generated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * CoinIdentifier uniquely identifies a Coin.
 */

@Schema(name = "CoinIdentifier", description = "CoinIdentifier uniquely identifies a Coin.")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CoinIdentifier {

  @JsonProperty("identifier")
  private String identifier;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CoinIdentifier {\n");
    sb.append("    identifier: ").append(toIndentedString(identifier)).append("\n");
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

