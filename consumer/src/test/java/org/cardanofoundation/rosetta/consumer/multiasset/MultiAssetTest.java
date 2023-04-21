package org.cardanofoundation.rosetta.consumer.multiasset;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sotatek.cardano.ledgersync.common.constant.Constant;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;

@Profile("test")
public class MultiAssetTest {

  @Test
  public void CheckEqualLoveLace() {
    String assetLoveLace = "LOVELACE";
    assertEquals(true, Constant.isLoveLace(assetLoveLace.getBytes()));
  }

}
