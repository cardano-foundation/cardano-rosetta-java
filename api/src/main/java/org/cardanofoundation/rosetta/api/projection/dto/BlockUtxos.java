package org.cardanofoundation.rosetta.api.projection.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.api.model.rest.Utxo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BlockUtxos {

  BlockDto block;
  List<Utxo> utxos;
}
