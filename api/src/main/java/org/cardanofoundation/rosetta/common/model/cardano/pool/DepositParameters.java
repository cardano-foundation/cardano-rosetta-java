package org.cardanofoundation.rosetta.common.model.cardano.pool;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Generated;
import java.util.HashMap;

/**
 * DepositParameters
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-03-21T15:54:41.273447600+07:00[Asia/Bangkok]")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DepositParameters {

  @JsonProperty("keyDeposit")
  private String keyDeposit;

  @JsonProperty("poolDeposit")
  private String poolDeposit;


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

  public HashMap<String, String> toHashMap() {
    HashMap<String, String> depositParameters = new HashMap<>();
    if (this.keyDeposit != null) {
      depositParameters.put("keyDeposit", this.keyDeposit);
    }
    if (this.poolDeposit != null) {
      depositParameters.put("poolDeposit", this.poolDeposit);
    }
    return depositParameters;
  }
}

