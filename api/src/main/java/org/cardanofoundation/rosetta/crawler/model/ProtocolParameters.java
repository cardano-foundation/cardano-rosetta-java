package org.cardanofoundation.rosetta.crawler.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * ProtocolParameters
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProtocolParameters {

  @JsonProperty("coinsPerUtxoSize")
  private Long coinsPerUtxoSize;

  @JsonProperty("maxTxSize")
  private Integer maxTxSize;

  @JsonProperty("maxValSize")
  private Long maxValSize;

  @JsonProperty("keyDeposit")
  private Long keyDeposit;

  @JsonProperty("maxCollateralInputs")
  private Integer maxCollateralInputs;

  @JsonProperty("minFeeCoefficient")
  private Integer minFeeCoefficient;

  @JsonProperty("minFeeConstant")
  private Integer minFeeConstant;

  @JsonProperty("minPoolCost")
  private Long minPoolCost;

  @JsonProperty("poolDeposit")
  private Long poolDeposit;

  @JsonProperty("protocol")
  private Integer protocol;


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ProtocolParameters {\n");
    sb.append("    coinsPerUtxoSize: ").append(toIndentedString(coinsPerUtxoSize)).append("\n");
    sb.append("    maxTxSize: ").append(toIndentedString(maxTxSize)).append("\n");
    sb.append("    maxValSize: ").append(toIndentedString(maxValSize)).append("\n");
    sb.append("    keyDeposit: ").append(toIndentedString(keyDeposit)).append("\n");
    sb.append("    maxCollateralInputs: ").append(toIndentedString(maxCollateralInputs)).append("\n");
    sb.append("    minFeeCoefficient: ").append(toIndentedString(minFeeCoefficient)).append("\n");
    sb.append("    minFeeConstant: ").append(toIndentedString(minFeeConstant)).append("\n");
    sb.append("    minPoolCost: ").append(toIndentedString(minPoolCost)).append("\n");
    sb.append("    poolDeposit: ").append(toIndentedString(poolDeposit)).append("\n");
    sb.append("    protocol: ").append(toIndentedString(protocol)).append("\n");
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

