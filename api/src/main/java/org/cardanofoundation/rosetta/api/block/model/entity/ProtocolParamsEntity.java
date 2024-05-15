package org.cardanofoundation.rosetta.api.block.model.entity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProtocolParamsEntity {

  private Integer minFeeA; //0
  private Integer minFeeB; //1
  private Integer maxBlockSize; //2
  private Integer maxTxSize; //3
  private Integer maxBlockHeaderSize; //4
  private BigInteger keyDeposit; //5
  private BigInteger poolDeposit; //6
  private Integer maxEpoch; //7
  @JsonProperty("nopt")
  private Integer nOpt; //8
  private BigDecimal poolPledgeInfluence; //rational //9
  private BigDecimal expansionRate; //unit interval //10
  private BigDecimal treasuryGrowthRate; //11
  private BigDecimal decentralisationParam; //12
  private String extraEntropy; //13
  private Integer protocolMajorVer; //14
  private Integer protocolMinorVer; //14
  private BigInteger minUtxo; //15

  private BigInteger minPoolCost; //16
  private BigInteger adaPerUtxoByte; //17

  //Alonzo changes
  private Map<String, long[]> costModels; //18
  private String costModelsHash;

  //ex_unit_prices
  private BigDecimal priceMem; //19
  private BigDecimal priceStep; //19

  //max tx ex units
  private BigInteger maxTxExMem; //20
  private BigInteger maxTxExSteps; //20

  //max block ex units
  private BigInteger maxBlockExMem; //21
  private BigInteger maxBlockExSteps; //21

  private Long maxValSize; //22

  private Integer collateralPercent; //23
  private Integer maxCollateralInputs; //24

  private Integer committeeMinSize; //27
  private Integer committeeMaxTermLength; //28
  private Integer govActionLifetime; //29
  private BigInteger govActionDeposit; //30
  private BigInteger drepDeposit; //31
  private Integer drepActivity; //32

}
