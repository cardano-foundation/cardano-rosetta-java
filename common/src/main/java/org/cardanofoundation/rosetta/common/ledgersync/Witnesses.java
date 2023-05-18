package org.cardanofoundation.rosetta.common.ledgersync;

import com.bloxbean.cardano.client.transaction.spec.PlutusScript;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.cardanofoundation.rosetta.common.ledgersync.kafka.serializer.PlutusScriptDeserializer;
import org.cardanofoundation.rosetta.common.ledgersync.nativescript.NativeScript;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class Witnesses {

  private List<VkeyWitness> vkeyWitnesses = new ArrayList<>();

  private List<NativeScript> nativeScripts = new ArrayList<>();

  private List<BootstrapWitness> bootstrapWitnesses = new ArrayList<>();

  //Alonzo
  @JsonDeserialize(contentUsing = PlutusScriptDeserializer.class)
  private List<PlutusScript> plutusV1Scripts = new ArrayList<>();

  private List<Datum> datums = new ArrayList<>();

  private List<Redeemer> redeemers = new ArrayList<>();

  @JsonDeserialize(contentUsing = PlutusScriptDeserializer.class)
  private List<PlutusScript> plutusV2Scripts = new ArrayList<>();
}
