package org.cardanofoundation.rosetta.common.model.cardano;

import java.math.BigInteger;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.cardanofoundation.rosetta.api.block.model.domain.ProtocolParams;

/**
 * ProtocolParameters
 */

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
  public class ProtocolParameters {

  @JsonProperty("coinsPerUtxoSize")
  private String coinsPerUtxoSize;

  @JsonProperty("maxTxSize")
  private Integer maxTxSize;

  @JsonProperty("maxValSize")
  private BigInteger maxValSize;

  @JsonProperty("keyDeposit")
  private String keyDeposit;

  @JsonProperty("maxCollateralInputs")
  private Integer maxCollateralInputs;

  @JsonProperty("minFeeCoefficient")
  private Integer minFeeCoefficient;

  @JsonProperty("minFeeConstant")
  private Integer minFeeConstant;

  @JsonProperty("minPoolCost")
  private String minPoolCost;

  @JsonProperty("poolDeposit")
  private String poolDeposit;

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

  public ProtocolParameters fromEntity(ProtocolParams entity) {
    return ProtocolParameters.builder()
        .coinsPerUtxoSize(entity.getAdaPerUtxoByte().toString())
        .maxTxSize(entity.getMaxTxSize())
        .maxValSize(BigInteger.valueOf(entity.getMaxValSize()))
        .keyDeposit(entity.getKeyDeposit().toString())
        .maxCollateralInputs(entity.getMaxCollateralInputs())
        .minFeeCoefficient(entity.getMinFeeA())
        .minFeeConstant(entity.getMinFeeB())
        .minPoolCost(entity.getMinPoolCost().toString())
        .poolDeposit(entity.getPoolDeposit().toString())
        .protocol(entity.getProtocolVersion().getMajor())
        .build();
  }
}
