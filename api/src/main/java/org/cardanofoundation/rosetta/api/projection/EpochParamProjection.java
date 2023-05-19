package org.cardanofoundation.rosetta.api.projection;

import java.math.BigDecimal;

/**
 * A Projection for the {@link org.openapitools.entity.EpochParam} entity
 */
public interface EpochParamProjection {

  BigDecimal getCoinsPerUtxoSize();

  Integer getMaxTxSize();

  BigDecimal getMaxValSize();

  BigDecimal getKeyDeposit();

  Integer getMaxCollateralInputs();

  Integer getMinFeeA();

  Integer getMinFeeB();

  BigDecimal getMinPoolCost();

  BigDecimal getPoolDeposit();

  Integer getProtocolMajor();
}