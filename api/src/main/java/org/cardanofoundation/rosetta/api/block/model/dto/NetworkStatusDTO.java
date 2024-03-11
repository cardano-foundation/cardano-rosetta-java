package org.cardanofoundation.rosetta.api.block.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openapitools.client.model.Peer;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkStatusDTO {
  private BlockDto latestBlock;
  private GenesisBlockDto genesisBlock;
  private List<Peer> peers;
}
