package org.cardanofoundation.rosetta.consumer.service.impl.plutus;

import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.PlutusV1Script;
import com.bloxbean.cardano.client.util.HexUtil;
import org.cardanofoundation.rosetta.common.entity.Script;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.enumeration.ScriptType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PlutusScriptV1ServiceImpl extends PlutusScriptService<PlutusV1Script>{

  @Override
  public Script handle(PlutusV1Script plutusScript, Tx tx) {
    try {
      return buildScript(getTxScriptBytes(plutusScript,tx), ScriptType.PLUTUSV1,tx,
          plutusScript.getPolicyId());
    } catch (CborSerializationException e) {
      log.error("Get Policy Id of plutusV1, tx {}",tx.getHash());
    }
    return null;
  }

  @Override
  protected byte[] getTxScriptBytes(PlutusV1Script plutusScript, Tx tx) {
    byte[] data = new byte[]{};
    try{
      data = HexUtil.decodeHexString(plutusScript.getCborHex());
    }catch (Exception e){
      log.warn("Exception serialize plutus V1 script tx_hash: {}, ",tx.getHash());
    }
    return data;
  }
}
