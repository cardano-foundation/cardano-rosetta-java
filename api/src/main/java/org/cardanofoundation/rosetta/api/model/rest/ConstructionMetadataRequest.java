package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.cardanofoundation.rosetta.api.model.ConstructionMetadataRequestOptions;
import org.cardanofoundation.rosetta.api.model.PublicKey;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A ConstructionMetadataRequest is utilized to get information required to construct a transaction. The Options object used to specify which metadata to return is left purposely unstructured to allow flexibility for implementers. Options is not required in the case that there is network-wide metadata of interest. Optionally, the request can also include an array of PublicKeys associated with the AccountIdentifiers returned in ConstructionPreprocessResponse.
 */

@Schema(name = "ConstructionMetadataRequest", description = "A ConstructionMetadataRequest is utilized to get information required to construct a transaction. The Options object used to specify which metadata to return is left purposely unstructured to allow flexibility for implementers. Options is not required in the case that there is network-wide metadata of interest. Optionally, the request can also include an array of PublicKeys associated with the AccountIdentifiers returned in ConstructionPreprocessResponse.")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
public class ConstructionMetadataRequest {

  @JsonProperty("network_identifier")
  private NetworkIdentifier networkIdentifier;

  @JsonProperty("options")
  private ConstructionMetadataRequestOptions options;

  @JsonProperty("public_keys")
  @Valid
  private List<PublicKey> publicKeys = null;

  public ConstructionMetadataRequest networkIdentifier(NetworkIdentifier networkIdentifier) {
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

  public ConstructionMetadataRequest options(ConstructionMetadataRequestOptions options) {
    this.options = options;
    return this;
  }

  /**
   * Get options
   * @return options
  */
  @NotNull @Valid 
  @Schema(name = "options", requiredMode = Schema.RequiredMode.REQUIRED)
  public ConstructionMetadataRequestOptions getOptions() {
    return options;
  }

  public void setOptions(ConstructionMetadataRequestOptions options) {
    this.options = options;
  }

  public ConstructionMetadataRequest publicKeys(List<PublicKey> publicKeys) {
    this.publicKeys = publicKeys;
    return this;
  }

  public ConstructionMetadataRequest addPublicKeysItem(PublicKey publicKeysItem) {
    if (this.publicKeys == null) {
      this.publicKeys = new ArrayList<>();
    }
    this.publicKeys.add(publicKeysItem);
    return this;
  }

  /**
   * Get publicKeys
   * @return publicKeys
  */
  @Valid 
  @Schema(name = "public_keys", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public List<PublicKey> getPublicKeys() {
    return publicKeys;
  }

  public void setPublicKeys(List<PublicKey> publicKeys) {
    this.publicKeys = publicKeys;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConstructionMetadataRequest constructionMetadataRequest = (ConstructionMetadataRequest) o;
    return Objects.equals(this.networkIdentifier, constructionMetadataRequest.networkIdentifier) &&
        Objects.equals(this.options, constructionMetadataRequest.options) &&
        Objects.equals(this.publicKeys, constructionMetadataRequest.publicKeys);
  }

  @Override
  public int hashCode() {
    return Objects.hash(networkIdentifier, options, publicKeys);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConstructionMetadataRequest {\n");
    sb.append("    networkIdentifier: ").append(toIndentedString(networkIdentifier)).append("\n");
    sb.append("    options: ").append(toIndentedString(options)).append("\n");
    sb.append("    publicKeys: ").append(toIndentedString(publicKeys)).append("\n");
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

