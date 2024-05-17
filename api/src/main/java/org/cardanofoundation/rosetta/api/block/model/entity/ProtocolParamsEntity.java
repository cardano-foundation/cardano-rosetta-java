package org.cardanofoundation.rosetta.api.block.model.entity;

import java.math.BigInteger;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
  private Integer maxTxSize; //3
  private BigInteger keyDeposit; //5
  private BigInteger poolDeposit; //6

  private Integer protocolMajorVer; //14
  private Integer protocolMinorVer; //14

  private BigInteger minPoolCost; //16
  private BigInteger adaPerUtxoByte; //17

  //Alonzo changes
  private Map<String, long[]> costModels; //18

  private Long maxValSize; //22

  private Integer maxCollateralInputs; //24

}
