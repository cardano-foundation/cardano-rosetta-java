package org.cardanofoundation.rosetta.api.block.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProtocolParams {
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
    private BigInteger minUtxo; //TODO //15

    private BigInteger minPoolCost; //16
    private BigInteger adaPerUtxoByte; //17
    //private String nonce;

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

//    //Cost per UTxO word for Alonzo.
//    //Cost per UTxO byte for Babbage and later.
//    private String coinsPerUtxoSize;
//    @Deprecated
//    private String coinsPerUtxoWord;

    //Conway era fields
//    private PoolVotingThresholds poolVotingThresholds; //25
//    private DrepVoteThresholds drepVotingThresholds; //26
    private Integer committeeMinSize; //27
    private Integer committeeMaxTermLength; //28
    private Integer govActionLifetime; //29
    private BigInteger govActionDeposit; //30
    private BigInteger drepDeposit; //31
    private Integer drepActivity; //32

    // TODO clarify if parameters are correctly set
    public static ProtocolParams fromJSONObject(JSONObject shelleyJsonObject) {
        ProtocolParams p = new ProtocolParams();
        JSONObject shelleyProtocolParams = shelleyJsonObject.getJSONObject("protocolParams");
        p.setMinFeeA(shelleyProtocolParams.getInt("minFeeA"));
        p.setMinFeeB(shelleyProtocolParams.getInt("minFeeB"));
        p.setMaxBlockSize(shelleyProtocolParams.getInt("maxBlockBodySize"));
        p.setMaxTxSize(shelleyProtocolParams.getInt("maxTxSize"));
        p.setMaxBlockHeaderSize(shelleyProtocolParams.getInt("maxBlockHeaderSize"));
        p.setKeyDeposit(shelleyProtocolParams.getBigInteger("keyDeposit"));
        p.setPoolDeposit(shelleyProtocolParams.getBigInteger("poolDeposit"));
        p.setNOpt(shelleyProtocolParams.getInt("nOpt"));
        p.setDecentralisationParam(shelleyProtocolParams.getBigDecimal("decentralisationParam"));
        p.setExtraEntropy(shelleyProtocolParams.getJSONObject("extraEntropy").getString("tag"));
        JSONObject protolVersion = shelleyProtocolParams.getJSONObject("protocolVersion");
        p.setProtocolMajorVer(protolVersion.getInt("major"));
        p.setProtocolMinorVer(protolVersion.getInt("minor"));
        p.setMinUtxo(shelleyProtocolParams.getBigInteger("minUTxOValue"));
        p.setMinPoolCost(shelleyProtocolParams.getBigInteger("minPoolCost"));
        p.setAdaPerUtxoByte(shelleyProtocolParams.getBigInteger("minFeeA"));

        return p;
    }

    public void merge(ProtocolParams other) {
        if (this.minFeeA == null) {
            this.minFeeA = other.minFeeA;
        }
        if (this.minFeeB == null) {
            this.minFeeB = other.minFeeB;
        }
        if (this.maxBlockSize == null) {
            this.maxBlockSize = other.maxBlockSize;
        }
        if (this.maxTxSize == null) {
            this.maxTxSize = other.maxTxSize;
        }
        if (this.maxBlockHeaderSize == null) {
            this.maxBlockHeaderSize = other.maxBlockHeaderSize;
        }
        if (this.keyDeposit == null) {
            this.keyDeposit = other.keyDeposit;
        }
        if (this.poolDeposit == null) {
            this.poolDeposit = other.poolDeposit;
        }
        if (this.maxEpoch == null) {
            this.maxEpoch = other.maxEpoch;
        }
        if (this.nOpt == null) {
            this.nOpt = other.nOpt;
        }
        if (this.poolPledgeInfluence == null) {
            this.poolPledgeInfluence = other.poolPledgeInfluence;
        }
        if (this.expansionRate == null) {
            this.expansionRate = other.expansionRate;
        }
        if (this.treasuryGrowthRate == null) {
            this.treasuryGrowthRate = other.treasuryGrowthRate;
        }
        if (this.decentralisationParam == null) {
            this.decentralisationParam = other.decentralisationParam;
        }
        if (this.extraEntropy == null) {
            this.extraEntropy = other.extraEntropy;
        }
        if (this.protocolMajorVer == null) {
            this.protocolMajorVer = other.protocolMajorVer;
        }
        if (this.protocolMinorVer == null) {
            this.protocolMinorVer = other.protocolMinorVer;
        }
        if (this.minUtxo == null) {
            this.minUtxo = other.minUtxo;
        }
        if (this.minPoolCost == null) {
            this.minPoolCost = other.minPoolCost;
        }
        if (this.adaPerUtxoByte == null) {
            this.adaPerUtxoByte = other.adaPerUtxoByte;
        }
        if (this.costModels == null) {
            if (this.costModels == null) {
                this.costModels = other.getCostModels();
            } else {
                var keys = other.getCostModels().keySet();
                keys.forEach(key -> this.costModels.put(key, other.costModels.get(key)));
            }
        }

        if (this.costModelsHash == null) {
            this.costModelsHash = other.costModelsHash;
        }

        if (this.priceMem == null) {
            this.priceMem = other.priceMem;
        }
        if (this.priceStep == null) {
            this.priceStep = other.priceStep;
        }
        if (this.maxTxExMem == null) {
            this.maxTxExMem = other.maxTxExMem;
        }
        if (this.maxTxExSteps == null) {
            this.maxTxExSteps = other.maxTxExSteps;
        }
        if (this.maxBlockExMem == null) {
            this.maxBlockExMem = other.maxBlockExMem;
        }
        if (this.maxBlockExSteps == null) {
            this.maxBlockExSteps = other.maxBlockExSteps;
        }
        if (this.maxValSize == null) {
            this.maxValSize = other.maxValSize;
        }
        if (this.collateralPercent == null) {
            this.collateralPercent = other.collateralPercent;
        }
        if (this.maxCollateralInputs == null) {
            this.maxCollateralInputs = other.maxCollateralInputs;
        }
//        if (other.poolVotingThresholds == null) {
//            this.poolVotingThresholds = other.poolVotingThresholds;
//        }
//        if (other.drepVotingThresholds == null) {
//            this.drepVotingThresholds = other.drepVotingThresholds;
//        }
        if (this.committeeMinSize == null) {
            this.committeeMinSize = other.committeeMinSize;
        }
        if (this.committeeMaxTermLength == null) {
            this.committeeMaxTermLength = other.committeeMaxTermLength;
        }
        if (this.govActionLifetime == null) {
            this.govActionLifetime = other.govActionLifetime;
        }
        if (this.govActionDeposit == null) {
            this.govActionDeposit = other.govActionDeposit;
        }
        if (this.drepDeposit == null) {
            this.drepDeposit = other.drepDeposit;
        }
        if (this.drepActivity == null) {
            this.drepActivity = other.drepActivity;
        }
    }
}
