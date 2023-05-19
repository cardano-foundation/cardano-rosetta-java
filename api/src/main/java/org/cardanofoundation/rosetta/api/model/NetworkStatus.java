package org.cardanofoundation.rosetta.api.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cardanofoundation.rosetta.api.projection.BlockDto;
import org.cardanofoundation.rosetta.api.projection.GenesisBlockDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkStatus {
  private BlockDto latestBlock;
  private GenesisBlockDto genesisBlock;
  private List<Peer> peers;
}
