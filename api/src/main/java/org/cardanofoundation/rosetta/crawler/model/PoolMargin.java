package org.cardanofoundation.rosetta.crawler.model;

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
 * PoolMargin
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PoolMargin {

  @JsonProperty("numerator")
  private String numerator;

  @JsonProperty("denominator")
  private String denominator;


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PoolMargin {\n");
    sb.append("    numerator: ").append(toIndentedString(numerator)).append("\n");
    sb.append("    denominator: ").append(toIndentedString(denominator)).append("\n");
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

