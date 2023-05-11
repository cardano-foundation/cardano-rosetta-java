package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Some blockchains require different metadata for different types of transaction construction (ex: delegation versus a transfer). Instead of requiring a blockchain node to return all possible types of metadata for construction (which may require multiple node fetches), the client can populate an options object to limit the metadata returned to only the subset required.
 */



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConstructionMetadataRequestOptions {

  @JsonProperty("relative_ttl")
  private Double relativeTtl;

  @JsonProperty("transaction_size")
  private Double transactionSize;

  public ConstructionMetadataRequestOptions relativeTtl(Double relativeTtl) {
    this.relativeTtl = relativeTtl;
    return this;
  }

  public void setTransactionSize(Double transactionSize) {
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

