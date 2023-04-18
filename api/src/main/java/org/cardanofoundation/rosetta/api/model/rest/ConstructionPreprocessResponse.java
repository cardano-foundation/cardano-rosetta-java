package org.cardanofoundation.rosetta.api.model.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import org.cardanofoundation.rosetta.api.model.ConstructionPreprocessResponseOptions;

import javax.annotation.Generated;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ConstructionPreprocessResponse contains &#x60;options&#x60; that will be sent unmodified to &#x60;/construction/metadata&#x60;. If it is not necessary to make a request to &#x60;/construction/metadata&#x60;, &#x60;options&#x60; should be omitted.  Some blockchains require the PublicKey of particular AccountIdentifiers to construct a valid transaction. To fetch these PublicKeys, populate &#x60;required_public_keys&#x60; with the AccountIdentifiers associated with the desired PublicKeys. If it is not necessary to retrieve any PublicKeys for construction, &#x60;required_public_keys&#x60; should be omitted.
 */

@Schema(name = "ConstructionPreprocessResponse", description = "ConstructionPreprocessResponse contains `options` that will be sent unmodified to `/construction/metadata`. If it is not necessary to make a request to `/construction/metadata`, `options` should be omitted.  Some blockchains require the PublicKey of particular AccountIdentifiers to construct a valid transaction. To fetch these PublicKeys, populate `required_public_keys` with the AccountIdentifiers associated with the desired PublicKeys. If it is not necessary to retrieve any PublicKeys for construction, `required_public_keys` should be omitted.")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
@AllArgsConstructor
public class ConstructionPreprocessResponse {

  @JsonProperty("options")
  private ConstructionPreprocessResponseOptions options;

  @JsonProperty("required_public_keys")
  @Valid
  private List<AccountIdentifier> requiredPublicKeys = null;


  public ConstructionPreprocessResponse options(ConstructionPreprocessResponseOptions options) {
    this.options = options;
    return this;
  }

  /**
   * Get options
   * @return options
  */
  @Valid 
  @Schema(name = "options", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public ConstructionPreprocessResponseOptions getOptions() {
    return options;
  }

  public void setOptions(ConstructionPreprocessResponseOptions options) {
    this.options = options;
  }

  public ConstructionPreprocessResponse requiredPublicKeys(List<AccountIdentifier> requiredPublicKeys) {
    this.requiredPublicKeys = requiredPublicKeys;
    return this;
  }

  public ConstructionPreprocessResponse addRequiredPublicKeysItem(AccountIdentifier requiredPublicKeysItem) {
    if (this.requiredPublicKeys == null) {
      this.requiredPublicKeys = new ArrayList<>();
    }
    this.requiredPublicKeys.add(requiredPublicKeysItem);
    return this;
  }

  /**
   * Get requiredPublicKeys
   * @return requiredPublicKeys
  */
  @Valid 
  @Schema(name = "required_public_keys", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public List<AccountIdentifier> getRequiredPublicKeys() {
    return requiredPublicKeys;
  }

  public void setRequiredPublicKeys(List<AccountIdentifier> requiredPublicKeys) {
    this.requiredPublicKeys = requiredPublicKeys;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConstructionPreprocessResponse constructionPreprocessResponse = (ConstructionPreprocessResponse) o;
    return Objects.equals(this.options, constructionPreprocessResponse.options) &&
        Objects.equals(this.requiredPublicKeys, constructionPreprocessResponse.requiredPublicKeys);
  }

  @Override
  public int hashCode() {
    return Objects.hash(options, requiredPublicKeys);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConstructionPreprocessResponse {\n");
    sb.append("    options: ").append(toIndentedString(options)).append("\n");
    sb.append("    requiredPublicKeys: ").append(toIndentedString(requiredPublicKeys)).append("\n");
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

