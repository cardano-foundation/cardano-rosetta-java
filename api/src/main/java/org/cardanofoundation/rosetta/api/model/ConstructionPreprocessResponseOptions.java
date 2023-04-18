package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * The options that will be sent directly to &#x60;/construction/metadata&#x60; by the caller.
 */

@Schema(name = "ConstructionPreprocessResponse_options", description = "The options that will be sent directly to `/construction/metadata` by the caller.")
@JsonTypeName("ConstructionPreprocessResponse_options")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
@AllArgsConstructor
public class ConstructionPreprocessResponseOptions {

  @JsonProperty("relative_ttl")
  private BigDecimal relativeTtl;

  @JsonProperty("transaction_size")
  private BigDecimal transactionSize;

  public ConstructionPreprocessResponseOptions(Double relativeTtl, Double transactionSize) {

  }

  public ConstructionPreprocessResponseOptions relativeTtl(BigDecimal relativeTtl) {
    this.relativeTtl = relativeTtl;
    return this;
  }

  /**
   * Get relativeTtl
   * @return relativeTtl
  */
  @NotNull @Valid 
  @Schema(name = "relative_ttl", requiredMode = Schema.RequiredMode.REQUIRED)
  public BigDecimal getRelativeTtl() {
    return relativeTtl;
  }

  public void setRelativeTtl(BigDecimal relativeTtl) {
    this.relativeTtl = relativeTtl;
  }

  public ConstructionPreprocessResponseOptions transactionSize(BigDecimal transactionSize) {
    this.transactionSize = transactionSize;
    return this;
  }

  /**
   * Get transactionSize
   * @return transactionSize
  */
  @NotNull @Valid 
  @Schema(name = "transaction_size", requiredMode = Schema.RequiredMode.REQUIRED)
  public BigDecimal getTransactionSize() {
    return transactionSize;
  }

  public void setTransactionSize(BigDecimal transactionSize) {
    this.transactionSize = transactionSize;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConstructionPreprocessResponseOptions constructionPreprocessResponseOptions = (ConstructionPreprocessResponseOptions) o;
    return Objects.equals(this.relativeTtl, constructionPreprocessResponseOptions.relativeTtl) &&
        Objects.equals(this.transactionSize, constructionPreprocessResponseOptions.transactionSize);
  }

  @Override
  public int hashCode() {
    return Objects.hash(relativeTtl, transactionSize);
  }

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

