package org.cardanofoundation.rosetta.api.block.model.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProtocolParams {

  private Integer minFeeA; //0
  private Integer minFeeB; //1
  private Integer maxBlockBodySize; //2
  private Integer maxTxSize; //3
  private Integer maxBlockHeaderSize; //4
  private BigInteger keyDeposit; //5
  private BigInteger poolDeposit; //6
  private Integer maxEpoch; //7
  @JsonProperty("nOpt")
  private Integer nOpt; //8
  private BigDecimal poolPledgeInfluence; //rational //9
  private BigDecimal expansionRate; //unit interval //10
  private BigDecimal treasuryGrowthRate; //11
  private BigDecimal decentralisationParam; //12
  private ExtraEntropy extraEntropy; //13
  private ProtocolVersion protocolVersion; //14
  @JsonProperty("minUTxOValue")
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

  //Conway era fields
//    private PoolVotingThresholds poolVotingThresholds; //25
//    private DrepVoteThresholds drepVotingThresholds; //26
  private Integer committeeMinSize; //27
  private Integer committeeMaxTermLength; //28
  private Integer govActionLifetime; //29
  private BigInteger govActionDeposit; //30
  private BigInteger drepDeposit; //31
  private Integer drepActivity; //32

  @Data
  public static class ExtraEntropy{
    String tag;
  }

  @Data
  public static class ProtocolVersion{
    Integer minor;
    Integer major;
  }
}
