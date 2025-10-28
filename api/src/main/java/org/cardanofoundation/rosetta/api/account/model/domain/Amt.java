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

  // TODO avoid using assetName field for now
  // TODO ASCI in case of CIP-26 and bech32 in case of CIP-68, actually it should always be ASCII and never bech32
  @Deprecated
  // consider removing
  private String assetName;

  private BigInteger quantity;

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

  @Deprecated
  // TODO avoid using assetName field for now
  public String getAssetName() {
    return assetName;
  }

}
