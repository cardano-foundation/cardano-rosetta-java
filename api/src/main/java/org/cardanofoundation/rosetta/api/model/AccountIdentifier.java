package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * The account_identifier uniquely identifies an account within a network. All fields in the account_identifier are utilized to determine this uniqueness (including the metadata field, if populated).
 */

@Schema(name = "AccountIdentifier", description = "The account_identifier uniquely identifies an account within a network. All fields in the account_identifier are utilized to determine this uniqueness (including the metadata field, if populated).")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
public class AccountIdentifier {

  @JsonProperty("address")
  private String address;

  @JsonProperty("sub_account")
  private SubAccountIdentifier subAccount;

  @JsonProperty("metadata")
  private AccountIdentifierMetadata metadata;

  public AccountIdentifier(String address) {
    this.address = address;
  }

  /**
   * The address may be a cryptographic public key (or some encoding of it) or a provided username.
   * @return address
  */
  @NotNull 
  @Schema(name = "address", example = "0x3a065000ab4183c6bf581dc1e55a605455fc6d61", description = "The address may be a cryptographic public key (or some encoding of it) or a provided username.", requiredMode = Schema.RequiredMode.REQUIRED)
  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public AccountIdentifier subAccount(SubAccountIdentifier subAccount) {
    this.subAccount = subAccount;
    return this;
  }

  /**
   * Get subAccount
   * @return subAccount
  */
  @Valid 
  @Schema(name = "sub_account", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public SubAccountIdentifier getSubAccount() {
    return subAccount;
  }

  public void setSubAccount(SubAccountIdentifier subAccount) {
    this.subAccount = subAccount;
  }

  public AccountIdentifier metadata(AccountIdentifierMetadata metadata) {
    this.metadata = metadata;
    return this;
  }

  /**
   * Get metadata
   * @return metadata
  */
  @Valid 
  @Schema(name = "metadata", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public AccountIdentifierMetadata getMetadata() {
    return metadata;
  }

  public void setMetadata(AccountIdentifierMetadata metadata) {
    this.metadata = metadata;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AccountIdentifier accountIdentifier = (AccountIdentifier) o;
    return Objects.equals(this.address, accountIdentifier.address) &&
        Objects.equals(this.subAccount, accountIdentifier.subAccount) &&
        Objects.equals(this.metadata, accountIdentifier.metadata);
  }

  @Override
  public int hashCode() {
    return Objects.hash(address, subAccount, metadata);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccountIdentifier {\n");
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    subAccount: ").append(toIndentedString(subAccount)).append("\n");
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
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

