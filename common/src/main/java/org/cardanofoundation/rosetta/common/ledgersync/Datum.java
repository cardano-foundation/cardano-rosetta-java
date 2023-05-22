package org.cardanofoundation.rosetta.common.ledgersync;

import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.DataItem;
import com.bloxbean.cardano.client.crypto.Blake2bUtil;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import org.cardanofoundation.rosetta.common.ledgersync.plutus.PlutusData;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.cardanofoundation.rosetta.common.util.CborSerializationUtil;
import org.cardanofoundation.rosetta.common.util.HexUtil;
import org.cardanofoundation.rosetta.common.util.JsonUtil;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Datum {

  private String hash;
  private String cbor;
  private String json;

  public static Datum from(DataItem plutusDataDI)
      throws CborDeserializationException, CborException {
    PlutusData plutusData = PlutusData.deserialize(plutusDataDI);
    if (Objects.isNull(plutusData)) {
      return null;
    }

    var cbor = CborSerializationUtil.serialize(plutusDataDI, false);
    var datumHash = HexUtil.encodeHexString(Blake2bUtil.blake2bHash256(cbor));
    return Datum.builder()
        .hash(datumHash)
        .cbor(HexUtil.encodeHexString(cbor))
        .json(JsonUtil.getPrettyJson(plutusData))
        .build();
  }
}
