package org.cardanofoundation.rosetta.crawler.model;

import co.nstant.in.cbor.model.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * An account may have state specific to a contract address (ERC-20 token) and/or a stake (delegated balance). The sub_account_identifier should specify which state (if applicable) an account instantiation refers to.
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubAccountIdentifier {

  @JsonProperty("address")
  private String address;

  @JsonProperty("metadata")
  private Map metadata;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubAccountIdentifier {\n");
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
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

