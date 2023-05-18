package org.cardanofoundation.rosetta.crawler.projection;

import java.math.BigDecimal;

/**
 * A Projection for the {@link org.openapitools.entity.EpochParam} entity
 */
public interface EpochParamProjection {
    BigDecimal getCoinsPerUtxoSize();

    BigDecimal getKeyDeposit();

    Integer getMaxCollateralInputs();

    Integer getMaxTxSize();

    BigDecimal getMaxValSize();

    Integer getMinFeeA();

    Integer getMinFeeB();

    BigDecimal getMinPoolCost();

    BigDecimal getPoolDeposit();

    Integer getProtocolMajor();
}