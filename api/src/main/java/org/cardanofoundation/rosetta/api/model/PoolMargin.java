package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * PoolMargin
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
public class PoolMargin {

  @JsonProperty("numerator")
  private String numerator;

  @JsonProperty("denominator")
  private String denominator;

  public PoolMargin numerator(String numerator) {
    this.numerator = numerator;
    return this;
  }

  /**
   * Get numerator
   * @return numerator
  */
  @NotNull 
  @Schema(name = "numerator", requiredMode = Schema.RequiredMode.REQUIRED)
  public String getNumerator() {
    return numerator;
  }

  public void setNumerator(String numerator) {
    this.numerator = numerator;
  }

  public PoolMargin denominator(String denominator) {
    this.denominator = denominator;
    return this;
  }

  /**
   * Get denominator
   * @return denominator
  */
  @NotNull 
  @Schema(name = "denominator", requiredMode = Schema.RequiredMode.REQUIRED)
  public String getDenominator() {
    return denominator;
  }

  public void setDenominator(String denominator) {
    this.denominator = denominator;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PoolMargin poolMargin = (PoolMargin) o;
    return Objects.equals(this.numerator, poolMargin.numerator) &&
        Objects.equals(this.denominator, poolMargin.denominator);
  }

  @Override
  public int hashCode() {
    return Objects.hash(numerator, denominator);
  }

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

