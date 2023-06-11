package org.cardanofoundation.rosetta.consumer.service.impl.plutus;

import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.PlutusV2Script;
import com.bloxbean.cardano.client.util.HexUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.rosetta.common.entity.Script;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.enumeration.ScriptType;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PlutusScriptV2ServiceImpl extends PlutusScriptService<PlutusV2Script>{

  @Override
  public Script handle(PlutusV2Script plutusScript, Tx tx) {
    try {
      return buildScript(getTxScriptBytes(plutusScript,tx), ScriptType.PLUTUSV2,tx,
          plutusScript.getPolicyId());
    } catch (CborSerializationException e) {
      log.error("Get Policy Id of plutusV2, tx {}",tx.getHash());
    }
    return null;
  }

  @Override
  protected byte[] getTxScriptBytes(PlutusV2Script plutusScript, Tx tx) {
    byte[] data = new byte[]{};
    try{
      data = HexUtil.decodeHexString(plutusScript.getCborHex());
    }catch (Exception e){
      log.warn("Exception serialize plutusV2 script tx_hash: {}, ",tx.getHash());
    }
    return data;
  }
}
