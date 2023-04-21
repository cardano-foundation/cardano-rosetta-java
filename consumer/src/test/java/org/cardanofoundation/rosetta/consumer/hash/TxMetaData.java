package org.cardanofoundation.rosetta.consumer.hash;

import com.sotatek.cardano.ledgersync.util.HexUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TxMetaData {

  @Test
  public void testHexEqual(){
   String json = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
   String cborHex = "a1190137784061616161616161616161616161616161616161616161616161616161616161616161616161616161616161616161616161616161616161616161616161616161";
    System.out.println(HexUtil.encodeHexString(json.getBytes()));
    System.out.println(cborHex);
   assertEquals(json.getBytes(),HexUtil.decodeHexString(cborHex));
  }

}
