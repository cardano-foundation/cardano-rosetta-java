package org.cardanofoundation.rosetta.api.projection.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.cardanofoundation.rosetta.api.model.rest.MaBalance;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BlockUtxosMultiAssets extends BlockUtxos {

  List<MaBalance> maBalances;
}
