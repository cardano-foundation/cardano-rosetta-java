package org.cardanofoundation.rosetta.common.ledgersync;


import com.bloxbean.cardano.client.transaction.spec.PlutusScript;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.cardanofoundation.rosetta.common.ledgersync.kafka.serializer.PlutusScriptDeserializer;
import org.cardanofoundation.rosetta.common.ledgersync.nativescript.NativeScript;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class AuxData {

  private Map<BigDecimal, String> metadataCbor;
  private String metadataJson;

  private List<NativeScript> nativeScripts;

  @JsonDeserialize(contentUsing = PlutusScriptDeserializer.class)
  private List<PlutusScript> plutusV1Scripts;

  @JsonDeserialize(contentUsing = PlutusScriptDeserializer.class)
  private List<PlutusScript> plutusV2Scripts;
}
