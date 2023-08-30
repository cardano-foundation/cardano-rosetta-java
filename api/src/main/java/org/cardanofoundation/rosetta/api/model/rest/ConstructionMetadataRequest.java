package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.cardanofoundation.rosetta.api.model.ConstructionMetadataRequestOptions;
import org.cardanofoundation.rosetta.api.model.PublicKey;

/**
 * A ConstructionMetadataRequest is utilized to get information required to construct a transaction. The Options object used to specify which metadata to return is left purposely unstructured to allow flexibility for implementers. Options is not required in the case that there is network-wide metadata of interest. Optionally, the request can also include an array of PublicKeys associated with the AccountIdentifiers returned in ConstructionPreprocessResponse.
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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



  public ConstructionMetadataRequest addPublicKeysItem(PublicKey publicKeysItem) {
    if (this.publicKeys == null) {
      this.publicKeys = new ArrayList<>();
    }
    this.publicKeys.add(publicKeysItem);
    return this;
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

