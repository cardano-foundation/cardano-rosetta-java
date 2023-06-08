package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
import javax.annotation.Generated;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * ConstructionPreprocessRequestMetadata
 */

@JsonTypeName("ConstructionPreprocessRequest_metadata")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
@AllArgsConstructor
@NoArgsConstructor
public class ConstructionPreprocessRequestMetadata {

  @JsonProperty("relative_ttl")
  private Double relativeTtl;

  @JsonProperty("deposit_parameters")
  private DepositParameters depositParameters;



  public ConstructionPreprocessRequestMetadata relativeTtl(Double relativeTtl) {
    this.relativeTtl = relativeTtl;
    return this;
  }

  /**
   * Get relativeTtl
   * @return relativeTtl
  */
  @Valid 
  @Schema(name = "relative_ttl", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public Double getRelativeTtl() {
    return relativeTtl;
  }

  public void setRelativeTtl(Double relativeTtl) {
    this.relativeTtl = relativeTtl;
  }

  public ConstructionPreprocessRequestMetadata depositParameters(DepositParameters depositParameters) {
    this.depositParameters = depositParameters;
    return this;
  }

  /**
   * Get depositParameters
   * @return depositParameters
  */
  @Valid 
  @Schema(name = "deposit_parameters", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public DepositParameters getDepositParameters() {
    return depositParameters;
  }

  public void setDepositParameters(DepositParameters depositParameters) {
    this.depositParameters = depositParameters;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConstructionPreprocessRequestMetadata constructionPreprocessRequestMetadata = (ConstructionPreprocessRequestMetadata) o;
    return Objects.equals(this.relativeTtl, constructionPreprocessRequestMetadata.relativeTtl) &&
        Objects.equals(this.depositParameters, constructionPreprocessRequestMetadata.depositParameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(relativeTtl, depositParameters);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConstructionPreprocessRequestMetadata {\n");
    sb.append("    relativeTtl: ").append(toIndentedString(relativeTtl)).append("\n");
    sb.append("    depositParameters: ").append(toIndentedString(depositParameters)).append("\n");
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

