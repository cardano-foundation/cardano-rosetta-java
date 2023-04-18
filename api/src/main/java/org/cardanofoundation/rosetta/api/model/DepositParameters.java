package org.cardanofoundation.rosetta.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * DepositParameters
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
@AllArgsConstructor
public class DepositParameters {

  @JsonProperty("keyDeposit")
  private String keyDeposit;

  @JsonProperty("poolDeposit")
  private String poolDeposit;

  public DepositParameters keyDeposit(String keyDeposit) {
    this.keyDeposit = keyDeposit;
    return this;
  }

  /**
   * key registration cost in Lovelace
   * @return keyDeposit
  */
  @NotNull 
  @Schema(name = "keyDeposit", description = "key registration cost in Lovelace", requiredMode = Schema.RequiredMode.REQUIRED)
  public String getKeyDeposit() {
    return keyDeposit;
  }

  public void setKeyDeposit(String keyDeposit) {
    this.keyDeposit = keyDeposit;
  }

  public DepositParameters poolDeposit(String poolDeposit) {
    this.poolDeposit = poolDeposit;
    return this;
  }

  /**
   * pool registration cost in Lovelace
   * @return poolDeposit
  */
  @NotNull 
  @Schema(name = "poolDeposit", description = "pool registration cost in Lovelace", requiredMode = Schema.RequiredMode.REQUIRED)
  public String getPoolDeposit() {
    return poolDeposit;
  }

  public void setPoolDeposit(String poolDeposit) {
    this.poolDeposit = poolDeposit;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DepositParameters depositParameters = (DepositParameters) o;
    return Objects.equals(this.keyDeposit, depositParameters.keyDeposit) &&
        Objects.equals(this.poolDeposit, depositParameters.poolDeposit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keyDeposit, poolDeposit);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DepositParameters {\n");
    sb.append("    keyDeposit: ").append(toIndentedString(keyDeposit)).append("\n");
    sb.append("    poolDeposit: ").append(toIndentedString(poolDeposit)).append("\n");
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

