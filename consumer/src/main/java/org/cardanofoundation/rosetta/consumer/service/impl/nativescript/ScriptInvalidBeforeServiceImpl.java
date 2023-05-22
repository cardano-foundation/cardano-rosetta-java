package org.cardanofoundation.rosetta.consumer.service.impl.nativescript;

import com.bloxbean.cardano.client.exception.CborSerializationException;
import org.cardanofoundation.rosetta.common.entity.Script;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.enumeration.ScriptType;
import org.cardanofoundation.rosetta.common.ledgersync.nativescript.RequireTimeBefore;
import org.cardanofoundation.rosetta.common.util.HexUtil;
import org.cardanofoundation.rosetta.common.util.JsonUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ScriptInvalidBeforeServiceImpl extends NativeScriptService<RequireTimeBefore> {

  @Override
  public Script handle(RequireTimeBefore nativeScript, Tx tx) {
    try {
      return buildScript(ScriptType.TIMELOCK, HexUtil.encodeHexString(nativeScript.getScriptHash()),tx,
          JsonUtil.getPrettyJson(nativeScript));
    } catch (CborSerializationException e) {
      log.error("Serialize native script hash error, tx {}",tx.getHash());
    }
    return null;
  }
}
