package org.cardanofoundation.rosetta.api.model.cardano;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openapitools.client.model.AccountIdentifier;
import org.openapitools.client.model.SignatureType;

/**
 * SigningPayload is signed by the client with the keypair associated with an AccountIdentifier using the specified SignatureType. SignatureType can be optionally populated if there is a restriction on the signature scheme that can be used to sign the payload.
 */



@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SigningPayload {

  @JsonProperty("address")
  private String address;

  @JsonProperty("account_identifier")
  private AccountIdentifier accountIdentifier;

  @JsonProperty("hex_bytes")
  private String hexBytes;

  @JsonProperty("signature_type")
  private SignatureType signatureType;


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SigningPayload {\n");
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    accountIdentifier: ").append(toIndentedString(accountIdentifier)).append("\n");
    sb.append("    hexBytes: ").append(toIndentedString(hexBytes)).append("\n");
    sb.append("    signatureType: ").append(toIndentedString(signatureType)).append("\n");
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

