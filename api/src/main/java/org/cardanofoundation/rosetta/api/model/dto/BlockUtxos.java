package org.cardanofoundation.rosetta.api.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BlockUtxos {

  BlockDto block;
  List<UtxoDto> utxos;
}
