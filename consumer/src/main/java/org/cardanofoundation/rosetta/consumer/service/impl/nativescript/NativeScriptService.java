package org.cardanofoundation.rosetta.consumer.service.impl.nativescript;

import org.cardanofoundation.rosetta.common.entity.Script;
import org.cardanofoundation.rosetta.common.entity.Tx;
import org.cardanofoundation.rosetta.common.enumeration.ScriptType;
import org.cardanofoundation.rosetta.common.ledgersync.nativescript.NativeScript;
import org.cardanofoundation.rosetta.consumer.service.SyncServiceInstance;


public abstract class NativeScriptService<T extends NativeScript> implements
    SyncServiceInstance<T> {

  public abstract Script handle(T nativeScript, Tx tx);


  protected Script buildScript(ScriptType type, String hash, Tx tx, String json) {
    return Script.builder().tx(tx).json(json).type(type)
        .hash(hash).build();
  }


}
