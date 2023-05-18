package org.cardanofoundation.rosetta.crawler.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import javax.annotation.Generated;

/**
 * Coin contains its unique identifier and the amount it represents.
 */

public class Coin {

  @JsonProperty("coin_identifier")
  private CoinIdentifier coinIdentifier;

  @JsonProperty("amount")
  private Amount amount;

  @JsonProperty("metadata")
  @Valid
  private Map<String, List<TokenBundleItem>> metadata = null;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Coin {\n");
    sb.append("    coinIdentifier: ").append(toIndentedString(coinIdentifier)).append("\n");
    sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
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

