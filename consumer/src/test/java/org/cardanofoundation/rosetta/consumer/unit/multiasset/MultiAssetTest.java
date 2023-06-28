package org.cardanofoundation.rosetta.consumer.unit.multiasset;

import static org.junit.Assert.assertEquals;

import org.cardanofoundation.rosetta.common.ledgersync.constant.Constant;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;

@Profile("test")
 class MultiAssetTest {

  @Test
   void CheckEqualLoveLace() {
    String assetLoveLace = "LOVELACE";
    assertEquals(true, Constant.isLoveLace(assetLoveLace.getBytes()));
  }

}
