package org.cardanofoundation.rosetta.api.model.cardano;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openapitools.client.model.PublicKey;
import org.openapitools.client.model.SignatureType;
import org.openapitools.client.model.SigningPayload;

/**
 * Signature contains the payload that was signed, the public keys of the keypairs used to produce the signature, the signature (encoded in hex), and the SignatureType. PublicKey is often times not known during construction of the signing payloads but may be needed to combine signatures properly.
 */

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Signature {

  @JsonProperty("signing_payload")
  private SigningPayload signingPayload;

  @JsonProperty("public_key")
  private PublicKey publicKey;

  @JsonProperty("signature_type")
  private SignatureType signatureType;

  @JsonProperty("hex_bytes")
  private String hexBytes;


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Signature signature = (Signature) o;
    return Objects.equals(this.signingPayload, signature.signingPayload) &&
        Objects.equals(this.publicKey, signature.publicKey) &&
        Objects.equals(this.signatureType, signature.signatureType) &&
        Objects.equals(this.hexBytes, signature.hexBytes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(signingPayload, publicKey, signatureType, hexBytes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Signature {\n");
    sb.append("    signingPayload: ").append(toIndentedString(signingPayload)).append("\n");
    sb.append("    publicKey: ").append(toIndentedString(publicKey)).append("\n");
    sb.append("    signatureType: ").append(toIndentedString(signatureType)).append("\n");
    sb.append("    hexBytes: ").append(toIndentedString(hexBytes)).append("\n");
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

