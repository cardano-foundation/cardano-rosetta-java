package org.cardanofoundation.rosetta.api.block.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.api.account.model.dto.UtxoDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BlockUtxos {

  BlockDto block;
  List<UtxoDto> utxos;
}
