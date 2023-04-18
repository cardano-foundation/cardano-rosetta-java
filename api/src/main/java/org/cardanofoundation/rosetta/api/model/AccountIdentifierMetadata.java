package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import java.util.Objects;

/**
 * Blockchains that utilize a username model (where the address is not a derivative of a cryptographic public key) should specify the public key(s) owned by the address in metadata.
 */

@Schema(name = "AccountIdentifier_metadata", description = "Blockchains that utilize a username model (where the address is not a derivative of a cryptographic public key) should specify the public key(s) owned by the address in metadata.")
@JsonTypeName("AccountIdentifier_metadata")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
public class AccountIdentifierMetadata {

  @JsonProperty("chain_code")
  private String chainCode;

  public AccountIdentifierMetadata chainCode(String chainCode) {
    this.chainCode = chainCode;
    return this;
  }

  /**
   * Hex string encoded extension of bip32 private and public keys with an extra 256 bits of entropy that consists of 32 bytes
   * @return chainCode
  */
  
  @Schema(name = "chain_code", description = "Hex string encoded extension of bip32 private and public keys with an extra 256 bits of entropy that consists of 32 bytes", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  public String getChainCode() {
    return chainCode;
  }

  public void setChainCode(String chainCode) {
    this.chainCode = chainCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AccountIdentifierMetadata accountIdentifierMetadata = (AccountIdentifierMetadata) o;
    return Objects.equals(this.chainCode, accountIdentifierMetadata.chainCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(chainCode);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccountIdentifierMetadata {\n");
    sb.append("    chainCode: ").append(toIndentedString(chainCode)).append("\n");
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

