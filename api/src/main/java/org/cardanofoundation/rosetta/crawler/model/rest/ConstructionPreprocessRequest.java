package org.cardanofoundation.rosetta.crawler.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.cardanofoundation.rosetta.crawler.model.Amount;
import org.cardanofoundation.rosetta.crawler.model.ConstructionPreprocessRequestMetadata;
import org.cardanofoundation.rosetta.crawler.model.Operation;


import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ConstructionPreprocessRequest is passed to the &#x60;/construction/preprocess&#x60; endpoint so that a Rosetta implementation can determine which metadata it needs to request for construction. Metadata provided in this object should NEVER be a product of live data (i.e. the caller must follow some network-specific data fetching strategy outside of the Construction API to populate required Metadata). If live data is required for construction, it MUST be fetched in the call to &#x60;/construction/metadata&#x60;. The caller can provide a max fee they are willing to pay for a transaction. This is an array in the case fees must be paid in multiple currencies. The caller can also provide a suggested fee multiplier to indicate that the suggested fee should be scaled. This may be used to set higher fees for urgent transactions or to pay lower fees when there is less urgency. It is assumed that providing a very low multiplier (like 0.0001) will never lead to a transaction being created with a fee less than the minimum network fee (if applicable). In the case that the caller provides both a max fee and a suggested fee multiplier, the max fee will set an upper bound on the suggested fee (regardless of the multiplier provided).
 */

@Schema(name = "ConstructionPreprocessRequest", description = "ConstructionPreprocessRequest is passed to the `/construction/preprocess` endpoint so that a Rosetta implementation can determine which metadata it needs to request for construction. Metadata provided in this object should NEVER be a product of live data (i.e. the caller must follow some network-specific data fetching strategy outside of the Construction API to populate required Metadata). If live data is required for construction, it MUST be fetched in the call to `/construction/metadata`. The caller can provide a max fee they are willing to pay for a transaction. This is an array in the case fees must be paid in multiple currencies. The caller can also provide a suggested fee multiplier to indicate that the suggested fee should be scaled. This may be used to set higher fees for urgent transactions or to pay lower fees when there is less urgency. It is assumed that providing a very low multiplier (like 0.0001) will never lead to a transaction being created with a fee less than the minimum network fee (if applicable). In the case that the caller provides both a max fee and a suggested fee multiplier, the max fee will set an upper bound on the suggested fee (regardless of the multiplier provided).")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
public class ConstructionPreprocessRequest {

  @JsonProperty("network_identifier")
  private NetworkIdentifier networkIdentifier;

  @JsonProperty("operations")
  @Valid
  private List<Operation> operations = new ArrayList<>();

  @JsonProperty("metadata")
  private ConstructionPreprocessRequestMetadata metadata;

  @JsonProperty("max_fee")
  @Valid
  private List<Amount> maxFee = null;

  @JsonProperty("suggested_fee_multiplier")
  private Double suggestedFeeMultiplier;

  public ConstructionPreprocessRequest networkIdentifier(NetworkIdentifier networkIdentifier) {
    this.networkIdentifier = networkIdentifier;
    return this;
  }

  /**
   * Get networkIdentifier
   * @return networkIdentifier
  */
  @NotNull @Valid 
  @Schema(name = "network_identifier", requiredMode = Schema.RequiredMode.REQUIRED)
  public NetworkIdentifier getNetworkIdentifier() {
    return networkIdentifier;
  }

  public void setNetworkIdentifier(NetworkIdentifier networkIdentifier) {
    this.networkIdentifier = networkIdentifier;
  }

  public ConstructionPreprocessRequest operations(List<Operation> operations) {
    this.operations = operations;
    return this;
  }

  public ConstructionPreprocessRequest addOperationsItem(Operation operationsItem) {
    this.operations.add(operationsItem);
    return this;
  }

  /**
   * Get operations
   * @return operations
  */
  @NotNull @Valid 
  @Schema(name = "operations", requiredMode = Schema.RequiredMode.REQUIRED)
  public List<Operation> getOperations() {
    return operations;
  }

  public void setOperations(List<Operation> operations) {
    this.operations = operations;
  }

  public ConstructionPreprocessRequest metadata(ConstructionPreprocessRequestMetadata metadata) {
    this.metadata = metadata;
    return this;
  }

  /**
   * Get metadata
   * @return metadata
  */
  @Valid 
  @Schema(name = "metadata", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public ConstructionPreprocessRequestMetadata getMetadata() {
    return metadata;
  }

  public void setMetadata(ConstructionPreprocessRequestMetadata metadata) {
    this.metadata = metadata;
  }

  public ConstructionPreprocessRequest maxFee(List<Amount> maxFee) {
    this.maxFee = maxFee;
    return this;
  }

  public ConstructionPreprocessRequest addMaxFeeItem(Amount maxFeeItem) {
    if (this.maxFee == null) {
      this.maxFee = new ArrayList<>();
    }
    this.maxFee.add(maxFeeItem);
    return this;
  }

  /**
   * Get maxFee
   * @return maxFee
  */
  @Valid 
  @Schema(name = "max_fee", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public List<Amount> getMaxFee() {
    return maxFee;
  }

  public void setMaxFee(List<Amount> maxFee) {
    this.maxFee = maxFee;
  }

  public ConstructionPreprocessRequest suggestedFeeMultiplier(Double suggestedFeeMultiplier) {
    this.suggestedFeeMultiplier = suggestedFeeMultiplier;
    return this;
  }

  /**
   * Get suggestedFeeMultiplier
   * minimum: 0
   * @return suggestedFeeMultiplier
  */
  @DecimalMin("0") 
  @Schema(name = "suggested_fee_multiplier", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public Double getSuggestedFeeMultiplier() {
    return suggestedFeeMultiplier;
  }

  public void setSuggestedFeeMultiplier(Double suggestedFeeMultiplier) {
    this.suggestedFeeMultiplier = suggestedFeeMultiplier;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConstructionPreprocessRequest constructionPreprocessRequest = (ConstructionPreprocessRequest) o;
    return Objects.equals(this.networkIdentifier, constructionPreprocessRequest.networkIdentifier) &&
        Objects.equals(this.operations, constructionPreprocessRequest.operations) &&
        Objects.equals(this.metadata, constructionPreprocessRequest.metadata) &&
        Objects.equals(this.maxFee, constructionPreprocessRequest.maxFee) &&
        Objects.equals(this.suggestedFeeMultiplier, constructionPreprocessRequest.suggestedFeeMultiplier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(networkIdentifier, operations, metadata, maxFee, suggestedFeeMultiplier);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConstructionPreprocessRequest {\n");
    sb.append("    networkIdentifier: ").append(toIndentedString(networkIdentifier)).append("\n");
    sb.append("    operations: ").append(toIndentedString(operations)).append("\n");
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
    sb.append("    maxFee: ").append(toIndentedString(maxFee)).append("\n");
    sb.append("    suggestedFeeMultiplier: ").append(toIndentedString(suggestedFeeMultiplier)).append("\n");
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

