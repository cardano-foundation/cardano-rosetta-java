package org.cardanofoundation.rosetta.consumer.service.impl.plutus;

import com.bloxbean.cardano.client.transaction.spec.PlutusScript;
import org.cardanofoundation.rosetta.common.entity.Script;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.enumeration.ScriptType;
import org.cardanofoundation.rosetta.consumer.service.SyncServiceInstance;


public abstract class PlutusScriptService<T extends PlutusScript> implements
    SyncServiceInstance<T> {


  public abstract Script handle(T plutusScript, Tx tx);

  protected abstract byte[] getTxScriptBytes(T plutusScript, Tx tx);

  protected Script buildScript(byte[] data, ScriptType type, Tx tx, String hash) {
    tx.addScriptSize(data.length);
    return Script.builder().tx(tx).serialisedSize(data.length).bytes(data)
        .type(type).hash(hash).build();
  }
}
