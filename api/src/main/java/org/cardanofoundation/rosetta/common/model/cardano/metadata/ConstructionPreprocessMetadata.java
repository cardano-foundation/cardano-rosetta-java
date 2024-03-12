package org.cardanofoundation.rosetta.common.model.cardano.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.common.model.cardano.pool.DepositParameters;

import javax.annotation.Generated;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Objects;

/**
 * ConstructionPreprocessRequestMetadata
 */

@JsonTypeName("ConstructionPreprocessRequest_metadata")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
@AllArgsConstructor
@NoArgsConstructor
public class ConstructionPreprocessMetadata {

  @JsonProperty("relative_ttl")
  private Double relativeTtl;

  @JsonProperty("deposit_parameters")
  private DepositParameters depositParameters;

  public static ConstructionPreprocessMetadata fromHashmap(HashMap<String, Object> metadata) {
    ConstructionPreprocessMetadata preprocessMetadata = new ConstructionPreprocessMetadata();
    if (metadata.containsKey("relative_ttl")) {
      if(metadata.get("relative_ttl") instanceof Double) {
        preprocessMetadata.setRelativeTtl((Double) metadata.get("relative_ttl"));
      } else if(metadata.get("relative_ttl") instanceof Integer) {
        preprocessMetadata.setRelativeTtl((Double.valueOf((Integer) metadata.get("relative_ttl"))));
      }
    }
    if (metadata.containsKey("deposit_parameters")) {
      HashMap<String, String> depositParameters = (HashMap<String, String>) metadata.get("deposit_parameters");
      DepositParameters depositParameters1 = new DepositParameters();
      if (depositParameters.containsKey("keyDeposit")) {
        depositParameters1.setKeyDeposit(depositParameters.get("keyDeposit"));
      }
      if (depositParameters.containsKey("poolDeposit")) {
        depositParameters1.setPoolDeposit(depositParameters.get("poolDeposit"));
      }
      preprocessMetadata.setDepositParameters(depositParameters1);
    }
    return preprocessMetadata;
  }


  public ConstructionPreprocessMetadata relativeTtl(Double relativeTtl) {
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

  public ConstructionPreprocessMetadata depositParameters(DepositParameters depositParameters) {
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
    ConstructionPreprocessMetadata constructionPreprocessRequestMetadata = (ConstructionPreprocessMetadata) o;
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

