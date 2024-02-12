package org.cardanofoundation.rosetta.common.ledgersync.plutus;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.ByteString;
import com.bloxbean.cardano.client.exception.CborDeserializationException;

import com.bloxbean.cardano.client.util.HexUtil;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.common.util.CborSerializationUtil;

@SuperBuilder
public class PlutusV2Script extends PlutusScript {
    public PlutusV2Script() {
        this.type = "PlutusScriptV2";
    }

    //plutus_script = bytes ; New
    public static PlutusV2Script deserialize(ByteString plutusScriptDI) throws CborDeserializationException {
        if (plutusScriptDI != null) {
            PlutusV2Script plutusScript = new PlutusV2Script();
            byte[] bytes;
            bytes = CborSerializationUtil.serialize(plutusScriptDI);

            plutusScript.setCborHex(HexUtil.encodeHexString(bytes));
            return plutusScript;
        } else {
            return null;
        }
    }

    @Override
    public byte[] getScriptTypeBytes() {
        return new byte[]{(byte) getScriptType()};
    }

    @Override
    public int getScriptType() {
        return 2;
    }
}
