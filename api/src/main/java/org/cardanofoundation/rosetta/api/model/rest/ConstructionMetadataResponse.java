package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.api.model.Amount;

/**
 * The ConstructionMetadataResponse returns network-specific metadata used for transaction construction. Optionally, the implementer can return the suggested fee associated with the transaction being constructed. The caller may use this info to adjust the intent of the transaction or to create a transaction with a different account that can pay the suggested fee. Suggested fee is an array in case fee payment must occur in multiple currencies.
 */

@Schema(name = "ConstructionMetadataResponse", description = "The ConstructionMetadataResponse returns network-specific metadata used for transaction construction. Optionally, the implementer can return the suggested fee associated with the transaction being constructed. The caller may use this info to adjust the intent of the transaction or to create a transaction with a different account that can pay the suggested fee. Suggested fee is an array in case fee payment must occur in multiple currencies.")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ConstructionMetadataResponse {

  @JsonProperty("metadata")
  private ConstructionMetadataResponseMetadata metadata;

  @JsonProperty("suggested_fee")
  @Valid
  private List<Amount> suggestedFee = null;

  public ConstructionMetadataResponse metadata(ConstructionMetadataResponseMetadata metadata) {
    this.metadata = metadata;
    return this;
  }

  /**
   * Get metadata
   * @return metadata
  */
  @NotNull @Valid 
  @Schema(name = "metadata", requiredMode = Schema.RequiredMode.REQUIRED)
  public ConstructionMetadataResponseMetadata getMetadata() {
    return metadata;
  }

  public void setMetadata(ConstructionMetadataResponseMetadata metadata) {
    this.metadata = metadata;
  }

  public ConstructionMetadataResponse suggestedFee(List<Amount> suggestedFee) {
    this.suggestedFee = suggestedFee;
    return this;
  }

  public ConstructionMetadataResponse addSuggestedFeeItem(Amount suggestedFeeItem) {
    if (this.suggestedFee == null) {
      this.suggestedFee = new ArrayList<>();
    }
    this.suggestedFee.add(suggestedFeeItem);
    return this;
  }

  /**
   * Get suggestedFee
   * @return suggestedFee
  */
  @Valid 
  @Schema(name = "suggested_fee", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public List<Amount> getSuggestedFee() {
    return suggestedFee;
  }

  public void setSuggestedFee(List<Amount> suggestedFee) {
    this.suggestedFee = suggestedFee;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConstructionMetadataResponse constructionMetadataResponse = (ConstructionMetadataResponse) o;
    return Objects.equals(this.metadata, constructionMetadataResponse.metadata) &&
        Objects.equals(this.suggestedFee, constructionMetadataResponse.suggestedFee);
  }

  @Override
  public int hashCode() {
    return Objects.hash(metadata, suggestedFee);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConstructionMetadataResponse {\n");
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
    sb.append("    suggestedFee: ").append(toIndentedString(suggestedFee)).append("\n");
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

