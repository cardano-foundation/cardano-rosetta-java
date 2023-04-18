package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * PublicKey contains a public key byte array for a particular CurveType encoded in hex. Note that there is no PrivateKey struct as this is NEVER the concern of an implementation.
 */

@Schema(name = "PublicKey", description = "PublicKey contains a public key byte array for a particular CurveType encoded in hex. Note that there is no PrivateKey struct as this is NEVER the concern of an implementation.")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
public class PublicKey {

  @JsonProperty("hex_bytes")
  private String hexBytes;

  @JsonProperty("curve_type")
  private CurveType curveType;

  public PublicKey(String hexBytes) {
    this.hexBytes = hexBytes;
  }

  /**
   * Hex-encoded public key bytes in the format specified by the CurveType.
   * @return hexBytes
  */
  @NotNull 
  @Schema(name = "hex_bytes", description = "Hex-encoded public key bytes in the format specified by the CurveType.", requiredMode = Schema.RequiredMode.REQUIRED)
  public String getHexBytes() {
    return hexBytes;
  }

  public void setHexBytes(String hexBytes) {
    this.hexBytes = hexBytes;
  }

  public PublicKey curveType(CurveType curveType) {
    this.curveType = curveType;
    return this;
  }

  /**
   * Get curveType
   * @return curveType
  */
  @NotNull @Valid 
  @Schema(name = "curve_type", requiredMode = Schema.RequiredMode.REQUIRED)
  public CurveType getCurveType() {
    return curveType;
  }

  public void setCurveType(CurveType curveType) {
    this.curveType = curveType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PublicKey publicKey = (PublicKey) o;
    return Objects.equals(this.hexBytes, publicKey.hexBytes) &&
        Objects.equals(this.curveType, publicKey.curveType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hexBytes, curveType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PublicKey {\n");
    sb.append("    hexBytes: ").append(toIndentedString(hexBytes)).append("\n");
    sb.append("    curveType: ").append(toIndentedString(curveType)).append("\n");
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

