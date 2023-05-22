package org.cardanofoundation.rosetta.api.projection;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A Projection for the {@link org.openapitools.entity.EpochParam} entity
 */
public interface EpochParamProjection {

  BigInteger getCoinsPerUtxoSize();

  BigInteger getKeyDeposit();

  Integer getMaxCollateralInputs();

  Integer getMaxTxSize();

  BigInteger getMaxValSize();

  Integer getMinFeeA();

  Integer getMinFeeB();

  BigInteger getMinPoolCost();

  BigInteger getPoolDeposit();

  Integer getProtocolMajor();
}