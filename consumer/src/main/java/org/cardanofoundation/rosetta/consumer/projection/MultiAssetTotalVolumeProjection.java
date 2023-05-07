package org.cardanofoundation.rosetta.consumer.projection;

import java.math.BigInteger;

public interface MultiAssetTotalVolumeProjection {

  Long getIdentId();

  BigInteger getTotalVolume();
}
