package org.cardanofoundation.rosetta.api.account.model.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Amt implements Serializable {

  private String unit; // subject = policyId + hex(assetName)
  private String policyId;

  private BigInteger quantity;

  /**
   * Returns symbol as hex
   *
   * unit (subject) = policyId(hex) + symbol(hex)
   */
  @Nullable
  public String getAssetNameAsHex() {
    return getSymbolHex();
  }

  /**
   * Returns symbol as hex
   *
   * unit (subject) = policyId(hex) + symbol(hex)
   */
  @Nullable
  public String getSymbolHex() {
    if (unit == null || policyId == null) {
      return null;
    }

    return unit.replace(policyId, "");
  }

}
