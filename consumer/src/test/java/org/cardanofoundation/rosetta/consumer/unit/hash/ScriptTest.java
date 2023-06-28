package org.cardanofoundation.rosetta.consumer.unit.hash;

import co.nstant.in.cbor.model.ByteString;
import com.bloxbean.cardano.client.crypto.Blake2bUtil;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.PlutusV1Script;
import org.cardanofoundation.rosetta.common.util.HexUtil;
import org.cardanofoundation.rosetta.consumer.service.ScriptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Profile("test-unit")
@ActiveProfiles("test-unit")
class ScriptTest {

  @Autowired
  private ScriptService scriptService;

  @Test
  void testHashCbor() throws CborDeserializationException {
    String cborHex = "0x4D01000033222220051200120011";
    PlutusV1Script plutusV1Script = PlutusV1Script.deserialize(
        new ByteString(HexUtil.decodeHexString(cborHex)));
    String hashCbor = HexUtil.encodeHexString(
        Blake2bUtil.blake2bHash224(getPlutusV1Bytes(cborHex)));
    System.out.println(hashCbor);
    String hash = "";
    try {
      hash = HexUtil.encodeHexString(plutusV1Script.getScriptHash());
    } catch (CborSerializationException e) {
      throw new RuntimeException(e);
    }
    assertEquals("67f33146617a5e61936081db3b2117cbf59bd2123748f58ac9678656", hash);
  }


  private byte[] getPlutusV1Bytes(String cborHex) {
    byte[] first = new byte[]{(byte) 1};
    byte[] data = HexUtil.decodeHexString(cborHex);
    var finalData = ByteBuffer.allocate(first.length + data.length)
        .put(first)
        .put(data)
        .array();
    return finalData;
  }

  @Test
  void testScriptHashByScriptRefer() throws CborSerializationException {
    String cborHex = "82014e4d01000033222220051200120011";
    assertEquals("67f33146617a5e61936081db3b2117cbf59bd2123748f58ac9678656",
        scriptService.getHashOfReferenceScript(cborHex));
  }
}
