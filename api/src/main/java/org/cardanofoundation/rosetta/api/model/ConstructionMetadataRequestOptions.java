package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Some blockchains require different metadata for different types of transaction construction (ex: delegation versus a transfer). Instead of requiring a blockchain node to return all possible types of metadata for construction (which may require multiple node fetches), the client can populate an options object to limit the metadata returned to only the subset required.
 */

@Schema(name = "ConstructionMetadataRequest_options", description = "Some blockchains require different metadata for different types of transaction construction (ex: delegation versus a transfer). Instead of requiring a blockchain node to return all possible types of metadata for construction (which may require multiple node fetches), the client can populate an options object to limit the metadata returned to only the subset required.")
@JsonTypeName("ConstructionMetadataRequest_options")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
public class ConstructionMetadataRequestOptions {

  @JsonProperty("relative_ttl")
  private BigDecimal relativeTtl;

  @JsonProperty("transaction_size")
  private BigDecimal transactionSize;

  public ConstructionMetadataRequestOptions relativeTtl(BigDecimal relativeTtl) {
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

  public ConstructionMetadataRequestOptions transactionSize(BigDecimal transactionSize) {
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
    ConstructionMetadataRequestOptions constructionMetadataRequestOptions = (ConstructionMetadataRequestOptions) o;
    return Objects.equals(this.relativeTtl, constructionMetadataRequestOptions.relativeTtl) &&
        Objects.equals(this.transactionSize, constructionMetadataRequestOptions.transactionSize);
  }

  @Override
  public int hashCode() {
    return Objects.hash(relativeTtl, transactionSize);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConstructionMetadataRequestOptions {\n");
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

