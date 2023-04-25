package org.cardanofoundation.rosetta.api.addedClass;

public interface ProtocolParametersResponse {
    Double getCoinsPerUtxoSize();
    Integer getMaxTxSize();
    Double getMaxValSize();
    Double getKeyDeposit();
    Integer getMaxCollateralInputs();
    Integer getMinFeeCoefficient();
    Integer getMinFeeConstant();
    Double getMinPoolCost();
    Double getPoolDeposit();
    Integer getProtocol();
}
