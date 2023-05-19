package org.cardanofoundation.rosetta.api.projection;

import com.bloxbean.cardano.client.api.model.Utxo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BlockUtxos {
    BlockDto block;
    List<Utxo> utxos;
}
