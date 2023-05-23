package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.api.model.ProtocolParameters;

import javax.annotation.Generated;
import javax.validation.Valid;
import java.util.Objects;

/**
 * ConstructionMetadataResponseMetadata
 */

@JsonTypeName("ConstructionMetadataResponse_metadata")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
@AllArgsConstructor
@NoArgsConstructor
public class ConstructionMetadataResponseMetadata {

  @JsonProperty("ttl")
  private String ttl;

  @JsonProperty("protocol_parameters")
  private ProtocolParameters protocolParameters;


  public ConstructionMetadataResponseMetadata ttl(String ttl) {
    this.ttl = ttl;
    return this;
  }

  /**
   * Get ttl
   * @return ttl
  */
  
  @Schema(name = "ttl", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public String getTtl() {
    return ttl;
  }

  public void setTtl(String ttl) {
    this.ttl = ttl;
  }

  public ConstructionMetadataResponseMetadata protocolParameters(ProtocolParameters protocolParameters) {
    this.protocolParameters = protocolParameters;
    return this;
  }

  /**
   * Get protocolParameters
   * @return protocolParameters
  */
  @Valid 
  @Schema(name = "protocol_parameters", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public ProtocolParameters getProtocolParameters() {
    return protocolParameters;
  }

  public void setProtocolParameters(ProtocolParameters protocolParameters) {
    this.protocolParameters = protocolParameters;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConstructionMetadataResponseMetadata constructionMetadataResponseMetadata = (ConstructionMetadataResponseMetadata) o;
    return Objects.equals(this.ttl, constructionMetadataResponseMetadata.ttl) &&
        Objects.equals(this.protocolParameters, constructionMetadataResponseMetadata.protocolParameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ttl, protocolParameters);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConstructionMetadataResponseMetadata {\n");
    sb.append("    ttl: ").append(toIndentedString(ttl)).append("\n");
    sb.append("    protocolParameters: ").append(toIndentedString(protocolParameters)).append("\n");
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

