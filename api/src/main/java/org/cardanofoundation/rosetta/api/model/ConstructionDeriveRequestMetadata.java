package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.Valid;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * ConstructionDeriveRequestMetadata
 */

@JsonTypeName("ConstructionDeriveRequest_metadata")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
@AllArgsConstructor
@NoArgsConstructor
public class ConstructionDeriveRequestMetadata {

  @JsonProperty("staking_credential")
  private PublicKey stakingCredential;

  @JsonProperty("address_type")
  private String addressType;


  public ConstructionDeriveRequestMetadata stakingCredential(PublicKey stakingCredential) {
    this.stakingCredential = stakingCredential;
    return this;
  }

  /**
   * Get stakingCredential
   * @return stakingCredential
  */
  @Valid 
  @Schema(name = "staking_credential", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public PublicKey getStakingCredential() {
    return stakingCredential;
  }

  public void setStakingCredential(PublicKey stakingCredential) {
    this.stakingCredential = stakingCredential;
  }

  public ConstructionDeriveRequestMetadata addressType(String addressType) {
    this.addressType = addressType;
    return this;
  }

  /**
   * * Base address - associated to a payment and a staking credential, * Reward address - associated to a staking credential * Enterprise address - holds no delegation rights and will be created when no stake key is sent to the API
   * @return addressType
  */
  
  @Schema(name = "address_type", description = "* Base address - associated to a payment and a staking credential, * Reward address - associated to a staking credential * Enterprise address - holds no delegation rights and will be created when no stake key is sent to the API", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public String getAddressType() {
    return addressType;
  }

  public void setAddressType(String addressType) {
    this.addressType = addressType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConstructionDeriveRequestMetadata constructionDeriveRequestMetadata = (ConstructionDeriveRequestMetadata) o;
    return Objects.equals(this.stakingCredential, constructionDeriveRequestMetadata.stakingCredential) &&
        Objects.equals(this.addressType, constructionDeriveRequestMetadata.addressType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(stakingCredential, addressType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConstructionDeriveRequestMetadata {\n");
    sb.append("    stakingCredential: ").append(toIndentedString(stakingCredential)).append("\n");
    sb.append("    addressType: ").append(toIndentedString(addressType)).append("\n");
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

