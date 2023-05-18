package org.cardanofoundation.rosetta.common.ledgersync.plutus;

import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.Map;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.util.JsonUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MapPlutusData implements PlutusData {

  @Builder.Default
  @JsonProperty("map")
  private java.util.Map<MapPlusDataKey, MapPlutusDataValue> map = new HashMap<>();

  public static MapPlutusData deserialize(Map mapDI) throws CborDeserializationException {
    if ( Objects.isNull(mapDI)) {
      return null;
    }

    MapPlutusData mapPlutusData = new MapPlutusData();
    for (DataItem keyDI : mapDI.getKeys()) {
      PlutusData key = PlutusData.deserialize(keyDI);
      PlutusData value = PlutusData.deserialize(mapDI.get(keyDI));

      mapPlutusData.put(MapPlusDataKey.builder().key(key).build(),
          MapPlutusDataValue.builder().value(value).build() );
    }

    return mapPlutusData;
  }

  public MapPlutusData put(
      MapPlusDataKey key, MapPlutusDataValue value) {
    if (map == null) {
      map = new HashMap<>();
    }

    map.put(key, value);

    return this;
  }

  @Override
  public DataItem serialize() throws CborSerializationException {
    if (map == null) {
      return null;
    }

    Map plutusDataMap = new Map();
    for (java.util.Map.Entry<MapPlusDataKey, MapPlutusDataValue> entry : map.entrySet()) {
      DataItem key = entry.getKey().getKey().serialize();
      DataItem value = entry.getValue().getValue().serialize();

      if (key == null) {
        throw new CborSerializationException(
            "Cbor serialization failed for PlutusData.  NULL serialized value found for key");
      }

      if (value == null) {
        throw new CborSerializationException(
            "Cbor serialization failed for PlutusData.  NULL serialized value found for value");
      }

      plutusDataMap.put(key, value);
    }

    return plutusDataMap;
  }

  public String toString() {
    return JsonUtil.getPrettyJson(this);
  }

}
