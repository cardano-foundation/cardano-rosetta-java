package org.cardanofoundation.rosetta.consumer.hash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import co.nstant.in.cbor.model.ByteString;
import com.bloxbean.cardano.client.crypto.Blake2bUtil;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.PlutusV1Script;
import com.sotatek.cardano.ledgersync.util.HexUtil;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ScriptTest {

  @Test
  public void testHashCbor() throws CborDeserializationException {
    String cborHex = "0x4D01000033222220051200120011";
    PlutusV1Script plutusV1Script = PlutusV1Script.deserialize(new ByteString(HexUtil.decodeHexString(cborHex)));
    String hashCbor = HexUtil.encodeHexString(Blake2bUtil.blake2bHash224(getPlutusV1Bytes(cborHex)));
    System.out.println(hashCbor);
    String hash = "";
    try {
      hash = HexUtil.encodeHexString(plutusV1Script.getScriptHash());
    } catch (CborSerializationException e) {
      throw new RuntimeException(e);
    }
    assertEquals("67f33146617a5e61936081db3b2117cbf59bd2123748f58ac9678656",hash);
  }


  private byte[] getPlutusV1Bytes(String cborHex){
    byte[] first = new byte[]{(byte) 1};
    byte[] data = HexUtil.decodeHexString(cborHex);
    var finalData = ByteBuffer.allocate(first.length + data.length)
        .put(first)
        .put(data)
        .array();
    return finalData;
  }
}
